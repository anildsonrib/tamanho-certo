package br.com.tamanhocerto.core.model

/**
 * Formatos de ESCRITA suportados. A conversao para o tipo do Android e uma
 * extensao em `:imaging` — este modulo e Kotlin puro (ARCHITECTURE.md secao 2).
 */
enum class ImageFormat(val mimeType: String, val extension: String) {
    JPEG("image/jpeg", "jpg"),
    PNG("image/png", "png"),
    WEBP("image/webp", "webp"),
    ;

    /** Se aceita o parametro de qualidade. PNG e sem perdas e nao aceita. */
    val isLossy: Boolean get() = this != PNG
}
