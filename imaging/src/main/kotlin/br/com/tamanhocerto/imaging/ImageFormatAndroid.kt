package br.com.tamanhocerto.imaging

import android.graphics.Bitmap
import android.os.Build
import br.com.tamanhocerto.core.model.ImageFormat

/**
 * A unica ponte entre o enum puro de `:core:model` e o Android.
 *
 * `Bitmap.CompressFormat.WEBP` foi descontinuado na API 30, que introduziu
 * WEBP_LOSSY. Como `minSdk` e 28, a escolha e por versao — e essa diferenca
 * vive so aqui (IMAGING-SPEC secao 3.1).
 */
fun ImageFormat.toCompressFormat(): Bitmap.CompressFormat = when (this) {
    ImageFormat.JPEG -> Bitmap.CompressFormat.JPEG
    ImageFormat.PNG -> Bitmap.CompressFormat.PNG
    ImageFormat.WEBP ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            @Suppress("DEPRECATION")
            Bitmap.CompressFormat.WEBP
        }
}

/** Formatos aceitos na LEITURA (IMAGING-SPEC secao 9). */
val SUPPORTED_INPUT_MIME_TYPES = setOf(
    "image/jpeg",
    "image/png",
    "image/webp",
    "image/heic",
    "image/heif",
    "image/gif",
    "image/bmp",
)
