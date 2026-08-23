package br.com.tamanhocerto.core.model

import java.io.File

sealed interface OperationResult {
    data class Success(val output: OutputRef, val stats: Stats) : OperationResult

    /**
     * Nao e erro: o app entrega o melhor que conseguiu e diz que nao chegou ao
     * alvo, com a sugestao do proximo passo (ARCHITECTURE.md secao 3).
     */
    data class TargetMissed(
        val output: OutputRef,
        val stats: Stats,
        val suggestion: Suggestion,
    ) : OperationResult

    data class Failed(val reason: FailureReason) : OperationResult
}

/** Preenchido a partir do ImageOutcome / PdfBuildOutcome das fases 3 e 4. */
data class Stats(
    val bytesBefore: Long,
    val bytesAfter: Long,
    val finalWidth: Int? = null,
    val finalHeight: Int? = null,
    val qualityUsed: Int? = null,
    val wasDownsampledForMemory: Boolean = false,
    val didNotUpscale: Boolean = false,
)

enum class Suggestion { NEEDS_DOWNSCALE, TRY_LOSSY_FORMAT, NONE }

/** Referencia ao arquivo produzido, dentro de cacheDir/work. */
data class OutputRef(val file: File, val suggestedName: String, val mimeType: String)

/**
 * Cada motivo tem mensagem propria em recurso de string (STRINGS.md secao 12).
 * Nada de "erro inesperado" — e o que derruba nota na loja.
 */
enum class FailureReason {
    PDF_PASSWORD_PROTECTED,
    FILE_CORRUPT,
    FORMAT_UNSUPPORTED,
    OUT_OF_SPACE,
    OUT_OF_MEMORY,
    CANCELLED,
    UNKNOWN,
}
