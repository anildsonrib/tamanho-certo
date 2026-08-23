package br.com.tamanhocerto.navigation

/** Destinos da NavHost (ARCHITECTURE.md secao 9, UI-SPEC secao 2). */
object Destinations {
    const val HOME = "home"
    const val CONFIGURE = "configure/{operationId}"
    const val RESULT = "result"
    const val POLICY = "policy"
    const val ABOUT = "about"

    const val ARG_OPERATION_ID = "operationId"

    fun configure(operationId: String): String = "configure/$operationId"
}
