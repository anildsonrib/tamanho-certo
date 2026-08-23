package br.com.tamanhocerto.core.ads

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * Testes do fluxo real de liberacao (`resolveUnlock`), que e o que o
 * `RealRewardGate` executa.
 *
 * A1 e A7 sao os dois criticos: se o SDK subir antes do aceite, a politica
 * de privacidade publicada passa a estar errada (ADS-SPEC secao 7).
 */
class RealRewardGateTest {

    private class FakeAd

    private class Recorder {
        var initialized = 0
        var consentAsked = 0
        var shown = 0
    }

    private suspend fun run(
        optIn: suspend () -> Boolean,
        recorder: Recorder = Recorder(),
        hasActivity: Boolean = true,
        consent: suspend () -> Unit = {},
        load: suspend () -> FakeAd? = { FakeAd() },
        show: suspend (FakeAd) -> Boolean = { true },
    ): Pair<Boolean, Recorder> {
        val result = resolveUnlock(
            askOptIn = optIn,
            initialize = { recorder.initialized++ },
            hasForegroundActivity = { hasActivity },
            requestConsent = {
                recorder.consentAsked++
                consent()
            },
            loadAd = load,
            showAd = { ad ->
                recorder.shown++
                show(ad)
            },
            loadTimeoutMs = 1_000L,
        )
        return result to recorder
    }

    /** A1 e A7 — recusa devolve false e o SDK NAO e inicializado. */
    @Test
    fun recusa_no_opt_in_nao_inicializa_o_sdk() = runTest {
        val (unlocked, recorder) = run(optIn = { false })

        assertFalse(unlocked)
        assertEquals(0, recorder.initialized)
        assertEquals(0, recorder.consentAsked)
        assertEquals(0, recorder.shown)
    }

    /** A2 — aceitou e nao ha rede: liberado. */
    @Test
    fun sem_rede_libera() = runTest {
        val (unlocked, _) = run(optIn = { true }, load = { throw IOException("offline") })
        assertTrue(unlocked)
    }

    /** A3 — o anuncio nao carrega: liberado. */
    @Test
    fun anuncio_que_nao_carrega_libera() = runTest {
        val (unlocked, recorder) = run(optIn = { true }, load = { null })

        assertTrue(unlocked)
        assertEquals(1, recorder.initialized)
        assertEquals(0, recorder.shown)
    }

    /** A4 — anuncio exibido e concluido: liberado. */
    @Test
    fun anuncio_concluido_libera() = runTest {
        val (unlocked, recorder) = run(optIn = { true }, show = { true })

        assertTrue(unlocked)
        assertEquals(1, recorder.shown)
    }

    /** A5 — o usuario fecha antes do fim: liberado do mesmo jeito. */
    @Test
    fun fechar_o_anuncio_antes_do_fim_libera() = runTest {
        val (unlocked, _) = run(optIn = { true }, show = { false })
        assertTrue(unlocked)
    }

    /** A6 — erro do UMP: liberado. */
    @Test
    fun erro_do_consentimento_libera() = runTest {
        val (unlocked, _) = run(optIn = { true }, consent = { error("falha do UMP") })
        assertTrue(unlocked)
    }

    /** A8 — nenhuma Activity em primeiro plano: liberado, sem pedir anuncio. */
    @Test
    fun sem_activity_em_primeiro_plano_libera() = runTest {
        val (unlocked, recorder) = run(optIn = { true }, hasActivity = false)

        assertTrue(unlocked)
        assertEquals(0, recorder.consentAsked)
        assertEquals(0, recorder.shown)
    }

    /** Falha do proprio diálogo tambem libera: fail-open ate no erro nosso. */
    @Test
    fun falha_do_dialogo_libera() = runTest {
        val (unlocked, _) = run(optIn = { error("falha na UI") })
        assertTrue(unlocked)
    }

    /** Carregamento que nunca responde para no tempo limite e libera. */
    @Test
    fun carregamento_travado_libera_no_tempo_limite() = runTest {
        val (unlocked, recorder) = run(
            optIn = { true },
            load = {
                kotlinx.coroutines.delay(60_000L)
                FakeAd()
            },
        )

        assertTrue(unlocked)
        assertEquals(0, recorder.shown)
    }
}
