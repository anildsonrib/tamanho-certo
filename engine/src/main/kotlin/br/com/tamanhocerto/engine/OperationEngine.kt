package br.com.tamanhocerto.engine

import br.com.tamanhocerto.core.model.ByteSource
import br.com.tamanhocerto.core.model.Operation
import br.com.tamanhocerto.core.model.RunOptions
import kotlinx.coroutines.flow.Flow

/**
 * O engine NAO chama o `RewardGate`. A decisao de exigir recompensa e da
 * camada de UI, que consulta `BatchPolicy` e so entao chama `run` — e assim o
 * engine permanece testavel sem anuncio e sem Activity (ENGINE-SPEC secao 9).
 */
interface OperationEngine {
    fun run(
        inputs: List<ByteSource>,
        operation: Operation,
        options: RunOptions,
    ): Flow<JobEvent>
}
