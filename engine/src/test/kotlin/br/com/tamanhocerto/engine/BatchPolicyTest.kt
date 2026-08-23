package br.com.tamanhocerto.engine

import br.com.tamanhocerto.core.files.EngineDefaults
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BatchPolicyTest {

    /** E7 — um arquivo por vez e sempre livre. */
    @Test
    fun um_item_nao_exige_recompensa() {
        assertFalse(BatchPolicy.requiresReward(1))
        assertFalse(BatchPolicy.requiresReward(0))
    }

    /** E8 — dois ou mais exigem a recompensa. */
    @Test
    fun dois_ou_mais_exigem_recompensa() {
        assertTrue(BatchPolicy.requiresReward(2))
        assertTrue(BatchPolicy.requiresReward(EngineDefaults.MAX_BATCH_ITEMS))
    }
}
