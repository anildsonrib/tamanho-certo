package br.com.tamanhocerto.feature.tools.home

/**
 * Identificador de cada uma das cinco operacoes. Viaja na rota
 * `configure/{operationId}` (UI-SPEC secao 2).
 */
enum class ToolId {
    COMPRESS,
    RESIZE,
    IMAGES_TO_PDF,
    PDF_TO_IMAGES,
    CONVERT,
    ;

    companion object {
        fun fromRouteArg(value: String?): ToolId? =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}
