package br.com.tamanhocerto.di

import br.com.tamanhocerto.BuildConfig
import br.com.tamanhocerto.core.ads.RealRewardGate
import br.com.tamanhocerto.core.ads.RealRewardedAdSource
import br.com.tamanhocerto.core.ads.RewardedAdSource
import br.com.tamanhocerto.core.model.RewardGate
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * O `:app` e o UNICO modulo que conhece `:core:ads` (ARCHITECTURE.md secao 2,
 * invariante 3). Trocar o SDK de anuncios nao toca em mais nada.
 */
@Module
@InstallIn(SingletonComponent::class)
object AdsModule {

    @Provides
    @Singleton
    fun provideRewardedAdSource(source: RealRewardedAdSource): RewardedAdSource = source

    /** O ID vem do `build.gradle.kts`, por tipo de build. */
    @Provides
    @Singleton
    fun provideUnitIdProvider(): RealRewardGate.RewardedUnitIdProvider =
        RealRewardGate.RewardedUnitIdProvider { BuildConfig.REWARDED_UNIT_ID }

    /**
     * O diálogo de opt-in e da camada de UI. O `OptInGateway` guarda a
     * pergunta pendente para a tela responder; sem tela ouvindo, o padrao e
     * aceitar — mais uma porta de fail-open.
     */
    @Provides
    @Singleton
    fun provideOptInPrompt(gateway: OptInGateway): RealRewardGate.OptInPrompt =
        RealRewardGate.OptInPrompt { gateway.ask() }

    @Provides
    @Singleton
    fun provideRewardGate(gate: RealRewardGate): RewardGate = gate
}
