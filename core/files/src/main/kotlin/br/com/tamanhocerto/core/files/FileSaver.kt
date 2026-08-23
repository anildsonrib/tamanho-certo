package br.com.tamanhocerto.core.files

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import javax.inject.Inject

/**
 * Gravacao pelo SAF: quem escolhe onde o resultado vai e o usuario. Nenhuma
 * escrita em MediaStore (D18) e nenhuma permissao persistente de URI (D8).
 */
class FileSaver @Inject constructor() {

    suspend fun save(resolver: ContentResolver, from: File, to: Uri): Long =
        withContext(Dispatchers.IO) {
            val output = resolver.openOutputStream(to)
                ?: throw FileNotFoundException(to.toString())
            output.use { sink -> from.inputStream().use { it.copyTo(sink) } }
            from.length()
        }
}
