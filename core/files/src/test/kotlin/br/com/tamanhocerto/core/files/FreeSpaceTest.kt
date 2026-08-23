package br.com.tamanhocerto.core.files

import org.junit.Assert.assertEquals
import org.junit.Test

class FreeSpaceTest {

    /** E9 — margem de 3x e piso de 20 MB. */
    @Test
    fun exige_tres_vezes_a_entrada_respeitando_o_piso() {
        // Entrada pequena: vale o piso.
        assertEquals(EngineDefaults.MIN_FREE_SPACE_BYTES, FreeSpace.needed(1_000))

        // Entrada grande: vale a margem de 3x.
        val grande = 100L * 1024 * 1024
        assertEquals((grande * 3), FreeSpace.needed(grande))
    }
}
