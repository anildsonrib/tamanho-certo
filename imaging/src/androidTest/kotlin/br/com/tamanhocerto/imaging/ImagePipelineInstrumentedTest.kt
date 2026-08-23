package br.com.tamanhocerto.imaging

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.exifinterface.media.ExifInterface
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.tamanhocerto.core.model.FailureReason
import br.com.tamanhocerto.core.model.ImageFormat
import br.com.tamanhocerto.core.model.MetadataPolicy
import br.com.tamanhocerto.core.model.SizeTarget
import br.com.tamanhocerto.core.model.Suggestion
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Assume.assumeNotNull
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream

/**
 * Testes instrumentados do motor de imagem (IMAGING-SPEC secao 10.2).
 *
 * I2, I3, I4 e I7 dependem de fixtures que so o responsavel pode fornecer
 * (`IMAGING-SPEC` secao 10.3). Eles estao escritos e ficam suspensos por
 * `assumeNotNull` enquanto o arquivo nao existir em `src/androidTest/assets/`:
 * no dia em que a fixture entrar, o teste passa a rodar sem nenhuma alteracao.
 * As tarefas correspondentes estao marcadas `[!]` no `TASKS.md`.
 */
@RunWith(AndroidJUnit4::class)
class ImagePipelineInstrumentedTest {

