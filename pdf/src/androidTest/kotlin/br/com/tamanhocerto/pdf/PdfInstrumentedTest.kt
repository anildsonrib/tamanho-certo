package br.com.tamanhocerto.pdf

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import br.com.tamanhocerto.core.model.ByteSource
import br.com.tamanhocerto.core.model.FailureReason
import br.com.tamanhocerto.core.model.ImageFormat
import br.com.tamanhocerto.core.model.MetadataPolicy
import br.com.tamanhocerto.core.model.PageMargin
import br.com.tamanhocerto.core.model.PageOrientation
import br.com.tamanhocerto.core.model.PageRange
import br.com.tamanhocerto.core.model.PageSize
import br.com.tamanhocerto.core.model.PageSpec
import br.com.tamanhocerto.core.model.RenderDensity
import br.com.tamanhocerto.core.model.SizeTarget
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Assume.assumeNotNull
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

/**
 * Testes instrumentados do motor de PDF (PDF-SPEC secao 7.2).
 *
 * Q5 depende de `protected.pdf`, que so o responsavel pode fornecer: nao ha
 * como criar PDF com senha usando so a API do Android. O teste esta escrito e
 * fica suspenso por `assumeNotNull` ate o arquivo existir em
 * `src/androidTest/assets/`. A tarefa esta `[!]` no `TASKS.md`.
 */
@RunWith(AndroidJUnit4::class)
class PdfInstrumentedTest {

    private class BytesSource(private val bytes: ByteArray) : ByteSource {
        override val displayName: String = "test"
        override val byteSize: Long get() = bytes.size.toLong()
        override suspend fun openStream(): InputStream = ByteArrayInputStream(bytes)
    }

    private val cacheDir: File
        get() = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir

    /** Q1 — tres imagens viram um PDF A4 de tres paginas. */
    @Test
    fun tres_imagens_viram_pdf_de_tres_paginas() = runTest {
        val out = ByteArrayOutputStream()

        val outcome = PdfBuilder.build(
            sources = List(3) { BytesSource(photoJpeg(800, 600)) },
            spec = PageSpec(PageSize.A4, PageOrientation.AUTO, PageMargin.SMALL),
            target = null,
            metadata = MetadataPolicy.STRIP_ALL,
            out = out,
        )

        assertEquals(3, outcome.pageCount)
        assertTrue(outcome.bytesWritten > 0)
        assertEquals(3, pageCountOf(writeTemp("q1.pdf", out.toByteArray())))
    }

    /** Q2 — alvo de tamanho respeitado, com no maximo duas montagens. */
    @Test
    fun pdf_com_alvo_de_tamanho_cabe_no_alvo() = runTest {
        val target = 300_000L
        val out = ByteArrayOutputStream()

        val outcome = PdfBuilder.build(
            sources = List(3) { BytesSource(photoJpeg(1600, 1200)) },
            spec = PageSpec(PageSize.A4, PageOrientation.AUTO, PageMargin.NONE),
            target = SizeTarget.Bytes(target),
            metadata = MetadataPolicy.STRIP_ALL,
            out = out,
        )

        assertTrue("saiu com ${outcome.bytesWritten}", outcome.bytesWritten <= target)
        assertTrue(outcome.targetHit)
        assertTrue(outcome.embedQualityUsed >= PdfDefaults.EMBED_QUALITY_MIN)
    }

    /** Q3 — PDF de duas paginas vira duas imagens, na densidade pedida. */
    @Test
    fun pdf_de_duas_paginas_vira_duas_imagens() = runTest {
        val file = writeTemp("q3.pdf", twoPagePdf())
        val pages = mutableListOf<Pair<Int, ByteArray>>()

        val outcome = PdfRasterizer.rasterize(
            file = file,
            range = PageRange.All,
            density = RenderDensity.LOW,
            format = ImageFormat.JPEG,
            quality = 90,
            onPage = { index, bytes -> pages += index to bytes },
        )

        assertEquals(2, outcome.pageCount)
        assertEquals(2, pages.size)
        val first = BitmapFactory.decodeByteArray(pages[0].second, 0, pages[0].second.size)
        assertNotNull(first)
        // Densidade baixa = 72 dpi = 1 px por ponto.
        assertEquals(PdfDefaults.A4_WIDTH_PT, first.width)
    }

    /** Q4 — intervalo de paginas rasteriza so o que foi pedido. */
    @Test
    fun intervalo_rasteriza_so_as_paginas_pedidas() = runTest {
        val file = writeTemp("q4.pdf", twoPagePdf())
        val indices = mutableListOf<Int>()

        PdfRasterizer.rasterize(
            file = file,
            range = PageRange.Interval(2, 2),
            density = RenderDensity.LOW,
            format = ImageFormat.JPEG,
            quality = 90,
            onPage = { index, _ -> indices += index },
        )

        assertEquals(listOf(1), indices)
    }

