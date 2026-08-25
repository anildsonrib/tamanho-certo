package br.com.tamanhocerto.feature.tools.configure

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.tamanhocerto.core.model.ImageFormat
import br.com.tamanhocerto.core.ui.component.NoticeCard
import br.com.tamanhocerto.core.ui.component.NoticeKind
import br.com.tamanhocerto.core.ui.component.PrimaryAction
import br.com.tamanhocerto.core.ui.component.SizeChip
import br.com.tamanhocerto.core.ui.theme.TamanhoCertoTheme
import br.com.tamanhocerto.feature.tools.R

/**
 * Tela BURRA: recebe estado e lambdas, nao conhece ViewModel e nao formata
 * nada. Trocar o visual mexe aqui; a ligacao fica no `ConfigureRoute`
 * (UI-SPEC secao 10b.1).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompressScreen(
    state: ConfigureUiState,
    form: OperationForm.Compress,
    onFormChange: (OperationForm.Compress) -> Unit,
    onSwitchToJpeg: () -> Unit,
    onStart: () -> Unit,
    onPickFiles: () -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Entra direto no layout, sem tela intermediaria e sem o seletor do
        // sistema abrir sozinho (pedido do responsavel em 2026-08-25). Com a
        // area de arquivos vazia, o resumo mostra "Nenhum arquivo
        // selecionado" (InputSummaryBlock) e o botao de baixo vira
        // "Selecionar arquivos" (ToolActionBar).
        InputSummaryBlock(state.input)

        if (!form.qualityMode) {
            Text(
                text = stringResource(R.string.compress_target_label),
                style = MaterialTheme.typography.titleMedium,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SizeShortcuts.values.forEach { bytes ->
                    SizeChip(
                        label = shortcutLabel(bytes),
                        selected = form.targetBytes == bytes && form.customValue.isEmpty(),
                        onClick = { onFormChange(form.copy(targetBytes = bytes, customValue = "")) },
                    )
                }
            }
            Text(
                text = stringResource(R.string.compress_target_hint),
                style = MaterialTheme.typography.bodySmall,
            )

            OutlinedTextField(
                value = form.customValue,
                onValueChange = { typed ->
                    // Preencher o campo desmarca o chip, e vice-versa.
                    onFormChange(
                        form.copy(
                            customValue = typed,
                            targetBytes = typed.toLongOrNull()?.let {
                                it * if (form.customUnitIsMb) MB else KB
                            },
                        ),
                    )
                },
                label = { Text(stringResource(R.string.compress_custom_label)) },
                suffix = {
                    Text(
                        if (form.customUnitIsMb) {
                            stringResource(R.string.compress_unit_mb)
                        } else {
                            stringResource(R.string.compress_unit_kb)
                        },
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        FormatPicker(
            label = stringResource(R.string.compress_format_label),
            selected = form.format,
            onSelect = { onFormChange(form.copy(format = it)) },
        )

        state.notice?.let { notice ->
            NoticeCard(text = stringResource(notice.message), kind = notice.kind.toComponent())
            notice.actionLabel?.let { label ->
                PrimaryAction(text = stringResource(label), onClick = onSwitchToJpeg)
            }
        }

        AdvancedBlock(form = form, onFormChange = onFormChange)

        (state.validation as? Validation.Blocked)?.let { blocked ->
            NoticeCard(text = blockedText(blocked), kind = NoticeKind.ERROR)
        }

        ToolActionBar(
            hasFiles = state.input.fileCount >= 1,
            actionLabel = stringResource(state.tool.actionRes()),
            actionEnabled = state.validation is Validation.Ok,
            onPickFiles = onPickFiles,
            onStart = onStart,
            onClearAll = onClearAll,
        )
    }
}

@Composable
private fun AdvancedBlock(
    form: OperationForm.Compress,
    onFormChange: (OperationForm.Compress) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.action_advanced),
            style = MaterialTheme.typography.titleSmall,
        )

        SwitchRow(
            label = stringResource(R.string.compress_quality_mode),
            checked = form.qualityMode,
            onCheckedChange = { onFormChange(form.copy(qualityMode = it)) },
        )

        if (form.qualityMode) {
            Text(stringResource(R.string.compress_quality_label))
            Slider(
                value = form.quality.toFloat(),
                onValueChange = { onFormChange(form.copy(quality = it.toInt())) },
                valueRange = QUALITY_RANGE_MIN..QUALITY_RANGE_MAX,
            )
        }

        SwitchRow(
            label = stringResource(R.string.compress_keep_metadata),
            checked = form.keepMetadata,
            onCheckedChange = { onFormChange(form.copy(keepMetadata = it)) },
        )
        if (!form.keepMetadata) {
            Text(
                text = stringResource(R.string.compress_keep_metadata_off),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
internal fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/** Deslizante de qualidade da UI-SPEC secao 4.1. */
private const val QUALITY_RANGE_MIN = 30f
private const val QUALITY_RANGE_MAX = 95f
private const val KB = 1024L
private const val MB = 1024L * 1024L

@Composable
private fun shortcutLabel(bytes: Long): String =
    if (bytes >= MB) {
        "${bytes / MB} " + stringResource(R.string.compress_unit_mb)
    } else {
        "${bytes / KB} " + stringResource(R.string.compress_unit_kb)
    }

@Preview(name = "Comprimir — claro", showBackground = true)
@Composable
private fun CompressPreview() {
    TamanhoCertoTheme(dynamicColor = false) {
        CompressScreen(
            state = previewState(),
            form = OperationForm.Compress(targetBytes = 500 * KB),
            onFormChange = {},
            onSwitchToJpeg = {},
            onPickFiles = {},
            onClearAll = {},
            onStart = {},
        )
    }
}

@Preview(name = "Comprimir — PNG com alvo, escuro", showBackground = true)
@Composable
private fun CompressPngNoticePreview() {
    TamanhoCertoTheme(darkTheme = true, dynamicColor = false) {
        CompressScreen(
            state = previewState().copy(
                notice = NoticeState(
                    message = R.string.notice_png_lossless,
                    kind = NoticeKindUi.WARNING,
                    actionLabel = R.string.notice_png_switch,
                ),
            ),
            form = OperationForm.Compress(targetBytes = 100 * KB, format = ImageFormat.PNG),
            onFormChange = {},
            onSwitchToJpeg = {},
            onPickFiles = {},
            onClearAll = {},
            onStart = {},
        )
    }
}

@Preview(name = "Comprimir — bloqueado", showBackground = true, fontScale = 1.5f)
@Composable
private fun CompressBlockedPreview() {
    TamanhoCertoTheme(dynamicColor = false) {
        CompressScreen(
            state = previewState().copy(
                validation = Validation.Blocked(R.string.invalid_target_zero),
            ),
            form = OperationForm.Compress(),
            onFormChange = {},
            onSwitchToJpeg = {},
            onPickFiles = {},
            onClearAll = {},
            onStart = {},
        )
    }
}
