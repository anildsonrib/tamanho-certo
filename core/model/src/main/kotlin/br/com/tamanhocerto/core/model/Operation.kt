package br.com.tamanhocerto.core.model

/** Tudo o que o app faz e uma operacao: entra arquivo, sai arquivo. */
sealed interface Operation {
    data class Compress(val target: SizeTarget, val format: ImageFormat) : Operation

    data class Resize(val spec: ResizeSpec, val format: ImageFormat) : Operation

    data class Convert(val format: ImageFormat, val flattenColor: Int) : Operation

    data class ImagesToPdf(val page: PageSpec, val target: SizeTarget?) : Operation

    data class PdfToImages(
        val pages: PageRange,
        val density: RenderDensity,
        val format: ImageFormat,
    ) : Operation
}

sealed interface SizeTarget {
    /** O diferencial do produto: o usuario diz quanto o arquivo pode pesar. */
    data class Bytes(val max: Long) : SizeTarget

    /** Modo avancado: o controle deslizante tradicional. */
    data class Quality(val value: Int) : SizeTarget
}

/**
 * Opcoes transversais, validas para qualquer operacao.
 *
 * NAO carrega formato de saida: o formato pertence a propria Operation, onde
 * ele faz sentido. Duas fontes para o formato geravam ambiguidade
 * (ARCHITECTURE.md secao 3).
 */
data class RunOptions(
    /** Resposta do usuario ao NeedsDownscale. O app nunca reduz sozinho. */
    val allowDownscale: Boolean = false,
    val metadata: MetadataPolicy = MetadataPolicy.STRIP_ALL,
)
