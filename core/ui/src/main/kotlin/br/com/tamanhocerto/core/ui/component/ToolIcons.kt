package br.com.tamanhocerto.core.ui.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Icones das cinco ferramentas da `home`. Traçado copiado ponto a ponto dos
 * `<svg>` da referencia visual aprovada (`tamanho_certo_home_centralizado.html`,
 * 2026-08-25) — viewport 24x24, traço 2, sem preenchimento. A cor final vem
 * do `tint` do `Icon`, não do path.
 */
private const val VIEWBOX = 24f
private val StrokeWidth = 2f

private fun ImageVector.Builder.strokePath(block: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit) {
    path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = StrokeWidth,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathBuilder = block,
    )
}

/** Preenchimento solido, para os quadradinhos do xadrez de `FormatIconPng`. */
private fun ImageVector.Builder.fillPath(block: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit) {
    path(fill = SolidColor(Color.Black), pathBuilder = block)
}

private fun builder(name: String) = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = VIEWBOX,
    viewportHeight = VIEWBOX,
)

/** `M15 3H6a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z` + `M15 3v6h6` + `M8 13l2 2 4-4` */
val ToolIconCompress: ImageVector by lazy {
    builder("ToolIconCompress").apply {
        strokePath {
            moveTo(15f, 3f)
            lineTo(6f, 3f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, dx1 = -2f, dy1 = 2f)
            verticalLineToRelative(14f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, dx1 = 2f, dy1 = 2f)
            horizontalLineToRelative(12f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, dx1 = 2f, dy1 = -2f)
            verticalLineTo(9f)
            close()

            moveTo(15f, 3f)
            verticalLineToRelative(6f)
            horizontalLineToRelative(6f)

            moveTo(8f, 13f)
            lineToRelative(2f, 2f)
            lineToRelative(4f, -4f)
        }
    }.build()
}

/** `M16 3h5v5` + `M8 21H3v-5` + `M21 3l-7 7` + `M3 21l7-7` */
val ToolIconResize: ImageVector by lazy {
    builder("ToolIconResize").apply {
        strokePath {
            moveTo(16f, 3f)
            horizontalLineToRelative(5f)
            verticalLineToRelative(5f)

            moveTo(8f, 21f)
            horizontalLineTo(3f)
            verticalLineToRelative(-5f)

            moveTo(21f, 3f)
            lineToRelative(-7f, 7f)

            moveTo(3f, 21f)
            lineToRelative(7f, -7f)
        }
    }.build()
}

/** `rect x=3 y=3 w=12 h=14 rx=2` + `M9 21h9a2 2 0 0 0 2-2V9l-6-6` */
val ToolIconImagesToPdf: ImageVector by lazy {
    builder("ToolIconImagesToPdf").apply {
        strokePath {
            moveTo(5f, 3f)
            horizontalLineToRelative(8f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = 2f, dy1 = 2f)
            verticalLineToRelative(10f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = -2f, dy1 = 2f)
            horizontalLineToRelative(-8f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = -2f, dy1 = -2f)
            verticalLineToRelative(-10f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = 2f, dy1 = -2f)
            close()

            moveTo(9f, 21f)
            horizontalLineToRelative(9f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, dx1 = 2f, dy1 = -2f)
            verticalLineTo(9f)
            lineToRelative(-6f, -6f)
        }
    }.build()
}

/** `M14 3H6a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z` + `M14 3v6h6` + circle(10,16,2) + `M14 18l1.5-2 1.5 2` */
val ToolIconPdfToImages: ImageVector by lazy {
    builder("ToolIconPdfToImages").apply {
        strokePath {
            moveTo(14f, 3f)
            lineTo(6f, 3f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, dx1 = -2f, dy1 = 2f)
            verticalLineToRelative(14f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, dx1 = 2f, dy1 = 2f)
            horizontalLineToRelative(12f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, dx1 = 2f, dy1 = -2f)
            verticalLineTo(9f)
            close()

            moveTo(14f, 3f)
            verticalLineToRelative(6f)
            horizontalLineToRelative(6f)

            moveTo(8f, 16f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = true, isPositiveArc = true, dx1 = 4f, dy1 = 0f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = true, isPositiveArc = true, dx1 = -4f, dy1 = 0f)

            moveTo(14f, 18f)
            lineToRelative(1.5f, -2f)
            lineToRelative(1.5f, 2f)
        }
    }.build()
}

