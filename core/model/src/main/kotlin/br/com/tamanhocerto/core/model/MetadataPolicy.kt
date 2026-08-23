package br.com.tamanhocerto.core.model

/**
 * O que fazer com os metadados do arquivo de origem.
 *
 * Em ambos os casos a orientacao EXIF e aplicada aos pixels antes de gravar
 * (IMAGING-SPEC secao 3.4): sem isso a foto sai deitada.
 */
enum class MetadataPolicy {
    /** Padrao do app: remove GPS e dados de camera (PRD secao 3.1). */
    STRIP_ALL,
    KEEP_ALL,
}
