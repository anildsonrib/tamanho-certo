package br.com.tamanhocerto.core.model

/**
 * Libera o processamento em lote.
 *
 * Nao recebe `Activity` de proposito: este modulo e Kotlin puro. Quem fornece
 * a Activity ao SDK e o `:core:ads` (ARCHITECTURE.md secao 8).
 */
interface RewardGate {
    /**
     * `true` = liberado. NUNCA devolve `false` por falha tecnica: sem rede,
     * anuncio nao carregado ou SDK com erro liberam assim mesmo (fail-open).
     * So a recusa explicita do usuario devolve `false`.
     */
    suspend fun requestUnlock(): Boolean
}
