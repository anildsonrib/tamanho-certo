package br.com.tamanhocerto.imaging

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class TargetSolverTest {

    /** T1 — o original ja cabe: nenhuma codificacao e feita. */
    @Test
    fun `original menor que o alvo devolve AlreadySmaller sem chamar o probe`() = runTest {
        val probe = FakeEncodeProbe(fullSizeAtMaxQuality = 1_000_000)

        val result = solveTargetSize(
            targetBytes = 500_000,
            sourceBytes = 300_000,
            reencodeRequired = false,
            probe = probe,
            allowDownscale = false,
        )

        assertEquals(TargetSolution.AlreadySmaller(300_000), result)
        assertEquals(0, probe.calls)
    }

    /** O passo 0 nao vale quando o formato de saida difere do de origem. */
    @Test
    fun `com reencode obrigatorio o passo 0 nao se aplica`() = runTest {
        val probe = FakeEncodeProbe(fullSizeAtMaxQuality = 1_000_000)

        val result = solveTargetSize(
            targetBytes = 500_000,
            sourceBytes = 300_000,
            reencodeRequired = true,
            probe = probe,
            allowDownscale = false,
        )

        assertTrue(result is TargetSolution.Hit)
        assertTrue(probe.calls > 0)
    }

    /** T2 — alvo alcancavel em qualidade media. */
    @Test
    fun `alvo alcancavel devolve Hit em escala cheia`() = runTest {
        val result = solveTargetSize(
            targetBytes = 500_000,
            sourceBytes = 1_000_000,
            reencodeRequired = false,
            probe = FakeEncodeProbe(fullSizeAtMaxQuality = 1_000_000),
            allowDownscale = false,
        )

        val hit = result as TargetSolution.Hit
        assertEquals(1f, hit.scale)
        assertTrue(hit.size <= 500_000)
        assertTrue(hit.quality in ImagingDefaults.QUALITY_MIN..ImagingDefaults.QUALITY_MAX)
    }

    /** T3 — alvo alcancavel exatamente na qualidade minima. */
    @Test
    fun `alvo que so cabe na qualidade minima devolve Hit em 30`() = runTest {
        // Na qualidade 30 o falso devolve 1_000_000 * 30/95 = 315_789.
        val result = solveTargetSize(
            targetBytes = 315_789,
            sourceBytes = 1_000_000,
            reencodeRequired = false,
            probe = FakeEncodeProbe(fullSizeAtMaxQuality = 1_000_000),
            allowDownscale = false,
        )

        val hit = result as TargetSolution.Hit
        assertEquals(ImagingDefaults.QUALITY_MIN, hit.quality)
    }

    /** T4 — nem a qualidade minima basta e o usuario nao autorizou reduzir. */
    @Test
    fun `sem autorizacao para reduzir devolve NeedsDownscale`() = runTest {
        val result = solveTargetSize(
            targetBytes = 50_000,
            sourceBytes = 1_000_000,
            reencodeRequired = false,
            probe = FakeEncodeProbe(fullSizeAtMaxQuality = 1_000_000),
            allowDownscale = false,
        )

        val needs = result as TargetSolution.NeedsDownscale
        assertTrue(needs.bestSize > 50_000)
    }

    /** T5 — mesmo caso, com autorizacao: reduz a escala. */
    @Test
    fun `com autorizacao para reduzir devolve Hit em escala menor`() = runTest {
        val result = solveTargetSize(
            targetBytes = 50_000,
            sourceBytes = 1_000_000,
            reencodeRequired = false,
            probe = FakeEncodeProbe(fullSizeAtMaxQuality = 1_000_000),
            allowDownscale = true,
        )

        val hit = result as TargetSolution.Hit
        assertTrue(hit.scale < 1f)
        assertTrue(hit.scale >= ImagingDefaults.MIN_SCALE)
        assertTrue(hit.size <= 50_000)
    }

    /** T6 — alvo impossivel mesmo na escala minima. */
    @Test
    fun `alvo impossivel devolve Impossible`() = runTest {
        val result = solveTargetSize(
            targetBytes = 1_000,
            sourceBytes = 10_000_000,
            reencodeRequired = false,
            probe = FakeEncodeProbe(fullSizeAtMaxQuality = 10_000_000),
            allowDownscale = true,
        )

        val impossible = result as TargetSolution.Impossible
        assertTrue(impossible.bestSize > 1_000)
    }

    /**
     * T7 — o teste mais importante da suite: o teto de codificacoes. Sem ele,
     * uma mudanca inocente no algoritmo deixa a compressao lenta.
     */
    @Test
    fun `nunca excede o teto de codificacoes`() = runTest {
        val cases = listOf(
            Triple(500_000L, 1_000_000L, false),
            Triple(50_000L, 1_000_000L, true),
            Triple(1_000L, 10_000_000L, true),
            Triple(315_789L, 1_000_000L, false),
            Triple(999_999L, 1_000_000L, true),
        )

        for ((target, source, downscale) in cases) {
            val probe = FakeEncodeProbe(fullSizeAtMaxQuality = source)
            solveTargetSize(target, source, reencodeRequired = true, probe, downscale)
            assertTrue(
                "alvo=$target chamou ${probe.calls} vezes",
                probe.calls <= ImagingDefaults.MAX_PROBE_CALLS,
            )
        }
    }

    /** T8 — resultado dentro da banda de aceitacao interrompe a busca cedo. */
    @Test
    fun `resultado dentro da banda de aceitacao interrompe a busca`() = runTest {
        val probe = FakeEncodeProbe(fullSizeAtMaxQuality = 1_000_000)
        // Qualidade 62 (o primeiro ponto medio) da 652_631, dentro de 90%..100%
        // de 660_000: a busca deve parar na primeira medida.
        solveTargetSize(
            targetBytes = 660_000,
            sourceBytes = 1_000_000,
            reencodeRequired = true,
            probe = probe,
            allowDownscale = false,
        )

        assertEquals(1, probe.calls)
    }

    /** T9 — cancelamento propaga; nunca vira falha. */
    @Test
    fun `cancelamento propaga`() = runTest {
        val probe = EncodeProbe { _, _ -> throw CancellationException("cancelado") }

        try {
            solveTargetSize(
                targetBytes = 100,
                sourceBytes = 1_000_000,
                reencodeRequired = true,
                probe = probe,
                allowDownscale = true,
            )
            fail("deveria ter propagado CancellationException")
        } catch (expected: CancellationException) {
            assertEquals("cancelado", expected.message)
        }
    }
}
