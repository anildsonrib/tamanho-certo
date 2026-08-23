package br.com.tamanhocerto.imaging

import android.graphics.Bitmap
import android.graphics.Canvas
import br.com.tamanhocerto.core.model.ImageFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import kotlin.math.roundToInt

/** Codificacao real. PNG ignora `quality` — a propria API ja ignora. */
object ImageEncoder {

    suspend fun encode(
        bitmap: Bitmap,
        format: ImageFormat,
        quality: Int,
        scale: Float,
        flattenColor: Int?,
        out: OutputStream,
    ): Long = withContext(Dispatchers.Default) {
        runImaging {
            val counting = CountingOutputStream(out)
            withPrepared(bitmap, format, scale, flattenColor) { prepared ->
                prepared.compress(format.toCompressFormat(), quality, counting)
            }
            counting.flush()
            counting.bytesWritten
        }
    }

    /**
     * Implementacao real de [EncodeProbe]: codifica em memoria e devolve so o
     * tamanho. NUNCA escreve em disco durante a busca — sao ate 14
     * codificacoes (IMAGING-SPEC secao 7).
     */
    fun probeFor(
        bitmap: Bitmap,
        format: ImageFormat,
        flattenColor: Int?,
    ): EncodeProbe = EncodeProbe { quality, scale ->
        withContext(Dispatchers.Default) {
            runImaging {
                val sink = ByteArrayOutputStream()
                withPrepared(bitmap, format, scale, flattenColor) { prepared ->
                    prepared.compress(format.toCompressFormat(), quality, sink)
                }
                sink.size().toLong()
            }
        }
    }

    /**
     * Prepara o bitmap (escala e achatamento) e o recicla ao fim quando foi
     * criado aqui. Um bitmap grande por vez (ARCHITECTURE.md secao 5).
     */
    private inline fun <T> withPrepared(
        bitmap: Bitmap,
        format: ImageFormat,
        scale: Float,
        flattenColor: Int?,
        block: (Bitmap) -> T,
    ): T {
        val scaled = if (scale == 1f) {
            bitmap
        } else {
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).roundToInt().coerceAtLeast(1),
                (bitmap.height * scale).roundToInt().coerceAtLeast(1),
                true,
            )
        }

        // JPEG nao tem canal alfa: sem achatar, a transparencia vira preto.
        val needsFlatten = flattenColor != null && format == ImageFormat.JPEG && scaled.hasAlpha()
        val prepared = if (needsFlatten) flatten(scaled, flattenColor) else scaled

        return try {
            block(prepared)
        } finally {
            if (prepared !== scaled) prepared.recycle()
            if (scaled !== bitmap) scaled.recycle()
        }
    }

    private fun flatten(bitmap: Bitmap, color: Int): Bitmap {
        val opaque = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        Canvas(opaque).apply {
            drawColor(color)
            drawBitmap(bitmap, 0f, 0f, null)
        }
        return opaque
    }
}

/** Conta os bytes escritos sem precisar consultar o arquivo depois. */
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
