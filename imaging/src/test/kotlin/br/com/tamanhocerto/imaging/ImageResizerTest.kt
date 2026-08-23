package br.com.tamanhocerto.imaging

import br.com.tamanhocerto.core.model.ResizeSpec
import br.com.tamanhocerto.core.model.Size
import org.junit.Assert.assertEquals
import org.junit.Test

class ImageResizerTest {

    private val source = Size(width = 4000, height = 3000)

    /** T10 — os quatro specs, com e sem trava de proporcao. */
    @Test
    fun `pixels sem trava usa os valores como vieram`() {
        val result = resolveDimensions(source, ResizeSpec.Pixels(1000, 1000, lockAspect = false))
        assertEquals(Size(1000, 1000), result)
    }

    @Test
    fun `pixels com trava cabe dentro da caixa pedida`() {
        // 1000/4000 = 0,25 e 1000/3000 = 0,333: vale o eixo de maior reducao.
        val result = resolveDimensions(source, ResizeSpec.Pixels(1000, 1000, lockAspect = true))
        assertEquals(Size(1000, 750), result)
    }

    @Test
    fun `porcentagem aplica o fator nos dois eixos`() {
        val result = resolveDimensions(source, ResizeSpec.Percent(50))
        assertEquals(Size(2000, 1500), result)
    }

    @Test
    fun `maior lado vira o valor pedido e o outro acompanha`() {
        val result = resolveDimensions(source, ResizeSpec.LongestSide(1920))
        assertEquals(Size(1920, 1440), result)
    }

    @Test
    fun `maior lado considera a altura quando ela e o maior lado`() {
        val retrato = Size(width = 3000, height = 4000)
        val result = resolveDimensions(retrato, ResizeSpec.LongestSide(1920))
        assertEquals(Size(1440, 1920), result)
    }

    /** T11 — o app nao amplia: devolve o original. */
    @Test
    fun `pedido maior que o original devolve o original`() {
        assertEquals(source, resolveDimensions(source, ResizeSpec.LongestSide(8000)))
        assertEquals(source, resolveDimensions(source, ResizeSpec.Percent(200)))
        assertEquals(
            source,
            resolveDimensions(source, ResizeSpec.Pixels(9000, 9000, lockAspect = true)),
        )
    }

    /** T12 — resultado abaixo de 1 px vira 1 px. */
    @Test
    fun `resultado menor que um pixel vira um pixel`() {
        val result = resolveDimensions(source, ResizeSpec.LongestSide(1))
        assertEquals(Size(1, 1), result)
    }
}
