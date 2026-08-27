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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.tamanhocerto.core.ui.component.NoticeCard
import br.com.tamanhocerto.core.ui.component.NoticeKind
import br.com.tamanhocerto.core.ui.component.SizeChip
import br.com.tamanhocerto.core.ui.theme.TamanhoCertoTheme
import br.com.tamanhocerto.feature.tools.R
import br.com.tamanhocerto.feature.tools.home.ToolId

/** Atalhos de maior lado da UI-SPEC secao 4.2. */
private val LONGEST_SIDE_SHORTCUTS = listOf(640, 1024, 1920, 2560)
private const val PERCENT_MIN = 10f
private const val PERCENT_MAX = 100f
private const val PERCENT_STEPS = 17

@Composable
fun ResizeScreen(
    state: ConfigureUiState,
    form: OperationForm.Resize,
    onFormChange: (OperationForm.Resize) -> Unit,
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
        // sistema abrir sozinho (pedido do responsavel em 2026-08-25).
        InputSummaryBlock(state.input)

        TabRow(selectedTabIndex = form.mode.ordinal) {
            ResizeMode.entries.forEach { mode ->
                Tab(
                    selected = mode == form.mode,
                    onClick = { onFormChange(form.copy(mode = mode)) },
                    text = { Text(stringResource(mode.labelRes())) },
                )
            }
        }

        when (form.mode) {
            ResizeMode.PIXELS -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = form.width,
                        onValueChange = { onFormChange(form.copy(width = it)) },
                        label = { Text(stringResource(R.string.resize_width)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = form.height,
                        onValueChange = { onFormChange(form.copy(height = it)) },
                        label = { Text(stringResource(R.string.resize_height)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                }
                SwitchRow(
                    label = stringResource(R.string.resize_lock),
                    checked = form.lockAspect,
                    onCheckedChange = { onFormChange(form.copy(lockAspect = it)) },
                )
            }

            ResizeMode.PERCENT -> {
                Text("${form.percent}%")
                Slider(
                    value = form.percent.toFloat(),
                    onValueChange = { onFormChange(form.copy(percent = it.toInt())) },
                    valueRange = PERCENT_MIN..PERCENT_MAX,
                    steps = PERCENT_STEPS,
                )
            }

            ResizeMode.LONGEST_SIDE -> {
                OutlinedTextField(
                    value = form.longestSide,
                    onValueChange = { onFormChange(form.copy(longestSide = it)) },
                    label = { Text(stringResource(R.string.resize_tab_longest)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LONGEST_SIDE_SHORTCUTS.forEach { pixels ->
                        SizeChip(
                            label = pixels.toString(),
                            selected = form.longestSide == pixels.toString(),
                            onClick = { onFormChange(form.copy(longestSide = pixels.toString())) },
                        )
                    }
                }
            }
        }

        form.resultText?.let {
            Text(text = it, style = MaterialTheme.typography.bodyMedium)
        }

        if (form.showsNoUpscaleWarning) {
            NoticeCard(
                text = stringResource(R.string.notice_no_upscale),
                kind = NoticeKind.WARNING,
            )
        }

        FormatPicker(
            label = stringResource(R.string.compress_format_label),
            selected = form.format,
            onSelect = { onFormChange(form.copy(format = it)) },
        )

        ToolActionBar(
            hasFiles = state.input.fileCount >= 1,
            actionLabel = stringResource(state.tool.actionRes()),
            actionEnabled = state.validation is Validation.Ok,
            onPickFiles = onPickFiles,
            onStart = onStart,
            onClearAll = onClearAll,
            // Paleta interna = cor do icone da ferramenta na `home`
            // (pedido do responsavel em 2026-08-26).
            containerColor = state.tool.accent().color,
        )
    }
}

private fun ResizeMode.labelRes(): Int = when (this) {
    ResizeMode.PIXELS -> R.string.resize_tab_pixels
    ResizeMode.PERCENT -> R.string.resize_tab_percent
    ResizeMode.LONGEST_SIDE -> R.string.resize_tab_longest
}

@Preview(name = "Redimensionar — pixels", showBackground = true)
@Composable
private fun ResizePreview() {
    TamanhoCertoTheme(dynamicColor = false) {
        ResizeScreen(
            state = previewState(ToolId.RESIZE),
            form = OperationForm.Resize(width = "1920", height = "1080"),
            onFormChange = {},
            onStart = {},
            onPickFiles = {},
            onClearAll = {},
        )
    }
}

@Preview(name = "Redimensionar — nao amplia, escuro", showBackground = true)
@Composable
private fun ResizeNoUpscalePreview() {
    TamanhoCertoTheme(darkTheme = true, dynamicColor = false) {
        ResizeScreen(
            state = previewState(ToolId.RESIZE),
            form = OperationForm.Resize(
                mode = ResizeMode.LONGEST_SIDE,
                longestSide = "8000",
                showsNoUpscaleWarning = true,
            ),
            onFormChange = {},
            onStart = {},
            onPickFiles = {},
            onClearAll = {},
        )
    }
}
