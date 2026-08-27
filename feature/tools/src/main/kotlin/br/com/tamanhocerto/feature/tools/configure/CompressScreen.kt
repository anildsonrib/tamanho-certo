package br.com.tamanhocerto.feature.tools.configure

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import br.com.tamanhocerto.core.ui.component.ActionIconCaret
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
    onStart: () -> Unit,
    onPickFiles: () -> Unit,
    onAddFiles: () -> Unit,
    onClearAll: () -> Unit,
    onRemoveFile: (index: Int) -> Unit,
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
        // selecionado" e o botao de baixo vira "Selecionar arquivos".
        InputSummaryBlock(state.input, state.tool.accent())

        if (!form.qualityMode) {
            Text(
                text = stringResource(R.string.compress_target_label),
                style = MaterialTheme.typography.titleMedium,
            )
            ChipGrid(SizeShortcuts.values) { bytes, chipModifier ->
                SizeChip(
                    label = shortcutLabel(bytes),
                    selected = form.targetBytes == bytes && form.customValue.isEmpty(),
                    onClick = { onFormChange(form.copy(targetBytes = bytes, customValue = "")) },
                    modifier = chipModifier,
                )
            }
        }

        // Sem seletor de formato desde 2026-08-27 (pedido do responsavel): a
        // saida mantem a extensao original do arquivo, e quem quiser trocar
        // vai para "Converter formato". O `format` do formulario continua
        // existindo, mas passa a ser preenchido pelo ViewModel a partir do
        // arquivo escolhido — nao pela tela.

        // Aviso do PNG: informa que o alvo sera atingido reduzindo as
        // dimensoes, sem oferecer troca de formato (2026-08-27).
        state.notice?.let { notice ->
            NoticeCard(text = stringResource(notice.message), kind = notice.kind.toComponent())
        }

        AdvancedSection(form = form, onFormChange = onFormChange)

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
            onAddFiles = onAddFiles,
            // Paleta interna = cor do icone da ferramenta na `home`
            // (pedido do responsavel em 2026-08-26).
            containerColor = state.tool.accent().color,
        )

        // Miniaturas dos arquivos escolhidos, na area vazia abaixo do botao
        // — o mesmo bloco de "Converter formato" (pedido do responsavel em
        // 2026-08-27).
        SelectedFilesGrid(items = state.input.items, onRemove = onRemoveFile)
    }
}

@Composable
private fun AdvancedSection(
    form: OperationForm.Compress,
    onFormChange: (OperationForm.Compress) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider()

        // Cabecalho que abre e fecha. Nasce fechada: e isso que faz a tela
        // encolher de verdade (pedido do responsavel em 2026-08-27). O campo
        // de valor livre desceu para ca no mesmo pedido — e o caminho de quem
        // sabe o numero exato, nao o caminho comum.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onFormChange(form.copy(advancedExpanded = !form.advancedExpanded)) }
                .heightIn(min = 48.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.action_advanced),
                style = MaterialTheme.typography.titleSmall,
            )
            val rotation by animateFloatAsState(
                targetValue = if (form.advancedExpanded) 180f else 0f,
                label = "advancedCaret",
            )
            Icon(
                imageVector = ActionIconCaret,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp).graphicsLayer { rotationZ = rotation },
            )
        }

        AnimatedVisibility(visible = form.advancedExpanded) {
            Column(
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (!form.qualityMode) {
                    CustomTargetField(form = form, onFormChange = onFormChange)
                }

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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomTargetField(
    form: OperationForm.Compress,
    onFormChange: (OperationForm.Compress) -> Unit,
) {
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
            onPickFiles = {},
            onClearAll = {},
            onAddFiles = {},
            onRemoveFile = {},
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
                ),
            ),
            form = OperationForm.Compress(targetBytes = 100 * KB, format = ImageFormat.PNG),
            onFormChange = {},
            onPickFiles = {},
            onClearAll = {},
            onAddFiles = {},
            onRemoveFile = {},
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
            onPickFiles = {},
            onClearAll = {},
            onAddFiles = {},
            onRemoveFile = {},
            onStart = {},
        )
    }
}
