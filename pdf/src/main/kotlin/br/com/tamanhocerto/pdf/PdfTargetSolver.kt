package br.com.tamanhocerto.pdf

import android.graphics.Bitmap
import br.com.tamanhocerto.core.model.ByteSource
import br.com.tamanhocerto.imaging.EncodeProbe
import br.com.tamanhocerto.imaging.ImageReader
import br.com.tamanhocerto.imaging.TargetSolution
import br.com.tamanhocerto.imaging.solveTargetSize
import br.com.tamanhocerto.pdf.PdfDefaults.EMBED_QUALITY_DEFAULT
import br.com.tamanhocerto.pdf.PdfDefaults.EMBED_QUALITY_MIN
import br.com.tamanhocerto.pdf.PdfDefaults.PDF_OVERHEAD_MARGIN
import java.io.ByteArrayOutputStream

/**
 * Fase de estimativa do alvo de tamanho do PDF (PDF-SPEC secao 4.2).
 *
 * Reaproveita o `solveTargetSize` do `:imaging` com um probe que devolve a
 * SOMA das imagens codificadas: as imagens dominam o tamanho do PDF e a
 * estrutura e pequena e previsivel, por isso a busca mira
 * `PDF_OVERHEAD_MARGIN` do alvo.
 */
object PdfTargetSolver {

    suspend fun estimateQuality(sources: List<ByteSource>, targetBytes: Long): Int {
        if (sources.isEmpty()) return EMBED_QUALITY_DEFAULT

        val probe = EncodeProbe { quality, _ -> sumOfEncodedSizes(sources, quality) }
        val aimed = (targetBytes * PDF_OVERHEAD_MARGIN).toLong()

        val solution = solveTargetSize(
            targetBytes = aimed,
            sourceBytes = Long.MAX_VALUE, // sempre recodifica: nao ha "ja cabe" aqui
            reencodeRequired = true,
            probe = probe,
            // A escala das imagens dentro do PDF vem do layout da pagina, nao
            // da busca: aqui so a qualidade varia.
            allowDownscale = false,
        )

        return when (solution) {
            is TargetSolution.Hit -> solution.quality
            // Nem a qualidade minima da busca cabe: o piso do PDF assume.
            is TargetSolution.NeedsDownscale -> EMBED_QUALITY_MIN
            is TargetSolution.Impossible -> EMBED_QUALITY_MIN
            is TargetSolution.AlreadySmaller -> EMBED_QUALITY_DEFAULT
        }
    }

    /** Uma imagem por vez, sempre: nunca a lista inteira decodificada. */
    private suspend fun sumOfEncodedSizes(sources: List<ByteSource>, quality: Int): Long {
        var total = 0L
        for (source in sources) {
            val decoded = ImageReader.decode(source)
            try {
                val sink = ByteArrayOutputStream()
                decoded.bitmap.compress(Bitmap.CompressFormat.JPEG, quality, sink)
                total += sink.size().toLong()
            } finally {
                if (!decoded.bitmap.isRecycled) decoded.bitmap.recycle()
            }
        }
        return total
    }
}
