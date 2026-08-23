package br.com.tamanhocerto.engine

import br.com.tamanhocerto.core.model.OperationResult

sealed interface JobEvent {
    data class Started(val total: Int) : JobEvent

    /** `percent` nulo = nao da para medir. NUNCA inventar porcentagem. */
    data class Progress(val index: Int, val total: Int, val percent: Int?) : JobEvent

    data class ItemDone(val index: Int, val name: String, val result: OperationResult) : JobEvent

    data class Finished(val succeeded: Int, val failed: Int) : JobEvent
}
