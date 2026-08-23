package br.com.tamanhocerto.feature.tools.configure

import androidx.annotation.StringRes
import br.com.tamanhocerto.core.model.ImageFormat
import br.com.tamanhocerto.core.model.PageMargin
import br.com.tamanhocerto.core.model.PageOrientation
import br.com.tamanhocerto.core.model.PageSize
import br.com.tamanhocerto.core.model.RenderDensity
import br.com.tamanhocerto.feature.tools.home.ToolId

/**
 * Estado unico e imutavel da tela de configuracao. Todo texto ja chega
 * formatado do ViewModel; o Composable nao formata nada (UI-SPEC secao 10b).
 */
data class ConfigureUiState(
    val tool: ToolId,
    val input: InputSummary = InputSummary(),
    val form: OperationForm = OperationForm.Compress(),
    val validation: Validation = Validation.Ok,
    val notice: NoticeState? = null,
    val phase: Phase = Phase.Idle,
)

data class InputSummary(
    val fileCount: Int = 0,
    val displayName: String? = null,
    /** Ja formatado ("1024 × 768 pixels"); nulo quando ainda nao foi lido. */
    val dimensionsText: String? = null,
    /** Ja formatado ("Tamanho atual: 2,4 MB"). */
    val sizeText: String? = null,
    val pagesText: String? = null,
    val sizeBytes: Long? = null,
    val pageCount: Int? = null,
    val hasAlpha: Boolean = false,
)

/** Bloqueio de formulario: a razao e uma chave de string, nunca texto solto. */
sealed interface Validation {
    data object Ok : Validation
    data class Blocked(@param:StringRes val reason: Int, val formatArg: Any? = null) : Validation
}

data class NoticeState(
    @param:StringRes val message: Int,
    val kind: NoticeKindUi,
    @param:StringRes val actionLabel: Int? = null,
)

enum class NoticeKindUi { INFO, WARNING, ERROR }

sealed interface Phase {
    data object Idle : Phase

    /** `percent` nulo = indicador indeterminado. Nunca inventar numero. */
    data class Running(
        val percent: Int?,
        val index: Int = 0,
        val total: Int = 1,
        val currentName: String? = null,
        val anyItemDone: Boolean = false,
    ) : Phase

    data object Done : Phase
}

/** Um formulario por operacao. */
sealed interface OperationForm {

    data class Compress(
        val targetBytes: Long? = null,
        val customValue: String = "",
        val customUnitIsMb: Boolean = false,
        val format: ImageFormat = ImageFormat.JPEG,
        val qualityMode: Boolean = false,
        val quality: Int = DEFAULT_QUALITY,
        val keepMetadata: Boolean = false,
        val advancedExpanded: Boolean = false,
    ) : OperationForm

    data class Resize(
        val mode: ResizeMode = ResizeMode.PIXELS,
        val width: String = "",
        val height: String = "",
        val lockAspect: Boolean = true,
        val percent: Int = DEFAULT_PERCENT,
        val longestSide: String = "",
        val format: ImageFormat = ImageFormat.JPEG,
        /** Ja formatado pelo ViewModel. */
        val resultText: String? = null,
        val showsNoUpscaleWarning: Boolean = false,
    ) : OperationForm

    data class ImagesToPdf(
        val pageSize: PageSize = PageSize.A4,
        val orientation: PageOrientation = PageOrientation.AUTO,
        val margin: PageMargin = PageMargin.SMALL,
        val targetBytes: Long? = null,
        val order: List<Int> = emptyList(),
    ) : OperationForm

    data class PdfToImages(
        val allPages: Boolean = true,
        val from: String = "",
        val to: String = "",
        val density: RenderDensity = RenderDensity.MEDIUM,
        val format: ImageFormat = ImageFormat.JPEG,
    ) : OperationForm

    data class Convert(
        val format: ImageFormat = ImageFormat.JPEG,
        val flattenColor: Int = WHITE,
    ) : OperationForm

    companion object {
        const val DEFAULT_QUALITY = 80
        const val DEFAULT_PERCENT = 50
        const val WHITE = 0xFFFFFFFF.toInt()
        const val BLACK = 0xFF000000.toInt()
        const val GRAY = 0xFF808080.toInt()

        fun forTool(tool: ToolId): OperationForm = when (tool) {
            ToolId.COMPRESS -> Compress()
            ToolId.RESIZE -> Resize()
            ToolId.IMAGES_TO_PDF -> ImagesToPdf()
            ToolId.PDF_TO_IMAGES -> PdfToImages()
            ToolId.CONVERT -> Convert()
        }
    }
}

enum class ResizeMode { PIXELS, PERCENT, LONGEST_SIDE }

/** Atalhos de tamanho da UI-SPEC secao 4.1, em bytes. */
object SizeShortcuts {
    private const val KB = 1024L
    private const val MB = 1024L * 1024L

    val values: List<Long> = listOf(100 * KB, 300 * KB, 500 * KB, MB, 2 * MB, 5 * MB)
}
