package br.com.tamanhocerto.core.files

import br.com.tamanhocerto.core.model.ByteSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * `PdfRenderer` exige um `ParcelFileDescriptor` sobre arquivo local
 * pesquisavel, e uma URI de documento nao serve (PDF-SPEC secao 5).
 *
 * Copiar SO quando a operacao for PdfToImages. Imagens nao sao copiadas — sao
 * lidas direto por stream.
 */
class SeekableCopy @Inject constructor(
    private val workDir: WorkDir,
) {
    suspend fun of(source: ByteSource, name: String = "input.pdf"): File =
        withContext(Dispatchers.IO) {
            val target = workDir.allocate(name)
            source.openStream().use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
            target
        }
}