/**
 * Icone de arquivo de imagem, usado no cartao de resumo da tela
 * "Converter formato" — pagina com dobra no canto superior direito
 * (referencia visual aprovada em 2026-08-26, mockup enviado pelo
 * responsavel), no lugar do retangulo simples anterior.
 */
val ToolIconFileImage: ImageVector by lazy {
    builder("ToolIconFileImage").apply {
        strokePath {
            // Contorno da pagina, com a dobra em (15,4)-(15,9)-(20,9).
            moveTo(7f, 3f)
            horizontalLineToRelative(8f)
            lineToRelative(5f, 5f)
            verticalLineToRelative(11f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = -2f, dy1 = 2f)
            horizontalLineToRelative(-11f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = -2f, dy1 = -2f)
            verticalLineToRelative(-14f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = 2f, dy1 = -2f)
            close()

            moveTo(15f, 3f)
            verticalLineToRelative(5f)
            horizontalLineToRelative(5f)

            // Glifo de imagem (sol + montanha), centralizado na metade
            // inferior da pagina.
            moveTo(9.5f, 13f)
            arcToRelative(1.3f, 1.3f, 0f, isMoreThanHalf = true, isPositiveArc = true, dx1 = 2.6f, dy1 = 0f)
            arcToRelative(1.3f, 1.3f, 0f, isMoreThanHalf = true, isPositiveArc = true, dx1 = -2.6f, dy1 = 0f)

            moveTo(7f, 19f)
            lineToRelative(3.5f, -3.5f)
            lineToRelative(2.5f, 2.5f)
            lineToRelative(2f, -2f)
            lineToRelative(3f, 3f)
        }
    }.build()
}

/**
 * Icones do rodape (Politica de privacidade / Sobre), referencia visual
 * aprovada em 2026-08-26 (`preview(1).html`).
 * `rect x=5 y=10 w=14 h=10 rx=2` + `M8 10V7a4 4 0 0 1 8 0v3`
 */
val NavIconPrivacy: ImageVector by lazy {
    builder("NavIconPrivacy").apply {
        strokePath {
            moveTo(5f, 10f)
            horizontalLineToRelative(14f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = 2f, dy1 = 2f)
            verticalLineToRelative(6f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = -2f, dy1 = 2f)
            horizontalLineToRelative(-14f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = -2f, dy1 = -2f)
            verticalLineToRelative(-6f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = 2f, dy1 = -2f)
            close()

            moveTo(8f, 10f)
            verticalLineTo(7f)
            arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = 8f, dy1 = 0f)
            verticalLineToRelative(3f)
        }
    }.build()
}

/** `circle cx=12 cy=12 r=9` + `M12 11v5` + `M12 8h.01` */
val NavIconAbout: ImageVector by lazy {
    builder("NavIconAbout").apply {
        strokePath {
            moveTo(21f, 12f)
            arcToRelative(9f, 9f, 0f, isMoreThanHalf = true, isPositiveArc = true, dx1 = -18f, dy1 = 0f)
            arcToRelative(9f, 9f, 0f, isMoreThanHalf = true, isPositiveArc = true, dx1 = 18f, dy1 = 0f)
            close()

            moveTo(12f, 11f)
            verticalLineToRelative(5f)

            moveTo(12f, 8f)
            lineToRelative(0.01f, 0f)
        }
    }.build()
}

/** `M21 12a9 9 0 1 1-2.64-6.36` + `M21 3v6h-6` */
val ToolIconConvert: ImageVector by lazy {
    builder("ToolIconConvert").apply {
        strokePath {
            moveTo(21f, 12f)
            arcToRelative(9f, 9f, 0f, isMoreThanHalf = true, isPositiveArc = true, dx1 = -2.64f, dy1 = -6.36f)

            moveTo(21f, 3f)
            verticalLineToRelative(6f)
            horizontalLineToRelative(-6f)
        }
    }.build()
}

/**
 * Seta de "voltar", usada no botao "Voltar" da tela "Converter formato"
 * (referencia visual aprovada em 2026-08-26, mockup enviado pelo
 * responsavel) — `M15 6l-6 6 6 6`.
 */
val NavIconBackChevron: ImageVector by lazy {
    builder("NavIconBackChevron").apply {
        strokePath {
            moveTo(15f, 6f)
            lineToRelative(-6f, 6f)
            lineToRelative(6f, 6f)
        }
    }.build()
}

/**
 * Pasta, usada no botao "Selecionar arquivos" da tela "Converter formato"
 * (mesma referencia do icone acima).
 * `M3 7a2 2 0 0 1 2-2h4l2 2h8a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z`
 */
