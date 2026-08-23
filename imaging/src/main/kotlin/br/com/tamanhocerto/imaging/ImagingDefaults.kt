package br.com.tamanhocerto.imaging

/**
 * Constantes fechadas da IMAGING-SPEC secao 2. Cada numero traz a origem.
 * Nao alterar sem alterar a especificacao.
 */
object ImagingDefaults {
    /** 12 MP em ARGB_8888 = ~48 MB de pico; cobre a foto padrao de celular. */
    const val MAX_DECODE_PIXELS = 12_000_000

    /** Abaixo disso o artefato de JPEG e visivel e o resultado e inutil. */
    const val QUALITY_MIN = 30

    /** Acima de 95 o arquivo cresce muito e o ganho visual e despreziveis. */
    const val QUALITY_MAX = 95

    /** Qualidade fixa durante a busca por escala: varia uma dimensao por vez. */
    const val QUALITY_AFTER_DOWNSCALE = 80

    /** Busca binaria em 30..95 (66 valores) precisa de ceil(log2 66) = 7. */
    const val MAX_QUALITY_ITERATIONS = 7

    /** Suficiente para convergir a escala com tolerancia de 2%. */
    const val MAX_SCALE_ITERATIONS = 5

    /** Abaixo de 20% da dimensao original a imagem deixa de servir ao proposito. */
    const val MIN_SCALE = 0.20f

    /** Resultado entre 90% e 100% do alvo ja e bom o bastante: encerra a busca. */
    const val ACCEPT_BAND = 0.90f

    /** Branco, ao achatar transparencia para JPEG. */
    const val DEFAULT_FLATTEN_COLOR: Int = 0xFFFFFFFF.toInt()

    /** Teto absoluto de codificacoes por operacao (IMAGING-SPEC secao 4). */
    const val MAX_PROBE_CALLS = MAX_QUALITY_ITERATIONS + MAX_SCALE_ITERATIONS + 2
}
