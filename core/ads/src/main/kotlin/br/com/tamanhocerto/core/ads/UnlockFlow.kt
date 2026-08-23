package br.com.tamanhocerto.core.ads

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.cancellation.CancellationException

/**
 * O fluxo de liberacao do lote, sem nenhum tipo do Android.
 *
 * Fica separado porque e a regra que sustenta a politica de privacidade e o
 * posicionamento do produto, e precisa ser testavel sem aparelho (ADS-SPEC
 * secao 7). O `RealRewardGate` apenas liga esta funcao ao SDK.
 *
 * REGRA: `false` sai daqui numa hipotese so — a recusa explicita do usuario
 * no passo 1. Todo o resto e fail-open.
 *
 * @param askOptIn diálogo do app, ANTES de qualquer coisa do SDK.
 * @param initialize sobe o SDK; so acontece depois do aceite (D12).
 * @param hasForegroundActivity sem Activity nao da para exibir — libera.
 * @param requestConsent UMP; erro nao bloqueia.
 * @param loadAd devolve o anuncio, ou null quando nao carregou.
 * @param showAd exibe; o retorno (recompensa concedida ou nao) nao muda nada.
 */
suspend fun <A : Any> resolveUnlock(
    askOptIn: suspend () -> Boolean,
    initialize: suspend () -> Unit,
    hasForegroundActivity: () -> Boolean,
    requestConsent: suspend () -> Unit,
    loadAd: suspend () -> A?,
    showAd: suspend (A) -> Boolean,
    loadTimeoutMs: Long = LOAD_TIMEOUT_MS,
): Boolean {
    val accepted = try {
        askOptIn()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: Exception) {
        // Ate uma falha nossa na UI libera: o anuncio nunca fica entre o
        // usuario e o arquivo dele.
        true
    }
    if (!accepted) return false

    try {
        initialize()
        if (!hasForegroundActivity()) return true

        runCatching { requestConsent() }

        val ad = try {
            withTimeout(loadTimeoutMs) { loadAd() }
        } catch (timeout: TimeoutCancellationException) {
            null
        } catch (error: Exception) {
            null
        } ?: return true

        runCatching { showAd(ad) }
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: Exception) {
        return true
    }
    return true
}

/** ADS-SPEC secao 3, passo 4. */
const val LOAD_TIMEOUT_MS = 8_000L
