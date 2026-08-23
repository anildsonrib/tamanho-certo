package br.com.tamanhocerto.di

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ponte entre o `RewardGate` (que nao conhece Compose) e o diálogo de opt-in
 * na tela.
 *
 * Sem tela ouvindo, `ask()` devolve `true`: e mais uma porta de fail-open, e
 * nunca um caminho que bloqueie o usuario (ADS-SPEC secao 3).
 */
@Singleton
class OptInGateway @Inject constructor() {

    private val _pending = MutableStateFlow<CompletableDeferred<Boolean>?>(null)

    /** Não-nulo enquanto a tela precisa mostrar o diálogo. */
    val pending: StateFlow<CompletableDeferred<Boolean>?> = _pending.asStateFlow()

    var hasListener: Boolean = false

    suspend fun ask(): Boolean {
        if (!hasListener) return true
        val answer = CompletableDeferred<Boolean>()
        _pending.value = answer
        return try {
            answer.await()
        } finally {
            _pending.value = null
        }
    }

    fun answer(accepted: Boolean) {
        _pending.value?.complete(accepted)
    }
}
