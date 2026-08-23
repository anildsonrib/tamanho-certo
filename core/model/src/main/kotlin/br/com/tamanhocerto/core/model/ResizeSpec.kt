package br.com.tamanhocerto.core.model

/** Como as novas dimensoes sao pedidas (PRD secao 3.2, IMAGING-SPEC secao 3.3). */
sealed interface ResizeSpec {
    data class Pixels(val width: Int, val height: Int, val lockAspect: Boolean) : ResizeSpec

    /** 1..100. */
    data class Percent(val value: Int) : ResizeSpec

    data class LongestSide(val pixels: Int) : ResizeSpec
}

/** Par de dimensoes em pixels. Puro, para o calculo ser testavel sem aparelho. */
data class Size(val width: Int, val height: Int)
