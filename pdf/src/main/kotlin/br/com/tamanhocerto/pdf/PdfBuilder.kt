package br.com.tamanhocerto.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import br.com.tamanhocerto.core.model.ByteSource
import br.com.tamanhocerto.core.model.MetadataPolicy
import br.com.tamanhocerto.core.model.PageSpec
import br.com.tamanhocerto.core.model.SizeTarget
import br.com.tamanhocerto.core.model.Suggestion
import br.com.tamanhocerto.imaging.ImageReader
import br.com.tamanhocerto.pdf.PdfDefaults.EMBED_QUALITY_DEFAULT
import br.com.tamanhocerto.pdf.PdfDefaults.EMBED_QUALITY_MIN
import br.com.tamanhocerto.pdf.PdfDefaults.MAX_PDF_VERIFICATIONS
import br.com.tamanhocerto.pdf.PdfDefaults.QUALITY_STEP_DOWN
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import kotlin.coroutines.coroutineContext

data class PdfBuildOutcome(
    val bytesWritten: Long,
    val pageCount: Int,
    val embedQualityUsed: Int,
    val targetHit: Boolean,
    val suggestion: Suggestion?,
)

/**
 * Imagens para PDF. As imagens sao embutidas sempre como JPEG, mesmo quando a
 * origem e PNG: PNG dentro de PDF de fotos multiplica o tamanho sem ganho
 * visivel, e a transparencia e achatada sobre branco (PDF-SPEC secao 4.2).
 */
object PdfBuilder {

    suspend fun build(
        sources: List<ByteSource>,
        spec: PageSpec,
        target: SizeTarget?,
        metadata: MetadataPolicy,
        out: OutputStream,
    ): PdfBuildOutcome = withContext(Dispatchers.Default) {
        runPdf {
            when (target) {
                null -> renderAtQuality(sources, spec, EMBED_QUALITY_DEFAULT, out).let {
                    PdfBuildOutcome(it.bytes, it.pages, EMBED_QUALITY_DEFAULT, true, Suggestion.NONE)
                }

                is SizeTarget.Quality -> renderAtQuality(sources, spec, target.value, out).let {
                    PdfBuildOutcome(it.bytes, it.pages, target.value, true, Suggestion.NONE)
                }

                is SizeTarget.Bytes -> buildToTarget(sources, spec, target.max, out)
            }
        }
    }

    /**
     * Remontar o PDF inteiro a cada iteracao seria lento com muitas paginas.
     * A qualidade e estimada sobre a soma das imagens codificadas e so depois
     * verificada montando o documento (PDF-SPEC secao 4.2).
     */
    private suspend fun buildToTarget(
        sources: List<ByteSource>,
        spec: PageSpec,
        targetBytes: Long,
        out: OutputStream,
    ): PdfBuildOutcome {
        var quality = PdfTargetSolver.estimateQuality(sources, targetBytes)
        var last: RenderResult? = null

        repeat(MAX_PDF_VERIFICATIONS) { attempt ->
            val sink = ByteArrayOutputStream()
            val result = renderAtQuality(sources, spec, quality, sink)

            if (result.bytes <= targetBytes || attempt == MAX_PDF_VERIFICATIONS - 1) {
                out.write(sink.toByteArray())
                out.flush()
                val hit = result.bytes <= targetBytes
                return PdfBuildOutcome(
                    bytesWritten = result.bytes,
                    pageCount = result.pages,
                    embedQualityUsed = quality,
                    targetHit = hit,
                    // Entrega, nao falha: o app da o melhor que conseguiu e diz a verdade.
                    suggestion = if (hit) Suggestion.NONE else Suggestion.NEEDS_DOWNSCALE,
                )
            }

            last = result
            quality = (quality - QUALITY_STEP_DOWN).coerceAtLeast(EMBED_QUALITY_MIN)
        }

        // Inalcancavel: o repeat sempre retorna na ultima tentativa.
        error("montagem do PDF terminou sem resultado (ultimo=$last)")
    }

    private class RenderResult(val bytes: Long, val pages: Int)

    /**
     * Uma imagem por vez: decodifica, desenha, recicla. Manter a lista inteira
     * decodificada e o caminho mais curto para OutOfMemoryError num PDF de 30
     * fotos (PDF-SPEC secao 4.3).
     */
    private suspend fun renderAtQuality(
        sources: List<ByteSource>,
        spec: PageSpec,
        quality: Int,
        out: OutputStream,
    ): RenderResult {
        val document = PdfDocument()
        var pages = 0
        try {
            for ((index, source) in sources.withIndex()) {
                coroutineContext.ensureActive()
                val decoded = ImageReader.decode(source)
                try {
                    val placement = computePlacement(
                        imageWidth = decoded.bitmap.width,
                        imageHeight = decoded.bitmap.height,
                        spec = spec,
                    )
                    val pageInfo = PdfDocument.PageInfo.Builder(
                        placement.pageWidthPt,
                        placement.pageHeightPt,
                        index + 1,
                    ).create()

                    val page = document.startPage(pageInfo)
                    // Fundo branco: sem isso a area fora da imagem sai
                    // transparente e vira preto ao ser convertida.
                    page.canvas.drawColor(Color.WHITE)
                    page.canvas.drawEmbedded(decoded.bitmap, placement, quality)
                    document.finishPage(page)
                    pages++
                } finally {
                    if (!decoded.bitmap.isRecycled) decoded.bitmap.recycle()
                }
            }

            val counting = CountingOutputStream(out)
            document.writeTo(counting)
            counting.flush()
            return RenderResult(counting.bytesWritten, pages)
        } finally {
            document.close()
        }
    }

    /**
     * Desenha a imagem ja recodificada em JPEG na qualidade pedida: e a
     * recodificacao que faz o alvo de tamanho do PDF existir.
     */
    private fun Canvas.drawEmbedded(bitmap: Bitmap, placement: Placement, quality: Int) {
        val jpegBytes = ByteArrayOutputStream().also {
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, it)
        }.toByteArray()

        val recoded = android.graphics.BitmapFactory.decodeByteArray(
            jpegBytes,
            0,
            jpegBytes.size,
        ) ?: bitmap

        try {
            val rect = placement.destRectPt
            drawBitmap(bitmap = recoded, dest = Rect(rect.left, rect.top, rect.right, rect.bottom))
        } finally {
            if (recoded !== bitmap) recoded.recycle()
        }
    }

    private fun Canvas.drawBitmap(bitmap: Bitmap, dest: Rect) {
        drawBitmap(bitmap, null, dest, null)
    }
}

private class CountingOutputStream(private val delegate: OutputStream) : OutputStream() {
    var bytesWritten: Long = 0L
        private set

    override fun write(b: Int) {
        delegate.write(b)
        bytesWritten++
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        delegate.write(b, off, len)
        bytesWritten += len
    }

    override fun flush() = delegate.flush()

    override fun close() = delegate.close()
}
