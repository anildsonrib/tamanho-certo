package br.com.tamanhocerto.feature.tools.configure

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.tamanhocerto.core.model.ImageFormat
import br.com.tamanhocerto.core.ui.component.NoticeKind
import br.com.tamanhocerto.core.ui.component.PrimaryAction
import br.com.tamanhocerto.core.ui.component.SizeChip
import br.com.tamanhocerto.feature.tools.R
import br.com.tamanhocerto.feature.tools.home.ToolId

/** Blocos comuns as cinco telas de configuracao. */
@Composable
fun InputSummaryBlock(input: InputSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        input.displayName?.let { Text(text = it, style = MaterialTheme.typography.titleSmall) }
        input.dimensionsText?.let {
            Text(text = it, style = MaterialTheme.typography.bodySmall)
        }
        input.sizeText?.let { Text(text = it, style = MaterialTheme.typography.bodySmall) }
        input.pagesText?.let { Text(text = it, style = MaterialTheme.typography.bodySmall) }
    }
}

/**
 * Estado sem arquivo selecionado ainda: as cinco ferramentas entram direto no
 * proprio layout (pedido do responsavel em 2026-08-25, revertendo o gesto
 * unico da UI-SPEC secao 3 para todas) — este bloco convida a escolher em vez
 * de mostrar controles vazios.
 */
@Composable
fun EmptySelectionBlock(subtitleRes: Int, onPickFiles: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.input_empty_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(subtitleRes),
            style = MaterialTheme.typography.bodySmall,
        )
        PrimaryAction(
            text = stringResource(R.string.action_select_files),
            onClick = onPickFiles,
            modifier = Modifier.fillMaxWidth(),
        )
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
