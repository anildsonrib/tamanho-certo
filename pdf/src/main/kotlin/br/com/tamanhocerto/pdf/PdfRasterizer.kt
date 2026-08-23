package br.com.tamanhocerto.pdf

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import br.com.tamanhocerto.core.model.FailureReason
import br.com.tamanhocerto.core.model.ImageFormat
import br.com.tamanhocerto.core.model.PageRange
import br.com.tamanhocerto.core.model.RenderDensity
import br.com.tamanhocerto.imaging.toCompressFormat
import br.com.tamanhocerto.pdf.PdfDefaults.DENSITY_HIGH_DPI
import br.com.tamanhocerto.pdf.PdfDefaults.DENSITY_LOW_DPI
import br.com.tamanhocerto.pdf.PdfDefaults.DENSITY_MEDIUM_DPI
import br.com.tamanhocerto.pdf.PdfDefaults.MAX_RENDER_PIXELS
import br.com.tamanhocerto.pdf.PdfDefaults.POINTS_PER_INCH
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.coroutines.coroutineContext
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class PdfRasterizeOutcome(
    val pageCount: Int,
    val renderedPages: Int,
    val wasDownsampledForMemory: Boolean,
)

/**
 * PDF para imagens.
 *
 * Recebe um ARQUIVO local, e nao um stream: `PdfRenderer` exige um
 * `ParcelFileDescriptor` sobre arquivo pesquisavel, e uma URI de documento nao
 * serve. Quem copia para `cacheDir/work` e o `:engine` (PDF-SPEC secao 5).
 */
object PdfRasterizer {

    suspend fun rasterize(
        file: File,
        range: PageRange,
        density: RenderDensity,
        format: ImageFormat,
        quality: Int,
        onPage: suspend (index: Int, bytes: ByteArray) -> Unit,
    ): PdfRasterizeOutcome = withContext(Dispatchers.Default) {
        runPdf {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    val pageCount = renderer.pageCount
                    val indices = PageRangeRules.resolve(range, pageCount)
                    if (indices.isEmpty()) {
                        throw PdfException(FailureReason.UNKNOWN)
                    }

                    var downsampled = false
                    for (index in indices) {
                        coroutineContext.ensureActive()
                        // Uma pagina aberta por vez: PdfRenderer nao permite duas.
                        renderer.openPage(index).use { page ->
                            val bitmap = renderPage(page, density) { downsampled = true }
                            try {
                                val sink = ByteArrayOutputStream()
                                bitmap.compress(format.toCompressFormat(), quality, sink)
                                onPage(index, sink.toByteArray())
                            } finally {
                                bitmap.recycle()
                            }
                        }
                    }

                    PdfRasterizeOutcome(pageCount, indices.size, downsampled)
                }
            }
        }
    }

    private fun renderPage(
        page: PdfRenderer.Page,
        density: RenderDensity,
        onDownsample: () -> Unit,
    ): Bitmap {
        val scale = dpiOf(density) / POINTS_PER_INCH
        var width = (page.width * scale).roundToInt().coerceAtLeast(1)
        var height = (page.height * scale).roundToInt().coerceAtLeast(1)

        val pixels = width.toLong() * height
        if (pixels > MAX_RENDER_PIXELS) {
            val reduction = sqrt(MAX_RENDER_PIXELS.toDouble() / pixels)
            width = (width * reduction).roundToInt().coerceAtLeast(1)
            height = (height * reduction).roundToInt().coerceAtLeast(1)
            onDownsample()
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        // Branco antes de renderizar: PDF com fundo transparente sai preto em
        // JPEG se isso for esquecido (PDF-SPEC secao 5).
        bitmap.eraseColor(Color.WHITE)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        return bitmap
    }

    private fun dpiOf(density: RenderDensity): Float = when (density) {
        RenderDensity.LOW -> DENSITY_LOW_DPI.toFloat()
        RenderDensity.MEDIUM -> DENSITY_MEDIUM_DPI.toFloat()
        RenderDensity.HIGH -> DENSITY_HIGH_DPI.toFloat()
    }
}

/** `PdfRenderer` e `Page` sao Closeable so a partir de APIs recentes. */
private inline fun <T> PdfRenderer.use(block: (PdfRenderer) -> T): T =
    try {
        block(this)
    } finally {
        close()
    }

private inline fun <T> PdfRenderer.Page.use(block: (PdfRenderer.Page) -> T): T =
    try {
        block(this)
    } finally {
        close()
    }
