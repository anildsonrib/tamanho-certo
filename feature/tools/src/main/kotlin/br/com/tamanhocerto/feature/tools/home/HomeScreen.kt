package br.com.tamanhocerto.feature.tools.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.tamanhocerto.core.ui.component.AppScaffold
import br.com.tamanhocerto.core.ui.component.SecondaryAction
import br.com.tamanhocerto.core.ui.component.ToolCard
import br.com.tamanhocerto.feature.tools.R
import br.com.tamanhocerto.core.ui.R as UiR

/**
 * Ordem dos cartoes = frequencia esperada de uso (UI-SPEC secao 3), que nao e
 * a ordem do PRD.
 */
private val TOOLS = listOf(
    ToolId.COMPRESS to (R.string.tool_compress_title to R.string.tool_compress_sub),
    ToolId.RESIZE to (R.string.tool_resize_title to R.string.tool_resize_sub),
    ToolId.IMAGES_TO_PDF to (R.string.tool_img2pdf_title to R.string.tool_img2pdf_sub),
    ToolId.PDF_TO_IMAGES to (R.string.tool_pdf2img_title to R.string.tool_pdf2img_sub),
    ToolId.CONVERT to (R.string.tool_convert_title to R.string.tool_convert_sub),
)

@Composable
fun HomeScreen(
    onToolClick: (ToolId) -> Unit,
    onPrivacyClick: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScaffold(title = stringResource(R.string.home_title), modifier = modifier) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
        ) {
            items(TOOLS) { (id, texts) ->
                val (titleRes, subRes) = texts
                ToolCard(
                    title = stringResource(titleRes),
                    subtitle = stringResource(subRes),
                    onClick = { onToolClick(id) },
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SecondaryAction(
                        text = stringResource(UiR.string.nav_privacy),
                        onClick = onPrivacyClick,
                    )
                    SecondaryAction(
                        text = stringResource(UiR.string.nav_about),
                        onClick = onAboutClick,
                    )
                }
            }
        }
    }
}
