package br.com.tamanhocerto.core.files

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OutputNamingTest {

    /** E1 — nome comum. */
    @Test
    fun nome_comum_recebe_sufixo_e_extensao() {
        assertEquals(
            "foto-menor.jpg",
            OutputNaming.nameFor("foto.jpg", "-menor", "jpg"),
        )
    }

    /** E2 — nome nulo cai no fallback. */
    @Test
    fun nome_nulo_vira_arquivo() {
        assertEquals(
            "arquivo-menor.jpg",
            OutputNaming.nameFor(null, "-menor", "jpg"),
        )
        assertEquals(
            "arquivo-menor.jpg",
            OutputNaming.nameFor("   ", "-menor", "jpg"),
        )
    }

    /** E3 — caracteres invalidos em nome de arquivo somem. */
    @Test
    fun caracteres_invalidos_sao_removidos() {
        assertEquals(
            "fototeste-menor.jpg",
            OutputNaming.nameFor("""foto:*?"<>|teste.jpg""", "-menor", "jpg"),
        )
    }

    /** E4 — base longa e cortada em 60 caracteres. */
    @Test
    fun base_longa_e_cortada() {
        val longa = "a".repeat(200)
        val name = OutputNaming.nameFor("$longa.jpg", "-menor", "jpg")

        assertEquals(EngineDefaults.MAX_BASE_NAME_LENGTH, name.substringBefore("-menor").length)
        assertTrue(name.endsWith("-menor.jpg"))
    }

    /** E5 — colisao recebe -2, depois -3. */
    @Test
    fun colisao_recebe_numero() {
        val um = setOf("foto-menor.jpg")
        assertEquals("foto-menor-2.jpg", OutputNaming.nameFor("foto.jpg", "-menor", "jpg", um))

        val dois = setOf("foto-menor.jpg", "foto-menor-2.jpg")
        assertEquals("foto-menor-3.jpg", OutputNaming.nameFor("foto.jpg", "-menor", "jpg", dois))
    }

    /** E6 — pagina de PDF entra antes do sufixo, com dois digitos. */
    @Test
    fun pagina_de_pdf_entra_antes_do_sufixo() {
        assertEquals(
            "foto-01-pagina.jpg",
            OutputNaming.nameFor("foto.pdf", "-pagina", "jpg", pageNumber = 1),
        )
        assertEquals(
            "foto-12-pagina.jpg",
            OutputNaming.nameFor("foto.pdf", "-pagina", "jpg", pageNumber = 12),
        )
    }
}