val ActionIconFolder: ImageVector by lazy {
    builder("ActionIconFolder").apply {
        // Preenchida, nao contornada (recorte de referencia enviado pelo
        // responsavel em 2026-08-26): pasta branca solida sobre o botao.
        fillPath {
            moveTo(3f, 7f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = 2f, dy1 = -2f)
            horizontalLineToRelative(4f)
            lineToRelative(2f, 2f)
            horizontalLineToRelative(8f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = 2f, dy1 = 2f)
            verticalLineToRelative(8f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = -2f, dy1 = 2f)
            horizontalLineToRelative(-14f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = -2f, dy1 = -2f)
            close()
        }
    }.build()
}

/**
 * Icones dos chips de formato, na tela "Converter formato" (mesma
 * referencia dos dois icones acima): foto simples para JPEG, arquivo
 * generico para WEBP. PNG usa `TransparencyChecker` (Canvas, nao vetor —
 * o padrao xadrez precisa de duas cores fixas, independente da selecao).
 */
val FormatIconPhoto: ImageVector by lazy {
    builder("FormatIconPhoto").apply {
        strokePath {
            moveTo(4f, 5f)
            horizontalLineToRelative(16f)
            arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = 1f, dy1 = 1f)
            verticalLineToRelative(12f)
            arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = -1f, dy1 = 1f)
            horizontalLineToRelative(-16f)
            arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = -1f, dy1 = -1f)
            verticalLineToRelative(-12f)
            arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = 1f, dy1 = -1f)
            close()

            moveTo(8.3f, 10f)
            arcToRelative(1.3f, 1.3f, 0f, isMoreThanHalf = true, isPositiveArc = true, dx1 = 2.6f, dy1 = 0f)
            arcToRelative(1.3f, 1.3f, 0f, isMoreThanHalf = true, isPositiveArc = true, dx1 = -2.6f, dy1 = 0f)

            moveTo(5f, 18f)
            lineToRelative(4.5f, -4.5f)
            lineToRelative(3f, 3f)
            lineToRelative(2.5f, -2.5f)
            lineToRelative(4f, 4f)
        }
    }.build()
}

/**
 * PNG: moldura arredondada com o xadrez de transparencia dentro
 * (referencia visual aprovada em 2026-08-26, recorte enviado pelo
 * responsavel) — mesmo estilo de traco dos outros dois icones de formato,
 * e nao um quadriculado solto. O xadrez e preenchido; a moldura, tracada.
 */
val FormatIconPng: ImageVector by lazy {
    builder("FormatIconPng").apply {
        strokePath {
            moveTo(6f, 3f)
            horizontalLineToRelative(12f)
            arcToRelative(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = 3f, dy1 = 3f)
            verticalLineToRelative(12f)
            arcToRelative(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = -3f, dy1 = 3f)
            horizontalLineToRelative(-12f)
            arcToRelative(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = -3f, dy1 = -3f)
            verticalLineToRelative(-12f)
            arcToRelative(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = 3f, dy1 = -3f)
            close()
        }
        // Xadrez 4x4 de quadrados de 3x3, entre 6 e 18: preenchido nas
        // casas em que (coluna + linha) e par.
        fillPath {
            listOf(6f, 9f, 12f, 15f).forEachIndexed { col, x ->
                listOf(6f, 9f, 12f, 15f).forEachIndexed { row, y ->
                    if ((col + row) % 2 == 0) {
                        moveTo(x, y)
                        horizontalLineToRelative(3f)
                        verticalLineToRelative(3f)
                        horizontalLineToRelative(-3f)
                        close()
                    }
                }
            }
        }
    }.build()
}

/** `rect x=4 y=3 w=16 h=18 rx=1.5` + `M9 3v4h6V3` (arquivo generico) */
val FormatIconFile: ImageVector by lazy {
    builder("FormatIconFile").apply {
        strokePath {
            moveTo(7f, 3f)
            horizontalLineToRelative(7f)
            lineToRelative(4f, 4f)
            verticalLineToRelative(13f)
            arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = -1f, dy1 = 1f)
            horizontalLineToRelative(-10f)
            arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = -1f, dy1 = -1f)
            verticalLineToRelative(-16f)
            arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = 1f, dy1 = -1f)
            close()

            moveTo(14f, 3f)
            verticalLineToRelative(4f)
            horizontalLineToRelative(4f)
        }
    }.build()
}
