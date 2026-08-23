package br.com.tamanhocerto.pdf

import br.com.tamanhocerto.core.model.PageRange

/** PURO: nao importa `android.*` (PDF-SPEC secao 1). */
object PageRangeRules {

    /** Intervalo 1-based valido: `from >= 1`, `to <= pageCount`, `from <= to`. */
    fun isValid(range: PageRange, pageCount: Int): Boolean = when (range) {
        is PageRange.All -> pageCount >= 1
        is PageRange.Interval -> range.from >= 1 && range.to <= pageCount && range.from <= range.to
    }

    /** Indices 0-based das paginas a processar. Lista vazia se o intervalo for invalido. */
    fun resolve(range: PageRange, pageCount: Int): List<Int> {
        if (!isValid(range, pageCount)) return emptyList()
        return when (range) {
            is PageRange.All -> (0 until pageCount).toList()
            is PageRange.Interval -> ((range.from - 1) until range.to).toList()
        }
    }
}
