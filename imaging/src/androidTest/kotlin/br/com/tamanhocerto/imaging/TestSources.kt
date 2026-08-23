package br.com.tamanhocerto.imaging

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.test.platform.app.InstrumentationRegistry
import br.com.tamanhocerto.core.model.ByteSource
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

/** ByteSource sobre bytes em memoria, para os testes instrumentados. */
class BytesSource(
    private val bytes: ByteArray,
    override val displayName: String? = "test",
) : ByteSource {
    override val byteSize: Long get() = bytes.size.toLong()

    override suspend fun openStream(): InputStream = ByteArrayInputStream(bytes)
}

object TestSources {

    /** Fixture do repositorio; null quando ainda nao foi fornecida. */
    fun assetOrNull(name: String): ByteArray? {
        val assets = InstrumentationRegistry.getInstrumentation().context.assets
        return runCatching { assets.open(name).use { it.readBytes() } }.getOrNull()
    }

    /** Foto sintetica com gradiente: comprime como foto, nao como cor chapada. */
    fun photoBytes(
        width: Int,
        height: Int,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        quality: Int = 95,
    ): ByteArray {
        val bitmap = photoBitmap(width, height)
        return try {
            ByteArrayOutputStream().also { bitmap.compress(format, quality, it) }.toByteArray()
        } finally {
            bitmap.recycle()
        }
    }

    fun photoBitmap(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        var i = 0
        while (i < width) {
            paint.color = Color.rgb((i * 7) % 256, (i * 13) % 256, (i * 29) % 256)
            canvas.drawRect(i.toFloat(), 0f, (i + 8).toFloat(), height.toFloat(), paint)
            i += 8
        }
        return bitmap
    }

    /** PNG com metade transparente. */
    fun transparentPngBytes(size: Int = 200): ByteArray {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT)
        canvas.drawRect(
            0f,
            0f,
            size.toFloat(),
            size / 2f,
            Paint().apply { color = Color.RED },
        )
        return try {
            ByteArrayOutputStream().also { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                .toByteArray()
        } finally {
            bitmap.recycle()
        }
    }

    /** Bytes que nao sao imagem nenhuma. */
    fun corruptBytes(): ByteArray = ByteArray(4096) { (it * 31 % 251).toByte() }
}
