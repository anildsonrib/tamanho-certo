package br.com.tamanhocerto.feature.tools.configure

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.tamanhocerto.core.model.ImageFormat
import br.com.tamanhocerto.core.ui.component.PrimaryAction
import br.com.tamanhocerto.core.ui.component.SizeChip
import br.com.tamanhocerto.core.ui.theme.TamanhoCertoTheme
import br.com.tamanhocerto.feature.tools.R
import br.com.tamanhocerto.feature.tools.home.ToolId

@Composable
fun ConvertScreen(
    state: ConfigureUiState,
    form: OperationForm.Convert,
    onFormChange: (OperationForm.Convert) -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        InputSummaryBlock(state.input)

        FormatPicker(
            label = stringResource(R.string.convert_target),
            selected = form.format,
            onSelect = { onFormChange(form.copy(format = it)) },
            hints = true,
        )

        // A pergunta da cor so aparece quando a transparencia seria perdida:
        // o app pergunta em vez de decidir sozinho (PRD secao 3.3).
        if (state.input.hasAlpha && form.format == ImageFormat.JPEG) {
            Text(
                text = stringResource(R.string.convert_flatten_label),
                style = MaterialTheme.typography.titleMedium,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    OperationForm.WHITE to R.string.convert_flatten_white,
                    OperationForm.BLACK to R.string.convert_flatten_black,
                    OperationForm.GRAY to R.string.convert_flatten_gray,
                ).forEach { (color, labelRes) ->
                    SizeChip(
                        label = stringResource(labelRes),
                        selected = form.flattenColor == color,
                        onClick = { onFormChange(form.copy(flattenColor = color)) },
                    )
                }
            }
        }

        PrimaryAction(
            text = stringResource(R.string.action_continue),
            onClick = onStart,
            enabled = state.validation is Validation.Ok,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(name = "Converter — com transparencia", showBackground = true)
@Composable
private fun ConvertPreview() {
    TamanhoCertoTheme(dynamicColor = false) {
        ConvertScreen(
            state = previewState(ToolId.CONVERT).copy(
                input = previewState().input.copy(hasAlpha = true),
            ),
            form = OperationForm.Convert(),
            onFormChange = {},
            onStart = {},
        )
    }
}
