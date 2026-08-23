package br.com.tamanhocerto.core.files

import androidx.activity.result.contract.ActivityResultContracts
import br.com.tamanhocerto.core.files.EngineDefaults.MAX_BATCH_ITEMS

/**
 * Contratos de selecao do sistema. Photo Picker e Storage Access Framework
 * NAO exigem permissao — e por isso que o app nao declara nenhuma (D4).
 */
object PickerContracts {

    fun pickImage() = ActivityResultContracts.PickVisualMedia()

    fun pickImages(maxItems: Int = MAX_BATCH_ITEMS) =
        ActivityResultContracts.PickMultipleVisualMedia(maxItems)

    fun openPdf() = ActivityResultContracts.OpenDocument()

    val PDF_MIME_FILTER = arrayOf("application/pdf")

    fun createDocument(mimeType: String) = ActivityResultContracts.CreateDocument(mimeType)

    fun openDocumentTree() = ActivityResultContracts.OpenDocumentTree()
}
