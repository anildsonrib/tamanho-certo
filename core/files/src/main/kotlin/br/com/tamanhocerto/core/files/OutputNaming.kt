package br.com.tamanhocerto.core.files

import br.com.tamanhocerto.core.files.EngineDefaults.MAX_BASE_NAME_LENGTH

/**
 * PURO: nao importa `android.*` (ENGINE-SPEC secao 1).
 *
 * O sufixo chega ja traduzido, de fora: `:core:files` nao tem acesso a recurso
 * de string (ENGINE-SPEC secao 6).
 */
object OutputNaming {

    /** Invalidos em nome de arquivo na maioria dos sistemas. */
    private val INVALID_CHARS = Regex("""[\/:*?"<>|]""")

    fun nameFor(
        originalName: String?,
        suffix: String,
        extension: String,
        existingNames: Set<String> = emptySet(),
        fallbackBase: String = "arquivo",
        pageNumber: Int? = null,
    ): String {
        val withoutExtension = originalName?.substringBeforeLast('.', originalName)
        val cleaned = withoutExtension
            ?.replace(INVALID_CHARS, "")
            ?.trim()
            .orEmpty()

        val base = cleaned.ifEmpty { fallbackBase }.take(MAX_BASE_NAME_LENGTH)
        // Em PdfToImages o numero da pagina vem ANTES do sufixo.
        val page = pageNumber?.let { "-" + it.toString().padStart(2, '0') }.orEmpty()

        val candidate = "$base$page$suffix.$extension"
        if (candidate !in existingNames) return candidate

        var attempt = 2
        while ("$base$page$suffix-$attempt.$extension" in existingNames) attempt++
        return "$base$page$suffix-$attempt.$extension"
    }
}
