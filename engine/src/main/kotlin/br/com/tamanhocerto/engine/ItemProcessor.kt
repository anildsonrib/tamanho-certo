package br.com.tamanhocerto.engine

import br.com.tamanhocerto.core.model.ByteSource
import br.com.tamanhocerto.core.model.Operation
import br.com.tamanhocerto.core.model.OperationResult
import br.com.tamanhocerto.core.model.RunOptions

/**
 * Executa UM item. Existe como interface para que o laco, o progresso e o
 * cancelamento do engine sejam testaveis com motores falsos, sem aparelho
 * (ENGINE-SPEC secao 10.2). A implementacao real chama `:imaging` e `:pdf`.
 */
interface ItemProcessor {

    /**
     * @param onProgress porcentagem do item, ou null quando a etapa nao e
     *   mensuravel. Nunca inventar numero.
     */
    suspend fun process(
        index: Int,
        input: ByteSource,
        operation: Operation,
        options: RunOptions,
        onProgress: suspend (percent: Int?) -> Unit,
    ): OperationResult
}