    /** I1 — foto grande com alvo em bytes: a saida cabe no alvo. */
    @Test
    fun comprime_ate_o_alvo_em_bytes() = runTest {
        val source = BytesSource(TestSources.photoBytes(2400, 1800))
        val target = 120_000L
        val out = ByteArrayOutputStream()

        val outcome = ImagePipeline.compress(
            source = source,
            target = SizeTarget.Bytes(target),
            format = ImageFormat.JPEG,
            metadata = MetadataPolicy.STRIP_ALL,
            allowDownscale = true,
            out = out,
        )

        assertTrue("saiu com ${outcome.bytesWritten} bytes", outcome.bytesWritten <= target)
        assertTrue(outcome.targetHit)
        assertNotNull(BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size()))
    }

    /** I2 — EXIF de rotacao 90 graus aplicado aos pixels. */
    @Test
    fun aplica_a_rotacao_do_exif_aos_pixels() = runTest {
        val bytes = TestSources.assetOrNull("photo_rotated90.jpg")
        assumeNotNull("fixture photo_rotated90.jpg ausente", bytes)

        val source = BytesSource(bytes!!)
        val info = ImageReader.readInfo(source)
        val out = ByteArrayOutputStream()

        ImagePipeline.convert(
            source = source,
            format = ImageFormat.JPEG,
            flattenColor = Color.WHITE,
            metadata = MetadataPolicy.STRIP_ALL,
            out = out,
        )

        // As dimensoes visuais ja vem trocadas, e a saida nasce em NORMAL.
        val result = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size())
        assertEquals(info.width, result.width)
        assertEquals(info.height, result.height)
        val exif = ExifInterface(out.toByteArray().inputStream())
        assertEquals(
            ExifInterface.ORIENTATION_NORMAL,
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL),
        )
    }

    /** I3 — STRIP_ALL nao deixa nenhuma tag de GPS na saida. */
    @Test
    fun strip_all_remove_o_gps() = runTest {
        val bytes = TestSources.assetOrNull("photo_with_gps.jpg")
        assumeNotNull("fixture photo_with_gps.jpg ausente", bytes)

        val out = ByteArrayOutputStream()
        ImagePipeline.convert(
            source = BytesSource(bytes!!),
            format = ImageFormat.JPEG,
            flattenColor = Color.WHITE,
            metadata = MetadataPolicy.STRIP_ALL,
            out = out,
        )

        val exif = ExifInterface(out.toByteArray().inputStream())
        assertNull(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE))
        assertNull(exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE))
    }

    /** I4 — KEEP_ALL preserva as tags, menos a de orientacao. */
    @Test
    fun keep_all_preserva_as_tags() = runTest {
        val bytes = TestSources.assetOrNull("photo_with_gps.jpg")
        assumeNotNull("fixture photo_with_gps.jpg ausente", bytes)

        val file = kotlin.io.path.createTempFile(suffix = ".jpg").toFile()
        file.outputStream().use { out ->
            ImagePipeline.convert(
                source = BytesSource(bytes!!),
                format = ImageFormat.JPEG,
                flattenColor = Color.WHITE,
                metadata = MetadataPolicy.KEEP_ALL,
                out = out,
                outputFile = file,
            )
        }

        val exif = ExifInterface(file.absolutePath)
        assertNotNull(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE))
        assertEquals(
            ExifInterface.ORIENTATION_NORMAL,
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1),
        )
        file.delete()
    }

    /** I5 — PNG transparente para JPEG achata sobre a cor escolhida. */
    @Test
    fun png_transparente_para_jpeg_achata_no_branco() = runTest {
        val out = ByteArrayOutputStream()

        ImagePipeline.convert(
            source = BytesSource(TestSources.transparentPngBytes()),
            format = ImageFormat.JPEG,
            flattenColor = Color.WHITE,
            metadata = MetadataPolicy.STRIP_ALL,
            out = out,
        )

        val result = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size())
        assertTrue(!result.hasAlpha())
        // A metade de baixo era transparente e agora precisa estar branca.
        val pixel = result.getPixel(result.width / 2, result.height - 2)
        assertTrue("pixel=$pixel", Color.red(pixel) > 240 && Color.green(pixel) > 240)
    }

    /** I6 — PNG com alvo em bytes nem tenta codificar. */
    @Test
    fun png_com_alvo_em_bytes_sugere_formato_com_perdas() = runTest {
        val out = ByteArrayOutputStream()

        val outcome = ImagePipeline.compress(
            source = BytesSource(TestSources.transparentPngBytes()),
            target = SizeTarget.Bytes(1_000),
            format = ImageFormat.PNG,
            metadata = MetadataPolicy.STRIP_ALL,
            allowDownscale = true,
            out = out,
        )

        assertEquals(Suggestion.TRY_LOSSY_FORMAT, outcome.suggestion)
        assertEquals(0L, outcome.bytesWritten)
        assertEquals(0, out.size())
    }

    /** I7 — HEIC decodifica (e o que justifica o minSdk 28). */
    @Test
    fun decodifica_heic() = runTest {
        val bytes = TestSources.assetOrNull("sample.heic")
        assumeNotNull("fixture sample.heic ausente", bytes)

        val info = ImageReader.readInfo(BytesSource(bytes!!))
        assertTrue(info.width > 0 && info.height > 0)
    }

    /**
     * I8 — imagem acima do orcamento de pixels e reduzida para caber na
     * memoria, e o fato sobe no resultado. O orcamento e passado reduzido em
     * vez de gerar uma imagem de 108 MP: decodificar 108 MP no proprio teste
     * estouraria a heap do processo de teste, que e o que o codigo evita.
     */
    @Test
    fun imagem_acima_do_orcamento_e_reduzida_e_avisa() = runTest {
        val source = BytesSource(TestSources.photoBytes(2000, 2000))

        val decoded = ImageReader.decode(source, maxPixels = 250_000)

        assertTrue(decoded.wasDownsampledForMemory)
        assertTrue(decoded.bitmap.width * decoded.bitmap.height <= 250_000)
        decoded.bitmap.recycle()
    }

    /** I9 — arquivo corrompido vira FILE_CORRUPT, e nao "erro inesperado". */
    @Test
    fun arquivo_corrompido_vira_file_corrupt() = runTest {
        try {
            ImageReader.readInfo(BytesSource(TestSources.corruptBytes()))
            fail("deveria ter falhado")
        } catch (error: ImagingException) {
            assertEquals(FailureReason.FILE_CORRUPT, error.reason)
        }
    }

    /** I10 — WebP produz arquivo valido na versao de Android em que roda. */
    @Test
    fun webp_produz_arquivo_valido() = runTest {
        val out = ByteArrayOutputStream()

        ImagePipeline.convert(
            source = BytesSource(TestSources.photoBytes(600, 400)),
            format = ImageFormat.WEBP,
            flattenColor = Color.WHITE,
            metadata = MetadataPolicy.STRIP_ALL,
            out = out,
        )

        val decoded: Bitmap? = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size())
        assertNotNull(decoded)
        assertEquals(600, decoded!!.width)
    }
}
