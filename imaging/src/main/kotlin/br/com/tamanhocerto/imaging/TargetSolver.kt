package br.com.tamanhocerto.imaging

import br.com.tamanhocerto.imaging.ImagingDefaults.ACCEPT_BAND
import br.com.tamanhocerto.imaging.ImagingDefaults.MAX_QUALITY_ITERATIONS
import br.com.tamanhocerto.imaging.ImagingDefaults.MAX_SCALE_ITERATIONS
import br.com.tamanhocerto.imaging.ImagingDefaults.MIN_SCALE
import br.com.tamanhocerto.imaging.ImagingDefaults.QUALITY_AFTER_DOWNSCALE
import br.com.tamanhocerto.imaging.ImagingDefaults.QUALITY_MAX
import br.com.tamanhocerto.imaging.ImagingDefaults.QUALITY_MIN
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext
import kotlin.math.sqrt

/**
 * NAO IMPORTA `android.*`, e nao pode passar a importar: e o nucleo do
 * produto e a unica logica com risco real de erro, testada com probe falso em
 * teste unitario comum (IMAGING-SPEC secao 1 e ARCHITECTURE.md secao 4).
 */
sealed interface TargetSolution {
    data class Hit(val quality: Int, val scale: Float, val size: Long) : TargetSolution

    /** Nem a qualidade minima basta. O app pergunta ao usuario; nao decide sozinho. */
    data class NeedsDownscale(val bestSize: Long) : TargetSolution

    data class Impossible(val bestSize: Long, val atScale: Float) : TargetSolution

    data class AlreadySmaller(val size: Long) : TargetSolution
}

/**
 * Busca a qualidade — e, se autorizado, a escala — que faz o arquivo caber em
 * `targetBytes`. Algoritmo passo a passo em IMAGING-SPEC secao 4.
 *
 * @param sourceBytes tamanho REAL do arquivo de origem, nunca uma recodificacao.
 * @param reencodeRequired true quando o formato de saida difere do de origem.
 * @param lossless true para formato sem perdas (PNG). A busca por qualidade
 *   nao existe ali — `Bitmap.compress` ignora o parametro —, entao os passos
 *   1 e 2 sao pulados e a unica alavanca e a escala. Antes de 2026-08-27 o
 *   `ImagePipeline` cortava PNG na entrada e devolvia "troque de formato";
 *   o responsavel decidiu manter a extensao e redimensionar, avisando.
 */
suspend fun solveTargetSize(
    targetBytes: Long,
    sourceBytes: Long,
    reencodeRequired: Boolean,
    probe: EncodeProbe,
    allowDownscale: Boolean,
    lossless: Boolean = false,
): TargetSolution {
    // Passo 0 — o original ja cabe? Comparado contra o arquivo de origem, e nao
    // contra uma recodificacao: um JPEG de 300 KB pode crescer para 800 KB ao
    // ser recodificado em qualidade 95, e o passo deixaria de reconhece-lo.
    if (!reencodeRequired && sourceBytes <= targetBytes) {
        return TargetSolution.AlreadySmaller(sourceBytes)
    }

    val counted = CountingProbe(probe)

    // Formato sem perdas: a qualidade nao muda nada. Mede uma vez em escala
    // cheia e, se nao couber, vai direto para a escala.
    if (lossless) {
        val full = counted.sizeAt(QUALITY_MAX, 1f)
        if (full <= targetBytes) return TargetSolution.Hit(QUALITY_MAX, 1f, full)
        if (!allowDownscale) return TargetSolution.NeedsDownscale(full)
        return solveByScale(targetBytes, full, counted)
    }

    // Passo 1 — busca binaria na qualidade, escala fixa em 1f.
    var lo = QUALITY_MIN
    var hi = QUALITY_MAX
    var best: Pair<Int, Long>? = null
    var iterations = 0
    while (lo <= hi && iterations < MAX_QUALITY_ITERATIONS) {
        iterations++
        val mid = (lo + hi) / 2
        val size = counted.sizeAt(mid, 1f)
        if (size <= targetBytes) {
            best = mid to size
            // Dentro da banda de aceitacao ja e bom o bastante: economiza codificacoes.
            if (size >= (targetBytes * ACCEPT_BAND).toLong()) break
            lo = mid + 1
        } else {
            hi = mid - 1
        }
    }
    best?.let { (quality, size) -> return TargetSolution.Hit(quality, 1f, size) }

    // Passo 2 — nem QUALITY_MIN cabe.
    val sizeAtMinQuality = counted.sizeAt(QUALITY_MIN, 1f)
    if (!allowDownscale) return TargetSolution.NeedsDownscale(sizeAtMinQuality)

    // Passo 3 — busca binaria na escala, qualidade fixa. O orcamento de
    // codificacoes e fechado: 1 (passo 2) + 1 (medida em escala cheia) +
    // MAX_SCALE_ITERATIONS, o que fecha o teto de MAX_PROBE_CALLS.
    val sizeAtFullScale = counted.sizeAt(QUALITY_AFTER_DOWNSCALE, 1f)
    return solveByScale(targetBytes, sizeAtFullScale, counted)
}

/**
 * Busca binaria so na escala, com a qualidade fixa. Usada pelo passo 3 da
 * busca normal e, desde 2026-08-27, como unico caminho dos formatos sem
 * perdas.
 */
private suspend fun solveByScale(
    targetBytes: Long,
    sizeAtFullScale: Long,
    counted: CountingProbe,
): TargetSolution {
    val guess = if (sizeAtFullScale <= 0L) {
        1f
    } else {
        sqrt(targetBytes.toDouble() / sizeAtFullScale.toDouble()).toFloat()
    }

    var low = MIN_SCALE
    var high = 1f
    var bestScale = 0f
    var bestScaleSize = 0L
    var scale = guess.coerceIn(MIN_SCALE, 1f)
    for (i in 0 until MAX_SCALE_ITERATIONS) {
        // Sem nenhuma escala aceita ate a ultima tentativa, mede MIN_SCALE: e
        // ela que decide entre Hit e Impossible.
        val isLast = i == MAX_SCALE_ITERATIONS - 1
        if (isLast && bestScale == 0f) scale = MIN_SCALE

        val size = counted.sizeAt(QUALITY_AFTER_DOWNSCALE, scale)
        if (size <= targetBytes) {
            // Mantem a MAIOR escala que cabe.
            if (scale > bestScale) {
                bestScale = scale
                bestScaleSize = size
            }
            low = scale
        } else {
            high = scale
            // So e impossivel se nenhuma escala tiver cabido ate aqui.
            if (isLast && bestScale == 0f) return TargetSolution.Impossible(size, scale)
        }
        scale = (low + high) / 2f
    }

    if (bestScale > 0f) return TargetSolution.Hit(QUALITY_AFTER_DOWNSCALE, bestScale, bestScaleSize)
    return TargetSolution.Impossible(sizeAtFullScale, 1f)
}

/**
 * Cada chamada e uma codificacao real. O teto existe para que uma mudanca no
 * algoritmo nao transforme a compressao em algo que demora dez segundos
 * (IMAGING-SPEC secao 10.1, teste T7).
 */
private class CountingProbe(private val delegate: EncodeProbe) {
    var calls: Int = 0
        private set

    suspend fun sizeAt(quality: Int, scale: Float): Long {
        coroutineContext.ensureActive()
        check(calls < ImagingDefaults.MAX_PROBE_CALLS) {
            "solveTargetSize excedeu ${ImagingDefaults.MAX_PROBE_CALLS} codificacoes"
        }
        calls++
        return delegate.sizeAt(quality, scale)
    }
}
