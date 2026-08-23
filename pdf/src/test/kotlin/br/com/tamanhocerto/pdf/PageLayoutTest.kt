package br.com.tamanhocerto.pdf

import br.com.tamanhocerto.core.model.PageMargin
import br.com.tamanhocerto.core.model.PageOrientation
import br.com.tamanhocerto.core.model.PageRange
import br.com.tamanhocerto.core.model.PageSize
import br.com.tamanhocerto.core.model.PageSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class PageLayoutTest {

    /** P1 — intervalo valido e invalido. */
    @Test
    fun valida_o_intervalo_de_paginas() {
        assertTrue(PageRangeRules.isValid(PageRange.Interval(1, 3), pageCount = 3))
        assertTrue(PageRangeRules.isValid(PageRange.All, pageCount = 1))

        assertFalse(PageRangeRules.isValid(PageRange.Interval(0, 2), pageCount = 3))
        assertFalse(PageRangeRules.isValid(PageRange.Interval(2, 4), pageCount = 3))
        assertFalse(PageRangeRules.isValid(PageRange.Interval(3, 2), pageCount = 3))
        assertFalse(PageRangeRules.isValid(PageRange.All, pageCount = 0))
    }

    @Test
    fun resolve_devolve_indices_zero_based() {
        assertEquals(listOf(0, 1, 2), PageRangeRules.resolve(PageRange.All, 3))
        assertEquals(listOf(1, 2), PageRangeRules.resolve(PageRange.Interval(2, 3), 3))
        assertEquals(emptyList<Int>(), PageRangeRules.resolve(PageRange.Interval(2, 9), 3))
    }

    /** P2 — imagem paisagem com orientacao automatica gira a pagina. */
    @Test
    fun orientacao_automatica_segue_a_imagem() {
        val paisagem = computePlacement(
            imageWidth = 2000,
            imageHeight = 1000,
            spec = PageSpec(PageSize.A4, PageOrientation.AUTO, PageMargin.NONE),
        )
        assertEquals(PdfDefaults.A4_HEIGHT_PT, paisagem.pageWidthPt)
        assertEquals(PdfDefaults.A4_WIDTH_PT, paisagem.pageHeightPt)

        val retrato = computePlacement(
            imageWidth = 1000,
            imageHeight = 2000,
            spec = PageSpec(PageSize.A4, PageOrientation.AUTO, PageMargin.NONE),
        )
        assertEquals(PdfDefaults.A4_WIDTH_PT, retrato.pageWidthPt)
        assertEquals(PdfDefaults.A4_HEIGHT_PT, retrato.pageHeightPt)
    }

    /** P3 — A4 retrato com margem media: centralizado e sem distorcer. */
    @Test
    fun a4_retrato_com_margem_media_centraliza_sem_distorcer() {
        val placement = computePlacement(
            imageWidth = 3000,
            imageHeight = 4000,
            spec = PageSpec(PageSize.A4, PageOrientation.PORTRAIT, PageMargin.MEDIUM),
        )
        val rect = placement.destRectPt

        val margin = PdfDefaults.MARGIN_MEDIUM_PT
        assertTrue(rect.left >= margin)
        assertTrue(rect.top >= margin)
        assertTrue(rect.right <= placement.pageWidthPt - margin)
        assertTrue(rect.bottom <= placement.pageHeightPt - margin)

        // Proporcao preservada (tolerancia de 1% por causa do arredondamento).
        val originalRatio = 3000.0 / 4000.0
        val drawnRatio = rect.width.toDouble() / rect.height
        assertTrue("proporcao=$drawnRatio", abs(originalRatio - drawnRatio) < 0.01)

        // Centralizado nos dois eixos.
        assertEquals(
            placement.pageWidthPt - rect.right,
            rect.left,
        )
    }

    /** P4 — imagem menor que a area util nao e ampliada. */
    @Test
    fun imagem_menor_que_a_area_util_nao_e_ampliada() {
        val placement = computePlacement(
            imageWidth = 100,
            imageHeight = 80,
            spec = PageSpec(PageSize.A4, PageOrientation.PORTRAIT, PageMargin.NONE),
        )
        assertEquals(100, placement.destRectPt.width)
        assertEquals(80, placement.destRectPt.height)
    }

    /** P5 — FitImage fixa o maior lado em 842 pt. */
    @Test
    fun fit_image_fixa_o_maior_lado() {
        val placement = computePlacement(
            imageWidth = 4000,
            imageHeight = 2000,
            spec = PageSpec(PageSize.FitImage, PageOrientation.AUTO, PageMargin.NONE),
        )
        assertEquals(PdfDefaults.FIT_IMAGE_LONGEST_SIDE_PT, placement.pageWidthPt)
        assertEquals(PdfDefaults.FIT_IMAGE_LONGEST_SIDE_PT / 2, placement.pageHeightPt)
    }

    /** P6 — margem que consome mais de 80% da pagina cai para zero. */
    @Test
    fun margem_grande_demais_cai_para_zero() {
        // Pagina de 100 x 100 pt: a margem media consumiria 96 dos 100.
        assertEquals(0, effectiveMarginPt(PdfDefaults.MARGIN_MEDIUM_PT, 100, 100))
        // Em A4 a mesma margem cabe folgada.
        assertEquals(
            PdfDefaults.MARGIN_MEDIUM_PT,
            effectiveMarginPt(
                PdfDefaults.MARGIN_MEDIUM_PT,
                PdfDefaults.A4_WIDTH_PT,
                PdfDefaults.A4_HEIGHT_PT,
            ),
        )
    }
}
