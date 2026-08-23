package br.com.tamanhocerto.core.files

import br.com.tamanhocerto.core.files.EngineDefaults.FREE_SPACE_MARGIN
import br.com.tamanhocerto.core.files.EngineDefaults.MIN_FREE_SPACE_BYTES
import java.io.File
import kotlin.math.max

sealed interface SpaceCheck {
    data object Ok : SpaceCheck
    data class Insufficient(val needed: Long, val available: Long) : SpaceCheck
}

/**
 * Verificado ANTES de comecar, nunca no meio: insuficiente vira OUT_OF_SPACE
 * sem ter processado nada (ENGINE-SPEC secao 5).
 */
object FreeSpace {

    fun needed(inputBytes: Long): Long =
        max((inputBytes * FREE_SPACE_MARGIN).toLong(), MIN_FREE_SPACE_BYTES)

    fun check(inputBytes: Long, dir: File): SpaceCheck {
        val required = needed(inputBytes)
        val available = dir.usableSpace
        return if (available >= required) {
            SpaceCheck.Ok
        } else {
            SpaceCheck.Insufficient(required, available)
        }
    }
}
