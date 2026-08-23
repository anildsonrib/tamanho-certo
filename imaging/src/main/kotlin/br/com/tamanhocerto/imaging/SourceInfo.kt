package br.com.tamanhocerto.imaging

import androidx.exifinterface.media.ExifInterface

/**
 * Metadados lidos antes de decodificar.
 *
 * `width` e `height` sao os VISUAIS: com rotacao EXIF de 90 ou 270 graus os
 * valores brutos ja vem trocados. Toda a UI e todo o calculo usam estes
 * (IMAGING-SPEC secao 3.2).
 */
data class SourceInfo(
    val width: Int,
    val height: Int,
    val mimeType: String?,
    val byteSize: Long,
    val exifOrientation: Int = ExifInterface.ORIENTATION_NORMAL,
    val hasAlpha: Boolean = false,
)
