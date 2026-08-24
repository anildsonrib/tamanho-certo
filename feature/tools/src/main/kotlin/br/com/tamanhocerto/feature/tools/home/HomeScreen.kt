package br.com.tamanhocerto.feature.tools.home

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.BoxWithConstraints
import br.com.tamanhocerto.core.ui.component.AppScaffold
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

// Metricas da referencia visual aprovada (page-padding, gap, radius em px = dp).
private val PagePadding = 14.dp
private val Gap = 12.dp

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
        centerTitle = true,
        titleStyle = TextStyle(
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.35).sp,
        ),
        containerColor = palette.background,
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // O bloco de cartoes fica centralizado no espaco disponivel (nao
            // preso ao topo), com rolagem apenas se a tela for pequena
            // demais para caber tudo — mesma composicao do HTML de
            // referencia, onde <main> tem flex:1 e align-content:center.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PagePadding, vertical = 8.dp),
                ) {
                    val cellWidth = (maxWidth - Gap) / 2
                    Column(verticalArrangement = Arrangement.spacedBy(Gap)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(Gap)) {
                            ToolCardFor(0, accents, onToolClick, Modifier.width(cellWidth))
                            ToolCardFor(1, accents, onToolClick, Modifier.width(cellWidth))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(Gap)) {
                            ToolCardFor(2, accents, onToolClick, Modifier.width(cellWidth))
                            ToolCardFor(3, accents, onToolClick, Modifier.width(cellWidth))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            ToolCardFor(4, accents, onToolClick, Modifier.width(cellWidth))
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PagePadding, vertical = 10.dp)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                TextButton(onClick = onPrivacyClick) {
                    Text(
                        text = stringResource(UiR.string.nav_privacy),
                        color = palette.footer,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                TextButton(onClick = onAboutClick) {
                    Text(
                        text = stringResource(UiR.string.nav_about),
                        color = palette.footer,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolCardFor(
    index: Int,
    accents: List<ToolAccent>,
    onToolClick: (ToolId) -> Unit,
    modifier: Modifier,
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
    )
}
