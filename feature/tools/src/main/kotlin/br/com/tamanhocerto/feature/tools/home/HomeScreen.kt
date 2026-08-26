package br.com.tamanhocerto.feature.tools.home

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
// estilo CamScanner). Ajustadas no mesmo dia (pedido do responsavel): os
// quatro primeiros cartoes usam proporcao 3:4 (largura:altura), o quinto
// tem a mesma altura dos outros mas ocupa a largura toda (como se fossem
// dois cartoes juntos), e a largura/altura de cada um e calculada a partir
// do espaco realmente disponivel (`GridArea` abaixo) para que as cinco
// ferramentas cabam na tela sem rolagem e sem sobra, em qualquer aparelho.
private val PagePadding = 20.dp
private val Gap = 10.dp
private val HeaderTopPadding = 8.dp
private const val CardAspectWidthToHeight = 3f / 4f

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
            // Cabecalho com altura propria (nao ocupa espaco extra alem do
            // seu conteudo) — e medido primeiro pelo Column, sobrando para
            // a grade exatamente o espaco entre ele e o rodape.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PagePadding)
                    .padding(top = HeaderTopPadding),
            ) {
                Text(
                    text = stringResource(R.string.home_title),
                    color = palette.text,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 28.sp,
                    letterSpacing = (-0.6).sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.home_subtitle),
                    color = palette.textSoft,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                )
            }

            // Area da grade: ocupa exatamente o espaco que sobra entre o
            // cabecalho e o rodape (`weight(1f)`), nunca mais nem menos —
            // e o que garante caber as cinco ferramentas sem rolagem e sem
            // sobra em qualquer altura de tela. Dentro dela, `cellWidth` e
            // calculado tanto pela largura quanto pela altura disponiveis
            // (o menor dos dois vence, como um "contain") para que os
            // quatro primeiros cartoes fiquem exatamente 3:4 sem estourar
            // nenhum dos dois eixos.
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = PagePadding, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                val cellWidthFromWidth = (maxWidth - Gap) / 2
                val cardHeightFromHeight = (maxHeight - Gap * 2) / 3
                val cellWidthFromHeight = cardHeightFromHeight * CardAspectWidthToHeight
                val cellWidth = minOf(cellWidthFromWidth, cellWidthFromHeight)
                val cardHeight = cellWidth / CardAspectWidthToHeight
                val gridWidth = cellWidth * 2 + Gap

                Column(
                    modifier = Modifier.width(gridWidth),
                    verticalArrangement = Arrangement.spacedBy(Gap),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Gap)) {
                        ToolCardFor(0, accents, onToolClick, Modifier.width(cellWidth).height(cardHeight))
                        ToolCardFor(1, accents, onToolClick, Modifier.width(cellWidth).height(cardHeight))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(Gap)) {
                        ToolCardFor(2, accents, onToolClick, Modifier.width(cellWidth).height(cardHeight))
                        ToolCardFor(3, accents, onToolClick, Modifier.width(cellWidth).height(cardHeight))
                    }
                    // Quinto cartao ("Converter formato"): mesma altura dos
                    // outros quatro, mas ocupa a largura toda da grade —
                    // como se fossem dois cartoes lado a lado — em formato
                    // horizontal (`.card.full` no HTML de referencia).
                    ToolCardFor(
                        4,
                        accents,
                        onToolClick,
                        Modifier.width(gridWidth).height(cardHeight),
                        horizontal = true,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PagePadding, vertical = 6.dp)
                    .padding(bottom = 4.dp),
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
