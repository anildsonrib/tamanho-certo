package br.com.tamanhocerto.core.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume

/** Carrega e exibe o anuncio premiado. Nenhum caminho aqui devolve `false`. */
interface RewardedAdSource {
    suspend fun load(unitId: String): RewardedAd?

    /** true quando a recompensa foi concedida. */
    suspend fun show(activity: Activity, ad: RewardedAd): Boolean
}

class RealRewardedAdSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : RewardedAdSource {

    override suspend fun load(unitId: String): RewardedAd? =
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                // AdRequest VAZIO: sem targeting, sem palavra-chave, sem
                // extras. Nada do arquivo do usuario chega perto do SDK.
                RewardedAd.load(
                    context,
                    unitId,
                    AdRequest.Builder().build(),
                    object : RewardedAdLoadCallback() {
                        override fun onAdLoaded(ad: RewardedAd) {
                            if (continuation.isActive) continuation.resume(ad)
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            if (continuation.isActive) continuation.resume(null)
                        }
                    },
                )
            }
        }

    override suspend fun show(activity: Activity, ad: RewardedAd): Boolean =
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                var earned = false
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        if (continuation.isActive) continuation.resume(earned)
                    }

                    override fun onAdFailedToShowFullScreenContent(
                        error: com.google.android.gms.ads.AdError,
                    ) {
                        if (continuation.isActive) continuation.resume(false)
                    }
                }
                ad.show(activity) { earned = true }
            }
        }
}
