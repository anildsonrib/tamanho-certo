package br.com.tamanhocerto.di

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tamanhocerto.core.ui.R as UiR

/**
 * Diálogo de opt-in do lote. Aparece ANTES de o SDK de anúncios subir, e diz
 * o que o anúncio libera (ADS-SPEC secao 3, passo 1).
 *
 * Recusar aqui e o unico `false` do fluxo inteiro — e ela nao gera erro: o
 * usuario volta para a configuracao e o caminho de um arquivo por vez segue
 * livre.
 */
@Composable
fun OptInDialog(gateway: OptInGateway) {
    DisposableEffect(gateway) {
        gateway.hasListener = true
        onDispose { gateway.hasListener = false }
    }

    val pending by gateway.pending.collectAsStateWithLifecycle()
    if (pending == null) return

    AlertDialog(
        onDismissRequest = { gateway.answer(false) },
        title = { Text(stringResource(UiR.string.batch_locked_title)) },
        text = { Text(stringResource(UiR.string.batch_locked_body)) },
        confirmButton = {
            TextButton(onClick = { gateway.answer(true) }) {
                Text(stringResource(UiR.string.batch_watch))
            }
        },
        dismissButton = {
            TextButton(onClick = { gateway.answer(false) }) {
                Text(stringResource(UiR.string.batch_not_now))
            }
        },
    )
}
