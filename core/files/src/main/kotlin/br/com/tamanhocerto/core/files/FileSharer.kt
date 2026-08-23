package br.com.tamanhocerto.core.files

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

/**
 * Share sheet por `FileProvider` nao exportado, com URI temporaria
 * (ARCHITECTURE.md secao 6).
 */
class FileSharer @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val authority: String get() = "${context.packageName}.fileprovider"

    fun uriFor(file: File): Uri = FileProvider.getUriForFile(context, authority, file)

    fun shareIntent(files: List<File>, mimeType: String): Intent {
        val uris = files.map(::uriFor)
        return if (uris.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uris.first())
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = mimeType
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
    }
}
