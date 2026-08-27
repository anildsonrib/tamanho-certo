package br.com.tamanhocerto.feature.tools.configure

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.tamanhocerto.core.model.ImageFormat
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import br.com.tamanhocerto.core.ui.component.ActionIconConvertArrows
import br.com.tamanhocerto.core.ui.component.ActionIconFolder
import br.com.tamanhocerto.core.ui.component.ActionIconFolderSize
import br.com.tamanhocerto.core.ui.component.ConvertArrowsHeight
import br.com.tamanhocerto.core.ui.component.ConvertArrowsWidth
import br.com.tamanhocerto.core.ui.component.NoticeKind
import br.com.tamanhocerto.core.ui.component.PrimaryAction
import br.com.tamanhocerto.core.ui.component.SecondaryAction
import br.com.tamanhocerto.core.ui.component.SizeChip
import br.com.tamanhocerto.feature.tools.R
import br.com.tamanhocerto.feature.tools.home.ToolId

/** Blocos comuns as cinco telas de configuracao. */
@Composable
fun InputSummaryBlock(input: InputSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Area de arquivos vazia: a tela inteira ja entra vazia (pedido do
        // responsavel em 2026-08-25), sem tela intermediaria — so o texto
        // muda aqui, mesmo lugar onde o nome do arquivo apareceria.
        val title = input.displayName ?: input.multiCountText
            ?: stringResource(R.string.input_empty_title).takeIf { input.fileCount < 1 }
        title?.let { Text(text = it, style = MaterialTheme.typography.titleSmall) }
        input.dimensionsText?.let {
            Text(text = it, style = MaterialTheme.typography.bodySmall)
        }
        val subtitle = input.multiCountText?.let { input.multiSizeText } ?: input.sizeText
        subtitle?.let { Text(text = it, style = MaterialTheme.typography.bodySmall) }
        input.pagesText?.let { Text(text = it, style = MaterialTheme.typography.bodySmall) }
    }
}

/**
 * Botao de acao das cinco telas de configuracao: com a area de arquivos
 * vazia mostra "Selecionar arquivos" e abre o seletor; com pelo menos um
 * arquivo, mostra o verbo da propria ferramenta e executa a operacao. Um
 * segundo botao "Limpar" (com confirmacao) so aparece com arquivo
 * selecionado (pedido do responsavel em 2026-08-25).
 *
 * Os dois icones do botao valem para as cinco ferramentas, como no
 * `actionBar()` do mockup (`docs/mockups/index.html`), que e um bloco so,
 * compartilhado por todas as telas: pasta sem arquivo selecionado, setas
 * opostas com arquivo. Antes de 2026-08-27 so "Converter formato" tinha
 * a pasta, e as setas nao existiam no Kotlin.
 */
@Composable
fun ToolActionBar(
    hasFiles: Boolean,
    actionLabel: String,
    actionEnabled: Boolean,
    onPickFiles: () -> Unit,
    onStart: () -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        PrimaryAction(
            text = if (hasFiles) actionLabel else stringResource(R.string.action_select_files),
            onClick = if (hasFiles) onStart else onPickFiles,
            enabled = !hasFiles || actionEnabled,
            icon = if (hasFiles) ActionIconConvertArrows else ActionIconFolder,
            iconSize = if (hasFiles) {
                DpSize(ConvertArrowsWidth, ConvertArrowsHeight)
            } else {
                DpSize(ActionIconFolderSize, ActionIconFolderSize)
            },
            containerColor = containerColor,
            modifier = Modifier.fillMaxWidth(),
        )

        if (hasFiles) {
            var confirming by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                SecondaryAction(
                    text = stringResource(R.string.action_clear),
                    onClick = { confirming = true },
                )
            }
            if (confirming) {
                AlertDialog(
                    onDismissRequest = { confirming = false },
                    title = { Text(stringResource(R.string.clear_confirm_title)) },
                    text = { Text(stringResource(R.string.clear_confirm_body)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                confirming = false
                                onClearAll()
                            },
                        ) {
                            Text(stringResource(R.string.clear_confirm_yes))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { confirming = false }) {
                            Text(stringResource(R.string.action_cancel))
                        }
                    },
                )
            }
        }
    }
}

@Composable
fun FormatPicker(
    label: String,
    selected: ImageFormat,
    onSelect: (ImageFormat) -> Unit,
    hints: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ImageFormat.entries.forEach { format ->
                SizeChip(
                    label = format.name,
                    selected = format == selected,
                    onClick = { onSelect(format) },
                )
            }
        }
        if (hints) {
            Text(
                text = stringResource(hintFor(selected)),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

internal fun hintFor(format: ImageFormat): Int = when (format) {
    ImageFormat.JPEG -> R.string.convert_jpeg_hint
    ImageFormat.PNG -> R.string.convert_png_hint
    ImageFormat.WEBP -> R.string.convert_webp_hint
}

/** Bloqueio de formulario ja resolvido em texto, com o argumento quando houver. */
@Composable
fun blockedText(blocked: Validation.Blocked): String =
    when (val arg = blocked.formatArg) {
        null -> stringResource(blocked.reason)
        else -> stringResource(blocked.reason, arg)
    }

fun NoticeKindUi.toComponent(): NoticeKind = when (this) {
    NoticeKindUi.INFO -> NoticeKind.INFO
    NoticeKindUi.WARNING -> NoticeKind.WARNING
    NoticeKindUi.ERROR -> NoticeKind.ERROR
}

/** Estado de exemplo dos `@Preview`. */
internal fun previewState(tool: ToolId = ToolId.COMPRESS) = ConfigureUiState(
    tool = tool,
    input = InputSummary(
        fileCount = 1,
        displayName = "foto.jpg",
        dimensionsText = "4032 × 3024 pixels",
        sizeText = "Tamanho atual: 3,8 MB",
        sizeBytes = 3_800_000,
    ),
    form = OperationForm.forTool(tool),
)
