package br.com.tamanhocerto.imaging

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
import br.com.tamanhocerto.core.model.ByteSource
import br.com.tamanhocerto.core.model.FailureReason
import br.com.tamanhocerto.imaging.ImagingDefaults.MAX_DECODE_PIXELS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class DecodedImage(
    val bitmap: Bitmap,
    val info: SourceInfo,
    /** true = reduzida para caber na memoria, e nao a pedido do usuario. */
    val wasDownsampledForMemory: Boolean,
)

/**
 * Decodificacao em duas passadas, sempre (ARCHITECTURE.md secao 5): primeiro
 * as dimensoes, depois o bitmap dentro do orcamento de pixels.
 */
object ImageReader {

    suspend fun readInfo(source: ByteSource): SourceInfo = withContext(Dispatchers.IO) {
        runImaging {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            source.openStream().use { BitmapFactory.decodeStream(it, null, bounds) }

            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                throw ImagingException(FailureReason.FILE_CORRUPT)
            }
            val mime = bounds.outMimeType
            if (mime != null && mime !in SUPPORTED_INPUT_MIME_TYPES) {
                throw ImagingException(FailureReason.FORMAT_UNSUPPORTED)
            }

            val orientation = source.openStream().use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )
            }
            val swap = ExifPolicy.swapsDimensions(orientation)

            SourceInfo(
                width = if (swap) bounds.outHeight else bounds.outWidth,
                height = if (swap) bounds.outWidth else bounds.outHeight,
                mimeType = mime,
                // Ausencia de tamanho significa "nao sei": nao inventar zero.
                byteSize = source.byteSize ?: -1L,
                exifOrientation = orientation,
                hasAlpha = mime == "image/png" || mime == "image/webp",
            )
        }
    }

    suspend fun decode(
        source: ByteSource,
        maxPixels: Int = MAX_DECODE_PIXELS,
    ): DecodedImage = withContext(Dispatchers.IO) {
        runImaging {
            val info = readInfo(source)
            val sampleSize = sampleSizeFor(info.width, info.height, maxPixels)

            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val decoded = source.openStream().use { BitmapFactory.decodeStream(it, null, options) }
                ?: throw ImagingException(FailureReason.FILE_CORRUPT)

            DecodedImage(
                bitmap = ExifPolicy.applyOrientation(decoded, info.exifOrientation),
                info = info,
                wasDownsampledForMemory = sampleSize > 1,
            )
        }
    }

    /** Menor potencia de 2 tal que a area decodificada caiba no orcamento. */
    internal fun sampleSizeFor(width: Int, height: Int, maxPixels: Int): Int {
        var sample = 1
        while ((width.toLong() / sample) * (height.toLong() / sample) > maxPixels) {
            sample *= 2
        }
        return sample
    }
}
