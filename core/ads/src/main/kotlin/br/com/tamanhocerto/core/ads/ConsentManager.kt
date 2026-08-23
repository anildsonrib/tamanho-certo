package br.com.tamanhocerto.core.ads

import android.app.Activity
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Consentimento (UMP), pedido antes do primeiro anuncio e dentro do mesmo
 * fluxo. Base legal art. 7o I da LGPD (D10).
 *
 * Erro do formulario NAO bloqueia o usuario: devolve e segue (fail-open).
 */
@Singleton
class ConsentManager @Inject constructor() {

    suspend fun request(activity: Activity) = withContext(Dispatchers.Main) {
        runCatching {
            val info = UserMessagingPlatform.getConsentInformation(activity)
            requestUpdate(activity, info)
            showFormIfRequired(activity)
        }
        Unit
    }

    /** Reabrir o formulario a pedido do usuario, pela tela Sobre. */
    suspend fun showPrivacyOptions(activity: Activity) = withContext(Dispatchers.Main) {
        runCatching {
            suspendCancellableCoroutine { continuation ->
                UserMessagingPlatform.showPrivacyOptionsForm(activity) {
                    if (continuation.isActive) continuation.resume(Unit)
                }
            }
        }
        Unit
    }

    private suspend fun requestUpdate(activity: Activity, info: ConsentInformation) =
        suspendCancellableCoroutine { continuation ->
            info.requestConsentInfoUpdate(
                activity,
                ConsentRequestParameters.Builder().build(),
                { if (continuation.isActive) continuation.resume(Unit) },
                { if (continuation.isActive) continuation.resume(Unit) },
            )
        }

    private suspend fun showFormIfRequired(activity: Activity) =
        suspendCancellableCoroutine { continuation ->
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                if (continuation.isActive) continuation.resume(Unit)
            }
        }
}
