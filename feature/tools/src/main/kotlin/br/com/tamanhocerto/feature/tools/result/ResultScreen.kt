package br.com.tamanhocerto.feature.tools.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.tamanhocerto.core.ui.component.BeforeAfterRow
import br.com.tamanhocerto.core.ui.component.NoticeCard
import br.com.tamanhocerto.core.ui.component.NoticeKind
import br.com.tamanhocerto.core.ui.component.PrimaryAction
import br.com.tamanhocerto.core.ui.component.SecondaryAction
import br.com.tamanhocerto.core.ui.theme.TamanhoCertoTheme
import br.com.tamanhocerto.feature.tools.R
import br.com.tamanhocerto.core.ui.R as UiR
import br.com.tamanhocerto.feature.tools.configure.ItemState
import br.com.tamanhocerto.feature.tools.configure.ResultBanner
import br.com.tamanhocerto.feature.tools.configure.ResultItem
import br.com.tamanhocerto.feature.tools.configure.ResultUiState
import br.com.tamanhocerto.feature.tools.configure.DownscalePrompt
import br.com.tamanhocerto.feature.tools.configure.toComponent

/**
 * Tela BURRA. O diálogo de alvo nao atingido e o unico modal do app, e existe
 * porque o PRD proibe reduzir resolucao sem perguntar (UI-SPEC secao 6).
 */
@Composable
fun ResultScreen(
    state: ResultUiState,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onRedo: () -> Unit,
    onHome: () -> Unit,
    onDownscaleAccept: () -> Unit,
    onDownscaleDecline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // A faixa de estado e anunciada pelo leitor de tela, e o texto sempre
        // acompanha a cor: cor nunca e o unico sinal (UI-SPEC secao 9).
        NoticeCard(
            text = state.bannerText ?: stringResource(state.banner.messageRes()),
            kind = state.banner.toKind(),
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )

        state.beforeAfterText?.let {
            BeforeAfterRow(beforeAfterText = it, reductionText = state.reductionText)
        }

        state.batchSummary?.let {
            Text(text = it, style = MaterialTheme.typography.bodyMedium)
        }

        state.detailLines.forEach { line ->
            Text(text = line, style = MaterialTheme.typography.bodySmall)
        }

        state.notices.forEach { notice ->
            NoticeCard(text = stringResource(notice.message), kind = notice.kind.toComponent())
        }

        if (state.items.size > 1) {
            state.items.forEach { item -> ResultItemRow(item) }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryAction(text = stringResource(R.string.action_save), onClick = onSave)
            SecondaryAction(text = stringResource(R.string.action_share), onClick = onShare)
        }
        SecondaryAction(text = stringResource(R.string.action_redo), onClick = onRedo)
        SecondaryAction(text = stringResource(UiR.string.nav_home), onClick = onHome)
    }

    state.downscalePrompt?.let { prompt ->
        DownscaleDialog(prompt, onDownscaleAccept, onDownscaleDecline)
    }
}

@Composable
private fun ResultItemRow(item: ResultItem) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = item.name, style = MaterialTheme.typography.bodyMedium)
        if (item.errorMessage != null) {
            Text(
                text = stringResource(item.errorMessage),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        } else {
            Text(text = item.beforeAfterText, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DownscaleDialog(
    prompt: DownscalePrompt,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDecline,
        title = { Text(stringResource(R.string.downscale_title)) },
        text = { Text(stringResource(R.string.downscale_body, prompt.targetText)) },
        confirmButton = {
            TextButton(onClick = onAccept) { Text(stringResource(R.string.downscale_yes)) }
        },
        dismissButton = {
            TextButton(onClick = onDecline) { Text(stringResource(R.string.downscale_no)) }
        },
    )
}

/** Texto de reserva quando o ViewModel nao formatou nada. */
private fun ResultBanner.messageRes(): Int = when (this) {
    ResultBanner.SUCCESS -> R.string.result_success
    ResultBanner.WARNING -> R.string.notice_no_upscale
    ResultBanner.ERROR -> R.string.error_unknown
}

private fun ResultBanner.toKind(): NoticeKind = when (this) {
    ResultBanner.SUCCESS -> NoticeKind.INFO
    ResultBanner.WARNING -> NoticeKind.WARNING
    ResultBanner.ERROR -> NoticeKind.ERROR
}

@Preview(name = "Resultado — sucesso", showBackground = true)
@Composable
private fun ResultSuccessPreview() {
    TamanhoCertoTheme(dynamicColor = false) {
        ResultScreen(
            state = ResultUiState(
                items = listOf(
                    ResultItem("foto-menor.jpg", "3,8 MB → 480 KB", ItemState.SUCCESS),
                ),
                beforeAfterText = "3,8 MB → 480 KB",
                reductionText = "87% menor",
            ),
            onSave = {}, onShare = {}, onRedo = {}, onHome = {},
            onDownscaleAccept = {}, onDownscaleDecline = {},
        )
    }
}

@Preview(name = "Resultado — alvo nao atingido", showBackground = true)
@Composable
private fun ResultMissedPreview() {
    TamanhoCertoTheme(dynamicColor = false) {
        ResultScreen(
            state = ResultUiState(
                items = listOf(
                    ResultItem("foto-menor.jpg", "3,8 MB → 620 KB", ItemState.WARNING),
                ),
                beforeAfterText = "3,8 MB → 620 KB",
                banner = ResultBanner.WARNING,
                downscalePrompt = DownscalePrompt("500 KB"),
            ),
            onSave = {}, onShare = {}, onRedo = {}, onHome = {},
            onDownscaleAccept = {}, onDownscaleDecline = {},
        )
    }
}

@Preview(name = "Resultado — lote com falha, escuro", showBackground = true, fontScale = 1.3f)
@Composable
private fun ResultBatchPreview() {
    TamanhoCertoTheme(darkTheme = true, dynamicColor = false) {
        ResultScreen(
            state = ResultUiState(
                items = listOf(
                    ResultItem("a-menor.jpg", "2 MB → 300 KB", ItemState.SUCCESS),
                    ResultItem(
                        name = "b.pdf",
                        beforeAfterText = "",
                        state = ItemState.FAILED,
                        errorMessage = R.string.error_pdf_password,
                    ),
                ),
                batchSummary = "1 de 2 arquivos concluídos",
                banner = ResultBanner.WARNING,
            ),
            onSave = {}, onShare = {}, onRedo = {}, onHome = {},
            onDownscaleAccept = {}, onDownscaleDecline = {},
        )
    }
}
