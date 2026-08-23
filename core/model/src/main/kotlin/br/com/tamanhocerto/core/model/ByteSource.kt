package br.com.tamanhocerto.core.model

import java.io.InputStream

/**
 * Leitura do arquivo de entrada. A interface fica aqui para que `:imaging` e
 * `:pdf` a consumam sem conhecer Android; a implementacao sobre
 * `ContentResolver` fica em `:core:files` (ENGINE-SPEC secao 3).
 *
 * `byteSize` e nulo quando o tamanho nao pode ser lido: ausencia de dado
 * significa "nao sei", nunca zero.
 */
interface ByteSource {
    val displayName: String?
    val byteSize: Long?

    suspend fun openStream(): InputStream
}
