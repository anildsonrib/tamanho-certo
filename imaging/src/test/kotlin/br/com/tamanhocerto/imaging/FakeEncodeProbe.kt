package br.com.tamanhocerto.imaging

/**
 * Probe falso: modela o tamanho como area x fator de qualidade, o que e
 * monotono como o codec real sem depender dele (IMAGING-SPEC secao 10.1).
 *
 * @param fullSizeAtMaxQuality bytes que a imagem ocuparia em escala 1 e
 *   qualidade [ImagingDefaults.QUALITY_MAX].
 */
class FakeEncodeProbe(
    private val fullSizeAtMaxQuality: Long,
) : EncodeProbe {
    var calls: Int = 0
        private set

    override suspend fun sizeAt(quality: Int, scale: Float): Long {
        calls++
        val qualityFactor = quality.toDouble() / ImagingDefaults.QUALITY_MAX
        val areaFactor = (scale * scale).toDouble()
        return (fullSizeAtMaxQuality * qualityFactor * areaFactor).toLong()
    }
}
