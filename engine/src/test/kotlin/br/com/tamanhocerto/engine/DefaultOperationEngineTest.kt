package br.com.tamanhocerto.engine

import br.com.tamanhocerto.core.files.EngineDefaults
import br.com.tamanhocerto.core.model.ByteSource
import br.com.tamanhocerto.core.model.FailureReason
import br.com.tamanhocerto.core.model.ImageFormat
import br.com.tamanhocerto.core.model.Operation
import br.com.tamanhocerto.core.model.OperationResult
import br.com.tamanhocerto.core.model.OutputRef
import br.com.tamanhocerto.core.model.RunOptions
import br.com.tamanhocerto.core.model.SizeTarget
import br.com.tamanhocerto.core.model.Stats
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream

class DefaultOperationEngineTest {

    private val operation = Operation.Compress(SizeTarget.Bytes(1_000), ImageFormat.JPEG)
    private val options = RunOptions()

    private class FakeSource(
        override val displayName: String,
        override val byteSize: Long = 1_000,
    ) : ByteSource {
        override suspend fun openStream(): InputStream = ByteArrayInputStream(ByteArray(0))
    }

    /** Diretorio real de teste: `usableSpace` de verdade, sem simular o disco. */
    private fun workspace(dir: File = File(System.getProperty("java.io.tmpdir")!!)) =
        object : DefaultOperationEngine.Workspace {
            override suspend fun prepare(): File = dir
        }

    private fun success(name: String) = OperationResult.Success(
        output = OutputRef(File(name), name, "image/jpeg"),
        stats = Stats(bytesBefore = 1_000, bytesAfter = 500),
    )

    private fun engine(
        processor: ItemProcessor,
        workspace: DefaultOperationEngine.Workspace = workspace(),
        now: () -> Long = System::currentTimeMillis,
    ) = DefaultOperationEngine(processor, workspace, UnconfinedTestDispatcher(), now)

    /** E10 — tres itens, todos bem. */
    @Test
    fun tres_itens_bem_sucedidos() = runTest {
        val processor = ItemProcessorStub { index, _ -> success("saida$index.jpg") }
        val inputs = List(3) { FakeSource("foto$it.jpg") }

        val events = engine(processor).run(inputs, operation, options).toList()

        assertEquals(JobEvent.Started(3), events.first())
        assertEquals(3, events.filterIsInstance<JobEvent.ItemDone>().size)
        assertEquals(JobEvent.Finished(3, 0), events.last())
    }

    /** E11 — o item 2 falha e o lote continua. */
    @Test
    fun falha_de_um_item_nao_interrompe_o_lote() = runTest {
        val processor = ItemProcessorStub { index, _ ->
            if (index == 1) {
                OperationResult.Failed(FailureReason.FILE_CORRUPT)
            } else {
                success("saida$index.jpg")
            }
        }
        val inputs = List(3) { FakeSource("foto$it.jpg") }

        val events = engine(processor).run(inputs, operation, options).toList()

        assertEquals(3, events.filterIsInstance<JobEvent.ItemDone>().size)
        assertEquals(JobEvent.Finished(2, 1), events.last())
    }

    /** E12 — cancelamento no item 2 propaga e preserva o item 1. */
    @Test
    fun cancelamento_propaga_e_preserva_o_concluido() = runTest {
        val processor = ItemProcessorStub { index, _ ->
            if (index == 1) throw CancellationException("cancelado") else success("saida$index.jpg")
        }
        val inputs = List(3) { FakeSource("foto$it.jpg") }
        val received = mutableListOf<JobEvent>()

        try {
            engine(processor).run(inputs, operation, options).collect { received += it }
            fail("deveria ter propagado CancellationException")
        } catch (expected: CancellationException) {
            assertEquals("cancelado", expected.message)
        }

        val done = received.filterIsInstance<JobEvent.ItemDone>()
        assertEquals(1, done.size)
        assertTrue(done.first().result is OperationResult.Success)
    }

    /** E13 — sem espaco, nada e processado. */
    @Test
    fun sem_espaco_nada_e_processado() = runTest {
        var called = false
        val processor = ItemProcessorStub { index, _ ->
            called = true
            success("saida$index.jpg")
        }
        // Diretorio inexistente: usableSpace = 0.
        val semEspaco = workspace(File("/diretorio/que/nao/existe/tamanho-certo"))
        val inputs = List(2) { FakeSource("foto$it.jpg") }

        val events = engine(processor, semEspaco).run(inputs, operation, options).toList()

        assertTrue(!called)
        val done = events.filterIsInstance<JobEvent.ItemDone>()
        assertEquals(2, done.size)
        done.forEach {
            assertEquals(
                OperationResult.Failed(FailureReason.OUT_OF_SPACE),
                it.result,
            )
        }
        assertEquals(JobEvent.Finished(0, 2), events.last())
    }

    /** E14 — emissoes de progresso respeitam o intervalo minimo. */
    @Test
    fun progresso_respeita_o_intervalo_minimo() = runTest {
        var clock = 0L
        val processor = ItemProcessorStub(
            onProgressCalls = 5,
        ) { index, _ -> success("saida$index.jpg") }

        // O relogio avanca metade do intervalo por chamada: so parte passa.
        val events = engine(processor, now = { clock += EngineDefaults.PROGRESS_THROTTLE_MS / 2; clock })
            .run(listOf(FakeSource("foto.jpg")), operation, options)
            .toList()

        val progress = events.filterIsInstance<JobEvent.Progress>()
        assertTrue("emitiu ${progress.size} de 5", progress.size < 5)
    }

    /** E15 — etapa sem medida reporta null, nunca um numero inventado. */
    @Test
    fun etapa_sem_medida_reporta_null() = runTest {
        val processor = ItemProcessorStub(onProgressCalls = 1) { index, _ ->
            success("saida$index.jpg")
        }
        var clock = 0L

        val events = engine(processor, now = { clock += EngineDefaults.PROGRESS_THROTTLE_MS; clock })
            .run(listOf(FakeSource("foto.jpg")), operation, options)
            .toList()

        val progress = events.filterIsInstance<JobEvent.Progress>()
        assertEquals(1, progress.size)
        assertEquals(null, progress.first().percent)
    }

    private class ItemProcessorStub(
        private val onProgressCalls: Int = 1,
        private val result: (index: Int, input: ByteSource) -> OperationResult,
    ) : ItemProcessor {
        override suspend fun process(
            index: Int,
            input: ByteSource,
            operation: Operation,
            options: RunOptions,
            onProgress: suspend (percent: Int?) -> Unit,
        ): OperationResult {
            repeat(onProgressCalls) { onProgress(null) }
            return result(index, input)
        }
    }
}
