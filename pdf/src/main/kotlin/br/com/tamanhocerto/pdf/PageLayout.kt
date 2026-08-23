package br.com.tamanhocerto.pdf

import br.com.tamanhocerto.core.model.PageMargin
import br.com.tamanhocerto.core.model.PageOrientation
import br.com.tamanhocerto.core.model.PageSize
import br.com.tamanhocerto.core.model.PageSpec
import br.com.tamanhocerto.pdf.PdfDefaults.A4_HEIGHT_PT
import br.com.tamanhocerto.pdf.PdfDefaults.A4_WIDTH_PT
import br.com.tamanhocerto.pdf.PdfDefaults.FIT_IMAGE_LONGEST_SIDE_PT
import br.com.tamanhocerto.pdf.PdfDefaults.LETTER_HEIGHT_PT
import br.com.tamanhocerto.pdf.PdfDefaults.LETTER_WIDTH_PT
import br.com.tamanhocerto.pdf.PdfDefaults.MARGIN_MEDIUM_PT
import br.com.tamanhocerto.pdf.PdfDefaults.MARGIN_NONE_PT
import br.com.tamanhocerto.pdf.PdfDefaults.MARGIN_SMALL_PT
import br.com.tamanhocerto.pdf.PdfDefaults.MAX_MARGIN_RATIO
import kotlin.math.min
import kotlin.math.roundToInt

/** Retangulo em pontos, dentro da pagina. */
data class RectPt(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    val width: Int get() = right - left
    val height: Int get() = bottom - top
}

data class Placement(
    val pageWidthPt: Int,
    val pageHeightPt: Int,
    val destRectPt: RectPt,
)

/**
 * PURO: nao importa `android.*` (PDF-SPEC secao 1). Uma imagem por pagina na
 * v1, sem excecao.
 */
fun computePlacement(
    imageWidth: Int,
    imageHeight: Int,
    spec: PageSpec,
): Placement {
    // 1 — orientacao.
    val landscape = when (spec.orientation) {
        PageOrientation.AUTO -> imageWidth > imageHeight
        PageOrientation.PORTRAIT -> false
        PageOrientation.LANDSCAPE -> true
    }

    // 2 — tamanho da pagina.
    val (pageWidth, pageHeight) = when (spec.size) {
        PageSize.A4 -> orient(A4_WIDTH_PT, A4_HEIGHT_PT, landscape)
        PageSize.Letter -> orient(LETTER_WIDTH_PT, LETTER_HEIGHT_PT, landscape)
        // A pagina assume as proporcoes da imagem, com o maior lado fixado
        // para que nao saia gigante nem minuscula.
        PageSize.FitImage -> {
            val longest = maxOf(imageWidth, imageHeight).coerceAtLeast(1)
            val factor = FIT_IMAGE_LONGEST_SIDE_PT.toDouble() / longest
            (imageWidth * factor).roundToInt().coerceAtLeast(1) to
                (imageHeight * factor).roundToInt().coerceAtLeast(1)
        }
    }

    // 3 — margem. Margem grande em pagina pequena tornaria a area util
    // inexistente: nesse caso ela cai para zero.
    val requested = when (spec.margin) {
        PageMargin.NONE -> MARGIN_NONE_PT
        PageMargin.SMALL -> MARGIN_SMALL_PT
        PageMargin.MEDIUM -> MARGIN_MEDIUM_PT
    }
    val margin = effectiveMarginPt(requested, pageWidth, pageHeight)

    val usableWidth = pageWidth - 2 * margin
    val usableHeight = pageHeight - 2 * margin

    // 4 — encaixe por contencao, centralizado. Nunca corta, nunca distorce e
    // nunca amplia alem de 100% da resolucao da imagem.
    val fitFactor = min(
        usableWidth.toDouble() / imageWidth,
        usableHeight.toDouble() / imageHeight,
    )
    val factor = min(fitFactor, 1.0)
    val drawWidth = (imageWidth * factor).roundToInt().coerceAtLeast(1)
    val drawHeight = (imageHeight * factor).roundToInt().coerceAtLeast(1)

    val left = margin + (usableWidth - drawWidth) / 2
    val top = margin + (usableHeight - drawHeight) / 2

    return Placement(
        pageWidthPt = pageWidth,
        pageHeightPt = pageHeight,
        destRectPt = RectPt(left, top, left + drawWidth, top + drawHeight),
    )
}

/**
 * Margem que consome mais de [MAX_MARGIN_RATIO] da menor dimensao da pagina
 * cai para zero: margem grande em pagina pequena tornaria a area util
 * inexistente (PDF-SPEC secao 4.1).
 *
 * Com A4 e Carta a guarda nunca dispara — ela existe para o dia em que uma
 * pagina menor for possivel.
 */
internal fun effectiveMarginPt(requestedPt: Int, pageWidthPt: Int, pageHeightPt: Int): Int =
    if (2 * requestedPt > MAX_MARGIN_RATIO * min(pageWidthPt, pageHeightPt)) {
        MARGIN_NONE_PT
    } else {
        requestedPt
    }

private fun orient(width: Int, height: Int, landscape: Boolean): Pair<Int, Int> =
    if (landscape) height to width else width to height
