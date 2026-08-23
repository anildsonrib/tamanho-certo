package br.com.tamanhocerto.imaging

import br.com.tamanhocerto.core.model.ResizeSpec
import br.com.tamanhocerto.core.model.Size
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Calculo de dimensoes. Puro: NAO importa `android.*` (IMAGING-SPEC secao 1).
 *
 * Regra do produto: o app nao amplia. Dimensao pedida maior que a original
 * devolve a original — ampliar so inventa pixel.
 */
fun resolveDimensions(source: Size, spec: ResizeSpec): Size {
    val target = when (spec) {
        is ResizeSpec.Pixels ->
            if (spec.lockAspect) {
                // Ajusta pelo eixo que exige MAIOR reducao, para caber dentro de w x h.
                val factor = min(
                    spec.width.toDouble() / source.width,
                    spec.height.toDouble() / source.height,
                )
                Size(
                    (source.width * factor).roundToInt(),
                    (source.height * factor).roundToInt(),
                )
            } else {
                Size(spec.width, spec.height)
            }

        is ResizeSpec.Percent -> Size(
            (source.width * spec.value / 100.0).roundToInt(),
            (source.height * spec.value / 100.0).roundToInt(),
        )

        is ResizeSpec.LongestSide -> {
            val longest = maxOf(source.width, source.height)
            val factor = spec.pixels.toDouble() / longest
            Size(
                (source.width * factor).roundToInt(),
                (source.height * factor).roundToInt(),
            )
        }
    }

    if (target.width >= source.width && target.height >= source.height) return source

    return Size(target.width.coerceAtLeast(1), target.height.coerceAtLeast(1))
}
