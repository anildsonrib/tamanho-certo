package br.com.tamanhocerto.core.ads

import br.com.tamanhocerto.core.model.RewardGate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pergunta que a UI faz antes de liberar o lote.
 *
 * REGRA UNICA: *fail-open*. `false` sai daqui numa hipotese so — o usuario
 * tocou em "Agora nao" no diálogo de opt-in. Sem rede, anuncio que nao carrega,
 * SDK com erro, anuncio fechado no meio, nenhuma Activity em primeiro plano:
 * tudo devolve `true` (ADS-SPEC secao 3).
 */
@Singleton
class RealRewardGate @Inject constructor(
    private val optIn: OptInPrompt,
    private val initializer: AdsInitializer,
    private val consent: ConsentManager,
    private val adSource: RewardedAdSource,
    private val activityHolder: CurrentActivityHolder,
    private val unitIdProvider: RewardedUnitIdProvider,
) : RewardGate {

    /** Diálogo de opt-in do app, exibido pela camada de UI. */
    fun interface OptInPrompt {
        /** false = o usuario recusou. E o unico `false` do fluxo inteiro. */
        suspend fun ask(): Boolean
    }

    fun interface RewardedUnitIdProvider {
        fun unitId(): String
    }

    override suspend fun requestUnlock(): Boolean = resolveUnlock(
        askOptIn = optIn::ask,
        initialize = initializer::ensureInitialized,
        hasForegroundActivity = { activityHolder.activity != null },
        requestConsent = { activityHolder.activity?.let { consent.request(it) } },
        loadAd = { adSource.load(unitIdProvider.unitId()) },
        showAd = { ad -> activityHolder.activity?.let { adSource.show(it, ad) } ?: true },
    )
}
