package br.com.tamanhocerto.feature.tools.home

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.BoxWithConstraints
import br.com.tamanhocerto.core.ui.component.AppScaffold
import br.com.tamanhocerto.core.ui.component.NavIconAbout
import br.com.tamanhocerto.core.ui.component.NavIconPrivacy
import br.com.tamanhocerto.core.ui.component.ToolCard
import br.com.tamanhocerto.core.ui.component.ToolIconCompress
import br.com.tamanhocerto.core.ui.component.ToolIconConvert
import br.com.tamanhocerto.core.ui.component.ToolIconImagesToPdf
import br.com.tamanhocerto.core.ui.component.ToolIconPdfToImages
import br.com.tamanhocerto.core.ui.component.ToolIconResize
import br.com.tamanhocerto.core.ui.theme.HomePaletteDark
import br.com.tamanhocerto.core.ui.theme.HomePaletteLight
import br.com.tamanhocerto.core.ui.theme.ToolAccent
import br.com.tamanhocerto.core.ui.theme.toolAccents
import br.com.tamanhocerto.feature.tools.R
import br.com.tamanhocerto.core.ui.R as UiR

/**
 * Ordem dos cartoes = frequencia esperada de uso (UI-SPEC secao 3), que nao e
 * a ordem do PRD. A cor e o icone de cada item seguem essa mesma ordem
 * (referencia visual aprovada em 2026-08-25).
 */
private val TOOLS = listOf(
    ToolId.COMPRESS to Triple(R.string.tool_compress_title, R.string.tool_compress_sub, ToolIconCompress),
    ToolId.RESIZE to Triple(R.string.tool_resize_title, R.string.tool_resize_sub, ToolIconResize),
    ToolId.IMAGES_TO_PDF to Triple(R.string.tool_img2pdf_title, R.string.tool_img2pdf_sub, ToolIconImagesToPdf),
    ToolId.PDF_TO_IMAGES to Triple(R.string.tool_pdf2img_title, R.string.tool_pdf2img_sub, ToolIconPdfToImages),
    ToolId.CONVERT to Triple(R.string.tool_convert_title, R.string.tool_convert_sub, ToolIconConvert),
)

// Metricas da referencia visual aprovada em 2026-08-26 (`preview(1).html`,
// estilo CamScanner; px = dp/sp, mesma convencao ja usada nesta tela).
private val PagePadding = 22.dp
private val Gap = 14.dp
private val HeaderTopPadding = 48.dp
private val GridTopMargin = 34.dp

@Composable
fun HomeScreen(
    onToolClick: (ToolId) -> Unit,
    onPrivacyClick: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val darkTheme = isSystemInDarkTheme()
    val palette = if (darkTheme) HomePaletteDark else HomePaletteLight
    val accents = toolAccents(darkTheme)

    AppScaffold(
        title = stringResource(R.string.home_title),
        modifier = modifier,
        showTopBar = false,
        containerColor = palette.background,
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Titulo, subtitulo e grade ficam ancorados no topo, com
            // rolagem se a tela for pequena demais para caber tudo — mesma
            // composicao do HTML de referencia (.content com padding-top
            // fixo, sem centralizacao vertical).
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PagePadding)
                        .padding(top = HeaderTopPadding),
                ) {
                    Text(
                        text = stringResource(R.string.home_title),
                        color = palette.text,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 35.sp,
                        letterSpacing = (-0.8).sp,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.home_subtitle),
                        color = palette.textSoft,
                        fontSize = 17.sp,
                        lineHeight = 25.sp,
                    )
                }

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PagePadding)
                        .padding(top = GridTopMargin, bottom = 8.dp),
                ) {
                    val cellWidth = (maxWidth - Gap) / 2
                    Column(verticalArrangement = Arrangement.spacedBy(Gap)) {
                        Row(
                            modifier = Modifier.height(IntrinsicSize.Max),
                            horizontalArrangement = Arrangement.spacedBy(Gap),
                        ) {
                            ToolCardFor(0, accents, onToolClick, Modifier.width(cellWidth).fillMaxHeight())
                            ToolCardFor(1, accents, onToolClick, Modifier.width(cellWidth).fillMaxHeight())
                        }
                        Row(
                            modifier = Modifier.height(IntrinsicSize.Max),
                            horizontalArrangement = Arrangement.spacedBy(Gap),
                        ) {
                            ToolCardFor(2, accents, onToolClick, Modifier.width(cellWidth).fillMaxHeight())
                            ToolCardFor(3, accents, onToolClick, Modifier.width(cellWidth).fillMaxHeight())
                        }
                        // Quinto cartao ("Converter formato") ocupa a
                        // largura toda, em formato horizontal — `.card.full`
                        // no HTML de referencia.
                        ToolCardFor(4, accents, onToolClick, Modifier.fillMaxWidth(), horizontal = true)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PagePadding, vertical = 14.dp)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FooterLink(
                    icon = NavIconPrivacy,
                    text = stringResource(UiR.string.nav_privacy),
                    color = palette.footer,
                    onClick = onPrivacyClick,
                )
                Spacer(
                    Modifier
                        .padding(horizontal = 6.dp)
                        .width(1.dp)
                        .height(22.dp)
                        .background(palette.outline),
                )
                FooterLink(
                    icon = NavIconAbout,
                    text = stringResource(UiR.string.nav_about),
                    color = palette.footer,
                    onClick = onAboutClick,
                )
            }
        }
    }
}

@Composable
private fun FooterLink(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    TextButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.width(16.dp).height(16.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            color = color,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ToolCardFor(
    index: Int,
    accents: List<ToolAccent>,
    onToolClick: (ToolId) -> Unit,
    modifier: Modifier,
    horizontal: Boolean = false,
) {
    val (id, texts) = TOOLS[index]
    val (titleRes, subRes, icon) = texts
    ToolCard(
        title = stringResource(titleRes),
        subtitle = stringResource(subRes),
        icon = icon,
        accent = accents[index],
        onClick = { onToolClick(id) },
        modifier = modifier,
        horizontal = horizontal,
    )
}
