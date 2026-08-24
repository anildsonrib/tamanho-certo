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
 * "Converter formato" (referencia visual aprovada em 2026-08-25,
 * `configure_convert_remodelado.html`).
 * `rect x=3 y=4 w=18 h=16 rx=2` + `circle cx=9 cy=10 r=2` + `m4 17 5-5 4 4 2-2 5 5`
 */
val ToolIconFileImage: ImageVector by lazy {
    builder("ToolIconFileImage").apply {
        strokePath {
            moveTo(5f, 4f)
            horizontalLineToRelative(14f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = 2f, dy1 = 2f)
            verticalLineToRelative(12f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = -2f, dy1 = 2f)
            horizontalLineToRelative(-14f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = -2f, dy1 = -2f)
            verticalLineToRelative(-12f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = 2f, dy1 = -2f)
            close()

            moveTo(7f, 10f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = true, isPositiveArc = true, dx1 = 4f, dy1 = 0f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = true, isPositiveArc = true, dx1 = -4f, dy1 = 0f)

            moveTo(4f, 17f)
            lineToRelative(5f, -5f)
            lineToRelative(4f, 4f)
            lineToRelative(2f, -2f)
            lineToRelative(5f, 5f)
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
