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

    /**
     * Operacoes que consomem a lista INTEIRA como uma unidade. `ImagesToPdf`
     * e a unica: varias imagens viram UM documento (PRD secao 3.4), e nao um
     * PDF por imagem.
     */
    suspend fun processAll(
        inputs: List<ByteSource>,
        operation: Operation,
        options: RunOptions,
        onProgress: suspend (percent: Int?) -> Unit,
    ): OperationResult = process(0, inputs.first(), operation, options, onProgress)
}

/** true quando a operacao consome a lista inteira de uma vez. */
fun Operation.consumesAllInputs(): Boolean = this is Operation.ImagesToPdf
