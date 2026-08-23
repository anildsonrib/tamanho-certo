package br.com.tamanhocerto.imaging

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface

/**
 * Orientacao e metadados.
 *
 * A rotacao e SEMPRE aplicada aos pixels antes de gravar, nas duas politicas
 * de metadado. Sem isso a foto sai deitada — o erro classico desta
 * funcionalidade (PRD secao 3.1).
 */
object ExifPolicy {

    /** Tags copiadas quando a politica e KEEP_ALL. Orientacao fica de fora. */
    private val COPYABLE_TAGS = listOf(
        ExifInterface.TAG_DATETIME,
        ExifInterface.TAG_DATETIME_ORIGINAL,
        ExifInterface.TAG_MAKE,
        ExifInterface.TAG_MODEL,
        ExifInterface.TAG_F_NUMBER,
        ExifInterface.TAG_EXPOSURE_TIME,
        ExifInterface.TAG_FOCAL_LENGTH,
        ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY,
        ExifInterface.TAG_WHITE_BALANCE,
        ExifInterface.TAG_FLASH,
        ExifInterface.TAG_GPS_LATITUDE,
        ExifInterface.TAG_GPS_LATITUDE_REF,
        ExifInterface.TAG_GPS_LONGITUDE,
        ExifInterface.TAG_GPS_LONGITUDE_REF,
        ExifInterface.TAG_GPS_ALTITUDE,
        ExifInterface.TAG_GPS_ALTITUDE_REF,
        ExifInterface.TAG_GPS_TIMESTAMP,
        ExifInterface.TAG_GPS_DATESTAMP,
    )

    /** true quando a orientacao troca largura por altura. */
    fun swapsDimensions(orientation: Int): Boolean = when (orientation) {
        ExifInterface.ORIENTATION_TRANSPOSE,
        ExifInterface.ORIENTATION_ROTATE_90,
        ExifInterface.ORIENTATION_TRANSVERSE,
        ExifInterface.ORIENTATION_ROTATE_270,
        -> true

        else -> false
    }

    /**
     * Devolve o bitmap com a orientacao ja aplicada. Recicla o original quando
     * cria um novo, para nao dobrar o pico de memoria.
     */
    fun applyOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.setScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }

            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(270f)
                matrix.postScale(-1f, 1f)
            }

            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(270f)
            else -> return bitmap
        }

        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated != bitmap) bitmap.recycle()
        return rotated
    }

    /**
     * Copia as tags do original para o arquivo de saida, exceto as de
     * orientacao, que ja foram aplicadas aos pixels. Usado so em KEEP_ALL:
     * em STRIP_ALL o arquivo nasce sem EXIF, porque o encoder grava bytes
     * novos (IMAGING-SPEC secao 8).
     */
    fun copyMetadata(from: ExifInterface, toFile: String) {
        val target = ExifInterface(toFile)
        for (tag in COPYABLE_TAGS) {
            from.getAttribute(tag)?.let { target.setAttribute(tag, it) }
        }
        target.setAttribute(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL.toString(),
        )
        target.saveAttributes()
    }
}
