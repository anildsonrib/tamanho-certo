package br.com.tamanhocerto.pdf

/**
 * Constantes fechadas da PDF-SPEC secao 2. O PDF trabalha em PONTOS,
 * onde 1 pt = 1/72 pol.
 */
object PdfDefaults {
    /** 210 x 297 mm a 72 dpi. */
    const val A4_WIDTH_PT = 595
    const val A4_HEIGHT_PT = 842

    /** 8,5 x 11 pol a 72 dpi. */
    const val LETTER_WIDTH_PT = 612
    const val LETTER_HEIGHT_PT = 792

    const val MARGIN_NONE_PT = 0

    /** ~8,5 mm. */
    const val MARGIN_SMALL_PT = 24

    /** ~17 mm. */
    const val MARGIN_MEDIUM_PT = 48

    /** Tela; arquivo pequeno. */
    const val DENSITY_LOW_DPI = 72

    /** Leitura e reimpressao comum. */
    const val DENSITY_MEDIUM_DPI = 150

    /** Padrao de impressao. */
    const val DENSITY_HIGH_DPI = 300

    /** Mesmo teto de memoria do :imaging. */
    const val MAX_RENDER_PIXELS = 12_000_000

    /** Qualidade das imagens dentro do PDF quando nao ha alvo definido. */
    const val EMBED_QUALITY_DEFAULT = 85

    /** Piso ao perseguir alvo de tamanho. */
    const val EMBED_QUALITY_MIN = 40

    /** Quantas vezes o PDF inteiro e remontado para conferir o alvo. */
    const val MAX_PDF_VERIFICATIONS = 2

    /** A busca mira 95% do alvo, deixando folga para a estrutura do PDF. */
    const val PDF_OVERHEAD_MARGIN = 0.95f

    /** Reducao de qualidade a cada remontagem que estoura o alvo. */
    const val QUALITY_STEP_DOWN = 10

    /** Margem que consome mais que isso da pagina zera (PDF-SPEC secao 4.1). */
    const val MAX_MARGIN_RATIO = 0.80f

    /** Maior lado da pagina em FitImage: a altura do A4. */
    const val FIT_IMAGE_LONGEST_SIDE_PT = A4_HEIGHT_PT

    /** Pontos por polegada do sistema de coordenadas do PDF. */
    const val POINTS_PER_INCH = 72f
}
