package br.com.tamanhocerto.imaging

import br.com.tamanhocerto.core.model.FailureReason
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/** Erro do motor de imagem, ja traduzido para o motivo que a tela conhece. */
class ImagingException(
    val reason: FailureReason,
    cause: Throwable? = null,
) : Exception(reason.name, cause)

/**
 * Mapeamento fechado de IMAGING-SPEC secao 9. `CancellationException` NAO
 * aparece aqui de proposito: cancelamento se relanca, nunca vira falha.
 */
fun Throwable.toFailureReason(): FailureReason = when {
    this is CancellationException -> throw this
    this is ImagingException -> reason
    this is OutOfMemoryError -> FailureReason.OUT_OF_MEMORY
    this is IOException && isOutOfSpace() -> FailureReason.OUT_OF_SPACE
    else -> FailureReason.UNKNOWN
}

/**
 * O Android nao tem excecao propria para disco cheio: a mensagem do
 * `ErrnoException` (ENOSPC) e o unico sinal disponivel.
 */
private fun IOException.isOutOfSpace(): Boolean {
    val text = (message ?: "") + (cause?.message ?: "")
    return text.contains("ENOSPC", ignoreCase = true) ||
        text.contains("No space left", ignoreCase = true)
}

/**
 * Executa o bloco convertendo qualquer falha no motivo correspondente.
 * `OutOfMemoryError` e capturado aqui: o app nunca fecha sozinho
 * (ARCHITECTURE.md secao 5).
 */
inline fun <T> runImaging(block: () -> T): T =
    try {
        block()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (imaging: ImagingException) {
        throw imaging
    } catch (oom: OutOfMemoryError) {
        throw ImagingException(FailureReason.OUT_OF_MEMORY, oom)
    } catch (error: Exception) {
        throw ImagingException(error.toFailureReason(), error)
    }
