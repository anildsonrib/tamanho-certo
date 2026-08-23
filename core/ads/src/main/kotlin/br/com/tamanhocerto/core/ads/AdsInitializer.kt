package br.com.tamanhocerto.core.ads

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Inicializacao PREGUICOSA do SDK (decisao D12).
 *
 * `MobileAds.initialize` e o momento em que o identificador de publicidade e o
 * IP comecam a ser transmitidos. Chamar isso na abertura do app faria o
 * usuario que nunca usa o lote transmitir dado sem necessidade — e a politica
 * publicada afirma o contrario.
 */
@Singleton
class AdsInitializer @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val started = AtomicBoolean(false)

    /** Visivel para o teste A7: nenhum caminho anterior ao aceite pode ligar isto. */
    val isInitialized: Boolean get() = started.get()

    // O lint acusa INTERNET faltando porque analisa este modulo isolado. A
    // permissao existe no artefato: ela vem do manifesto do proprio SDK, e
    // esta documentada em docs/DATA-SAFETY.md secao 5b. NAO a declaramos, para
    // manter a decisao D4 (o app nao pede permissao nenhuma).
    @SuppressLint("MissingPermission")
    suspend fun ensureInitialized() {
        if (!started.compareAndSet(false, true)) return
        withContext(Dispatchers.IO) {
            runCatching { MobileAds.initialize(context) }
        }
    }
}
