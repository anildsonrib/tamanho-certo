package br.com.tamanhocerto.pdf

import br.com.tamanhocerto.core.model.FailureReason
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class PdfException(
    val reason: FailureReason,
    cause: Throwable? = null,
) : Exception(reason.name, cause)

/**
 * Mapeamento fechado da PDF-SPEC secao 6.
 *
 * `SecurityException` e o sinal de PDF protegido por senha, e e por isso que
 * esse caso tem tipo proprio: o PRD o identifica como a reclamacao mais
 * provavel do app se for mal tratado.
 */
inline fun <T> runPdf(block: () -> T): T =
    try {
        block()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (pdf: PdfException) {
        throw pdf
    } catch (protected: SecurityException) {
        throw PdfException(FailureReason.PDF_PASSWORD_PROTECTED, protected)
    } catch (oom: OutOfMemoryError) {
        throw PdfException(FailureReason.OUT_OF_MEMORY, oom)
    } catch (io: IOException) {
        throw PdfException(io.toPdfReason(), io)
    } catch (error: Exception) {
        throw PdfException(FailureReason.UNKNOWN, error)
    }

/** Disco cheio so se distingue pela mensagem do ErrnoException (ENOSPC). */
fun IOException.toPdfReason(): FailureReason {
    val text = (message ?: "") + (cause?.message ?: "")
    return if (text.contains("ENOSPC", ignoreCase = true) ||
        text.contains("No space left", ignoreCase = true)
    ) {
        FailureReason.OUT_OF_SPACE
    } else {
        FailureReason.FILE_CORRUPT
    }
}