    /** Q5 — PDF protegido por senha tem motivo proprio. */
    @Test
    fun pdf_protegido_por_senha_tem_motivo_proprio() = runTest {
        val bytes = assetOrNull("protected.pdf")
        assumeNotNull("fixture protected.pdf ausente", bytes)

        val file = writeTemp("q5.pdf", bytes!!)
        try {
            PdfRasterizer.rasterize(
                file = file,
                range = PageRange.All,
                density = RenderDensity.LOW,
                format = ImageFormat.JPEG,
                quality = 90,
                onPage = { _, _ -> },
            )
            fail("deveria ter falhado")
        } catch (error: PdfException) {
            assertEquals(FailureReason.PDF_PASSWORD_PROTECTED, error.reason)
        }
    }

    /** Q6 — arquivo corrompido vira FILE_CORRUPT. */
    @Test
    fun arquivo_corrompido_vira_file_corrupt() = runTest {
        val file = writeTemp("q6.pdf", ByteArray(2048) { (it * 17 % 251).toByte() })

        try {
            PdfRasterizer.rasterize(
                file = file,
                range = PageRange.All,
                density = RenderDensity.LOW,
                format = ImageFormat.JPEG,
                quality = 90,
                onPage = { _, _ -> },
            )
            fail("deveria ter falhado")
        } catch (error: PdfException) {
            assertEquals(FailureReason.FILE_CORRUPT, error.reason)
        }
    }

    /** Q7 — pagina sem fundo sai branca em JPEG, nunca preta. */
    @Test
    fun pagina_sem_fundo_sai_branca_em_jpeg() = runTest {
        val file = writeTemp("q7.pdf", transparentBackgroundPdf())
        var bytes: ByteArray? = null

        PdfRasterizer.rasterize(
            file = file,
            range = PageRange.All,
            density = RenderDensity.LOW,
            format = ImageFormat.JPEG,
            quality = 90,
            onPage = { _, page -> bytes = page },
        )

        val bitmap = BitmapFactory.decodeByteArray(bytes!!, 0, bytes!!.size)
        val corner = bitmap.getPixel(2, 2)
        assertTrue(
            "canto=$corner",
            Color.red(corner) > 240 && Color.green(corner) > 240 && Color.blue(corner) > 240,
        )
    }

    /** Q8 — 30 imagens concluem sem OutOfMemoryError. */
    @Test
    fun trinta_imagens_concluem_sem_estourar_a_memoria() = runTest {
        val out = ByteArrayOutputStream()

        val outcome = PdfBuilder.build(
            sources = List(30) { BytesSource(photoJpeg(1200, 900)) },
            spec = PageSpec(PageSize.A4, PageOrientation.AUTO, PageMargin.NONE),
            target = null,
            metadata = MetadataPolicy.STRIP_ALL,
            out = out,
        )

        assertEquals(30, outcome.pageCount)
    }

    // --- apoio ---------------------------------------------------------

    private fun assetOrNull(name: String): ByteArray? {
        val assets = InstrumentationRegistry.getInstrumentation().context.assets
        return runCatching { assets.open(name).use { it.readBytes() } }.getOrNull()
    }

    private fun writeTemp(name: String, bytes: ByteArray): File =
        File(cacheDir, name).apply { writeBytes(bytes) }

    private fun pageCountOf(file: File): Int {
        val descriptor = android.os.ParcelFileDescriptor.open(
            file,
            android.os.ParcelFileDescriptor.MODE_READ_ONLY,
        )
        val renderer = android.graphics.pdf.PdfRenderer(descriptor)
        return try {
            renderer.pageCount
        } finally {
            renderer.close()
            descriptor.close()
        }
    }

    private fun photoJpeg(width: Int, height: Int): ByteArray {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        var x = 0
        while (x < width) {
            paint.color = Color.rgb((x * 7) % 256, (x * 13) % 256, (x * 29) % 256)
            canvas.drawRect(x.toFloat(), 0f, (x + 6).toFloat(), height.toFloat(), paint)
            x += 6
        }
        return try {
            ByteArrayOutputStream().also { bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it) }
                .toByteArray()
        } finally {
            bitmap.recycle()
        }
    }

    private fun twoPagePdf(): ByteArray {
        val document = PdfDocument()
        repeat(2) { index ->
            val info = PdfDocument.PageInfo.Builder(
                PdfDefaults.A4_WIDTH_PT,
                PdfDefaults.A4_HEIGHT_PT,
                index + 1,
            ).create()
            val page = document.startPage(info)
            page.canvas.drawColor(Color.WHITE)
            page.canvas.drawText("pagina ${index + 1}", 40f, 60f, Paint().apply { textSize = 24f })
            document.finishPage(page)
        }
        return ByteArrayOutputStream().also { document.writeTo(it) }.toByteArray()
            .also { document.close() }
    }

    /** Pagina sem nenhum fundo desenhado: e o caso que sai preto se esquecido. */
    private fun transparentBackgroundPdf(): ByteArray {
        val document = PdfDocument()
        val info = PdfDocument.PageInfo.Builder(200, 200, 1).create()
        val page = document.startPage(info)
        page.canvas.drawCircle(100f, 100f, 20f, Paint().apply { color = Color.RED })
        document.finishPage(page)
        return ByteArrayOutputStream().also { document.writeTo(it) }.toByteArray()
            .also { document.close() }
    }
}
