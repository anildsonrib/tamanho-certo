package br.com.tamanhocerto.engine

import br.com.tamanhocerto.core.files.EngineDefaults.FREE_BATCH_LIMIT

/**
 * PURO: nao importa `android.*`. E a regra de negocio da monetizacao inteira
 * em uma linha — muda-la e mudar o produto, nao um detalhe (ENGINE-SPEC
 * secao 8).
 *
 * Um arquivo por vez e sempre livre. Dois ou mais exigem a recompensa.
 */
object BatchPolicy {
    fun requiresReward(itemCount: Int): Boolean = itemCount > FREE_BATCH_LIMIT
}
