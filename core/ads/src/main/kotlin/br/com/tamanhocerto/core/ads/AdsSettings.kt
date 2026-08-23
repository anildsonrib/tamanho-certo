package br.com.tamanhocerto.core.ads

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reabrir o formulario de consentimento, linkado na tela Sobre
 * (`ads_consent_settings` em STRINGS.md).
 */
@Singleton
class AdsSettings @Inject constructor(
    private val consent: ConsentManager,
    private val activityHolder: CurrentActivityHolder,
) {
    suspend fun openConsentForm() {
        val activity = activityHolder.activity ?: return
        consent.showPrivacyOptions(activity)
    }
}
