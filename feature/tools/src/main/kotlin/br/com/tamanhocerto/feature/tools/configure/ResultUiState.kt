package br.com.tamanhocerto.feature.tools.configure

import androidx.annotation.StringRes
import br.com.tamanhocerto.core.model.FailureReason
import br.com.tamanhocerto.feature.tools.R
import java.io.File

/** Estado da tela de resultado. Todo texto ja chega formatado. */
data class ResultUiState(
    val items: List<ResultItem> = emptyList(),
    val beforeAfterText: String? = null,
    val reductionText: String? = null,
    val batchSummary: String? = null,
    val banner: ResultBanner = ResultBanner.SUCCESS,
    /** Texto da faixa de estado, ja formatado pelo ViewModel. */
    val bannerText: String? = null,
    val detailLines: List<String> = emptyList(),
    val notices: List<NoticeState> = emptyList(),
    /** Diálogo modal de alvo nao atingido; o unico do app. */
    val downscalePrompt: DownscalePrompt? = null,
)

enum class ResultBanner { SUCCESS, WARNING, ERROR }

data class DownscalePrompt(val targetText: String)

data class ResultItem(
    val name: String,
    val beforeAfterText: String,
    val state: ItemState,
    /** Mensagem especifica da falha; nunca "erro inesperado" generico. */
    @param:StringRes val errorMessage: Int? = null,
    val file: File? = null,
    val mimeType: String? = null,
)

enum class ItemState { SUCCESS, WARNING, FAILED }

/** Mapeamento fechado de motivo para mensagem (STRINGS.md secao 12). */
@StringRes
fun FailureReason.messageRes(): Int = when (this) {
    FailureReason.PDF_PASSWORD_PROTECTED -> R.string.error_pdf_password
    FailureReason.FILE_CORRUPT -> R.string.error_file_corrupt
    FailureReason.FORMAT_UNSUPPORTED -> R.string.error_format_unsupported
    FailureReason.OUT_OF_SPACE -> R.string.error_out_of_space
    FailureReason.OUT_OF_MEMORY -> R.string.error_out_of_memory
    FailureReason.CANCELLED -> R.string.error_cancelled
    FailureReason.UNKNOWN -> R.string.error_unknown
}
