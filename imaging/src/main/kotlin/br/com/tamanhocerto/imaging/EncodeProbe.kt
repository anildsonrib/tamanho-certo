package br.com.tamanhocerto.imaging

/** Codifica na qualidade e escala dadas e devolve so o tamanho em bytes. */
fun interface EncodeProbe {
    suspend fun sizeAt(quality: Int, scale: Float): Long
}
