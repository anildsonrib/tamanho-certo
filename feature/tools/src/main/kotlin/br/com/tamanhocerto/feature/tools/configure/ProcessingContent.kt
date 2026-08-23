package br.com.tamanhocerto.feature.tools.configure

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.tamanhocerto.core.ui.component.SecondaryAction
import br.com.tamanhocerto.core.ui.theme.TamanhoCertoTheme
import br.com.tamanhocerto.feature.tools.R

/**
 * Estado de processamento: substitui o conteudo da tela de configuracao e nao
 * e destino de navegacao (UI-SPEC secao 5).
 */
@Composable
fun ProcessingContent(
    phase: Phase.Running,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirming by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.processing_title),
            style = MaterialTheme.typography.titleMedium,
        )

        if (phase.total > 1) {
            Text(
                text = stringResource(R.string.processing_item, phase.index + 1, phase.total),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        phase.currentName?.let { Text(text = it, style = MaterialTheme.typography.bodySmall) }

        // Sem `percent` o indicador e indeterminado: nunca inventar numero.
        if (phase.percent == null) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else {
            LinearProgressIndicator(
                progress = { phase.percent / PERCENT_FULL },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        SecondaryAction(
            text = stringResource(R.string.action_cancel),
            // Em item unico cancela direto; com item concluido, confirma antes.
            onClick = { if (phase.anyItemDone) confirming = true else onCancel() },
        )
    }

    if (confirming) {
        AlertDialog(
            onDismissRequest = { confirming = false },
            title = { Text(stringResource(R.string.processing_cancel_confirm_title)) },
            text = { Text(stringResource(R.string.processing_cancel_confirm_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirming = false
                        onCancel()
                    },
                ) {
                    Text(stringResource(R.string.processing_cancel_confirm_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirming = false }) {
                    Text(stringResource(R.string.processing_cancel_confirm_no))
                }
            },
        )
    }
}

private const val PERCENT_FULL = 100f

@Preview(name = "Processando — indeterminado", showBackground = true)
@Composable
private fun ProcessingIndeterminatePreview() {
    TamanhoCertoTheme(dynamicColor = false) {
        ProcessingContent(phase = Phase.Running(percent = null), onCancel = {})
    }
}

@Preview(name = "Processando — lote", showBackground = true)
@Composable
private fun ProcessingBatchPreview() {
    TamanhoCertoTheme(darkTheme = true, dynamicColor = false) {
        ProcessingContent(
            phase = Phase.Running(percent = 40, index = 2, total = 12, anyItemDone = true),
            onCancel = {},
        )
    }
}
