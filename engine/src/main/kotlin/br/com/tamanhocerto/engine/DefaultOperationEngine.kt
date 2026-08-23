package br.com.tamanhocerto.engine

import br.com.tamanhocerto.core.files.EngineDefaults.PROGRESS_THROTTLE_MS
import br.com.tamanhocerto.core.files.FreeSpace
import br.com.tamanhocerto.core.files.SpaceCheck
import br.com.tamanhocerto.core.model.ByteSource
import br.com.tamanhocerto.core.model.FailureReason
import br.com.tamanhocerto.core.model.Operation
import br.com.tamanhocerto.core.model.OperationResult
import br.com.tamanhocerto.core.model.RunOptions
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.coroutineContext

/**
 * Uma operacao por vez, itens em serie. Paralelizar multiplicaria o pico de
 * memoria sem ganho: o gargalo e CPU e I/O (D19).
 */
class DefaultOperationEngine @Inject constructor(
    private val processor: ItemProcessor,
    private val workspace: Workspace,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val now: () -> Long = System::currentTimeMillis,
) : OperationEngine {

    /** O que o engine precisa do sistema de arquivos, e nada mais. */
    interface Workspace {
        suspend fun prepare(): File
    }

    override fun run(
        inputs: List<ByteSource>,
        operation: Operation,
        options: RunOptions,
    ): Flow<JobEvent> = flow {
        val dir = workspace.prepare()

        // Verificacao de espaco ANTES de comecar, com o MAIOR arquivo: os
        // itens sao processados em serie e cada saida sai antes do proximo.
        val largest = inputs.mapNotNull { it.byteSize }.maxOrNull() ?: 0L
        val space = FreeSpace.check(largest, dir)
        if (space is SpaceCheck.Insufficient) {
            emit(JobEvent.Started(inputs.size))
            inputs.forEachIndexed { index, input ->
                emit(
                    JobEvent.ItemDone(
                        index = index,
                        name = input.displayName.orEmpty(),
                        result = OperationResult.Failed(FailureReason.OUT_OF_SPACE),
                    ),
                )
            }
            emit(JobEvent.Finished(succeeded = 0, failed = inputs.size))
            return@flow
        }

        // ImagesToPdf junta tudo num documento so: um item, nao N.
        val itemCount = if (operation.consumesAllInputs()) 1 else inputs.size
        emit(JobEvent.Started(itemCount))

        if (operation.consumesAllInputs()) {
            val result = try {
                processor.processAll(inputs, operation, options) { percent ->
                    emit(JobEvent.Progress(0, 1, percent))
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (oom: OutOfMemoryError) {
                OperationResult.Failed(FailureReason.OUT_OF_MEMORY)
            } catch (error: Exception) {
                OperationResult.Failed(error.toFailureReason())
            }
            emit(JobEvent.ItemDone(0, inputs.firstOrNull()?.displayName.orEmpty(), result))
            val failed = if (result is OperationResult.Failed) 1 else 0
            emit(JobEvent.Finished(succeeded = 1 - failed, failed = failed))
            return@flow
        }

        var succeeded = 0
        var failed = 0
        var lastProgressAt = 0L

        for ((index, input) in inputs.withIndex()) {
            coroutineContext.ensureActive()

            val result = try {
                processor.process(index, input, operation, options) { percent ->
                    val instant = now()
                    if (instant - lastProgressAt >= PROGRESS_THROTTLE_MS) {
                        lastProgressAt = instant
                        emit(JobEvent.Progress(index, inputs.size, percent))
                    }
                }
            } catch (cancellation: CancellationException) {
                // Cancelamento sempre relancado, nunca convertido em Failed.
                // Os itens ja concluidos permanecem disponiveis.
                throw cancellation
            } catch (oom: OutOfMemoryError) {
                // O app nunca fecha sozinho (ARCHITECTURE.md secao 5).
                OperationResult.Failed(FailureReason.OUT_OF_MEMORY)
            } catch (error: Exception) {
                OperationResult.Failed(error.toFailureReason())
            }

            if (result is OperationResult.Failed) failed++ else succeeded++

            // Uma falha nao interrompe o lote: o proximo item continua.
            emit(JobEvent.ItemDone(index, input.displayName.orEmpty(), result))
        }

        // O WorkDir NAO e apagado aqui: as saidas ainda serao salvas ou
        // compartilhadas pela tela de resultado. A limpeza ocorre no prepare()
        // seguinte e no onDestroy (ENGINE-SPEC secao 9, passo 6).
        emit(JobEvent.Finished(succeeded, failed))
    }.flowOn(dispatcher)
}

/** Falha sem tipo conhecido nunca vira sucesso silencioso. */
private fun Throwable.toFailureReason(): FailureReason = FailureReason.UNKNOWN
