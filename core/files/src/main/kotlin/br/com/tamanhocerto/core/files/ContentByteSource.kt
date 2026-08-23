package br.com.tamanhocerto.core.files

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import br.com.tamanhocerto.core.model.ByteSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.InputStream

/**
 * Leitura de um arquivo escolhido pelo usuario, por URI concedida pelo
 * sistema. Nenhuma permissao envolvida, e o original nunca e aberto para
 * escrita (ARCHITECTURE.md secao 6).
 */
class ContentByteSource(
    private val resolver: ContentResolver,
    private val uri: Uri,
    override val displayName: String?,
    override val byteSize: Long?,
) : ByteSource {

    override suspend fun openStream(): InputStream =
        resolver.openInputStream(uri) ?: throw FileNotFoundException(uri.toString())

    companion object {
        /**
         * Le nome e tamanho pelo `OpenableColumns`. Coluna ausente vira nulo:
         * ausencia de dado significa "nao sei", nunca zero.
         */
        suspend fun from(resolver: ContentResolver, uri: Uri): ContentByteSource =
            withContext(Dispatchers.IO) {
                var name: String? = null
                var size: Long? = null

                resolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex >= 0 && !cursor.isNull(nameIndex)) {
                            name = cursor.getString(nameIndex)
                        }
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                            size = cursor.getLong(sizeIndex)
                        }
                    }
                }

                ContentByteSource(resolver, uri, name, size)
            }
    }
}
