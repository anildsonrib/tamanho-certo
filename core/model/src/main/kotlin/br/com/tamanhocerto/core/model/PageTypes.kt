package br.com.tamanhocerto.core.model

/** Tamanho da pagina do PDF (PRD secao 3.4). */
sealed interface PageSize {
    data object A4 : PageSize
    data object Letter : PageSize
    data object FitImage : PageSize
}

enum class PageOrientation { AUTO, PORTRAIT, LANDSCAPE }

enum class PageMargin { NONE, SMALL, MEDIUM }

data class PageSpec(
    val size: PageSize,
    val orientation: PageOrientation,
    val margin: PageMargin,
)

/** Quais paginas extrair de um PDF. */
sealed interface PageRange {
    data object All : PageRange

    /** 1-based, inclusivo nas duas pontas. */
    data class Interval(val from: Int, val to: Int) : PageRange
}
