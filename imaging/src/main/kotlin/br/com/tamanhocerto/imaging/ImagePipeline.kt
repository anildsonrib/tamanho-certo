package br.com.tamanhocerto.imaging

import androidx.exifinterface.media.ExifInterface
import br.com.tamanhocerto.core.model.ByteSource
import br.com.tamanhocerto.core.model.ImageFormat
import br.com.tamanhocerto.core.model.MetadataPolicy
import br.com.tamanhocerto.core.model.ResizeSpec
import br.com.tamanhocerto.core.model.Size
import br.com.tamanhocerto.core.model.SizeTarget
import br.com.tamanhocerto.core.model.Suggestion
import br.com.tamanhocerto.imaging.ImagingDefaults.QUALITY_MAX
import java.io.File
import java.io.OutputStream

data class ImageOutcome(
    val bytesWritten: Long,
    val finalWidth: Int,
    val finalHeight: Int,
    /** null quando o formato e sem perdas. */
    val qualityUsed: Int?,
    val targetHit: Boolean,
    val suggestion: Suggestion?,
    val wasDownsampledForMemory: Boolean,
    val didNotUpscale: Boolean,
)

/**
 * Fachada usada pelo `:engine`. Recebe um `OutputStream` pronto: nomear e
 * gravar arquivo e da fase 5 (IMAGING-SPEC secao 12).
 *
 * `outputFile` e opcional e existe apenas para MetadataPolicy.KEEP_ALL: o
 * `ExifInterface` so grava tags sobre um arquivo, nunca sobre um stream. Com
 * KEEP_ALL e sem arquivo, os metadados nao sao copiados.
 */
object ImagePipeline {

    suspend fun compress(
        source: ByteSource,
        target: SizeTarget,
        format: ImageFormat,
        metadata: MetadataPolicy,
        allowDownscale: Boolean,
        out: OutputStream,
        outputFile: File? = null,
    ): ImageOutcome {
        // PNG e sem perdas: a busca nao tem o que buscar. Gastar sete
        // codificacoes para descobrir isso e desperdicio, e a mensagem ao
        // usuario e melhor (IMAGING-SPEC secao 8).
        if (target is SizeTarget.Bytes && format == ImageFormat.PNG) {
            val info = ImageReader.readInfo(source)
            return ImageOutcome(
                bytesWritten = 0L,
                finalWidth = info.width,
                finalHeight = info.height,
                qualityUsed = null,
                targetHit = false,
                suggestion = Suggestion.TRY_LOSSY_FORMAT,
                wasDownsampledForMemory = false,
                didNotUpscale = false,
            )
        }

        return ImageReader.decode(source).use { image ->
            when (target) {
                is SizeTarget.Quality -> writeAt(
                    image = image,
                    format = format,
                    quality = target.value,
                    scale = 1f,
                    metadata = metadata,
                    source = source,
                    out = out,
                    outputFile = outputFile,
                    targetHit = true,
                    suggestion = Suggestion.NONE,
                )

                is SizeTarget.Bytes -> solveAndWrite(
                    image = image,
                    target = target,
                    format = format,
                    metadata = metadata,
                    allowDownscale = allowDownscale,
                    source = source,
                    out = out,
                    outputFile = outputFile,
                )
            }
        }
    }

    suspend fun resize(
        source: ByteSource,
        spec: ResizeSpec,
        format: ImageFormat,
        metadata: MetadataPolicy,
        out: OutputStream,
        outputFile: File? = null,
    ): ImageOutcome = ImageReader.decode(source).use { image ->
        val current = Size(image.bitmap.width, image.bitmap.height)
        val wanted = resolveDimensions(current, spec)
        val didNotUpscale = wanted == current && askedForMoreThanOriginal(current, spec)
        val scale = wanted.width.toFloat() / current.width

        writeAt(
            image = image,
            format = format,
            quality = QUALITY_MAX,
            scale = scale,
            metadata = metadata,
            source = source,
            out = out,
            outputFile = outputFile,
            targetHit = true,
            suggestion = Suggestion.NONE,
            didNotUpscale = didNotUpscale,
        )
    }

    suspend fun convert(
        source: ByteSource,
        format: ImageFormat,
        flattenColor: Int,
        metadata: MetadataPolicy,
        out: OutputStream,
        outputFile: File? = null,
    ): ImageOutcome = ImageReader.decode(source).use { image ->
        writeAt(
            image = image,
            format = format,
            quality = QUALITY_MAX,
            scale = 1f,
            metadata = metadata,
            source = source,
            out = out,
            outputFile = outputFile,
            targetHit = true,
            suggestion = Suggestion.NONE,
            flattenColor = flattenColor,
        )
    }

    private suspend fun solveAndWrite(
        image: DecodedImage,
        target: SizeTarget.Bytes,
        format: ImageFormat,
        metadata: MetadataPolicy,
        allowDownscale: Boolean,
        source: ByteSource,
        out: OutputStream,
        outputFile: File?,
    ): ImageOutcome {
        val sourceBytes = image.info.byteSize
        val reencodeRequired =
            image.info.mimeType != format.mimeType || image.wasDownsampledForMemory

        val solution = solveTargetSize(
            targetBytes = target.max,
            // Tamanho desconhecido nao pode virar "ja cabe": forca a busca.
            sourceBytes = if (sourceBytes >= 0) sourceBytes else Long.MAX_VALUE,
            reencodeRequired = reencodeRequired,
            probe = ImageEncoder.probeFor(
                image.bitmap,
                format,
                ImagingDefaults.DEFAULT_FLATTEN_COLOR,
            ),
            allowDownscale = allowDownscale,
        )

        return when (solution) {
            is TargetSolution.AlreadySmaller -> writeAt(
                image, format, QUALITY_MAX, 1f, metadata, source, out, outputFile,
                targetHit = true, suggestion = Suggestion.NONE,
            )

            is TargetSolution.Hit -> writeAt(
                image, format, solution.quality, solution.scale, metadata, source, out, outputFile,
                targetHit = true, suggestion = Suggestion.NONE,
            )

            // O app entrega o melhor que conseguiu e pergunta ao usuario.
            is TargetSolution.NeedsDownscale -> writeAt(
                image, format, ImagingDefaults.QUALITY_MIN, 1f, metadata, source, out, outputFile,
                targetHit = false, suggestion = Suggestion.NEEDS_DOWNSCALE,
            )

            is TargetSolution.Impossible -> writeAt(
                image, format, ImagingDefaults.QUALITY_AFTER_DOWNSCALE, solution.atScale,
                metadata, source, out, outputFile,
                targetHit = false, suggestion = Suggestion.NONE,
            )
        }
    }

    @Suppress("LongParameterList")
    private suspend fun writeAt(
        image: DecodedImage,
        format: ImageFormat,
        quality: Int,
        scale: Float,
        metadata: MetadataPolicy,
        source: ByteSource,
        out: OutputStream,
        outputFile: File?,
        targetHit: Boolean,
        suggestion: Suggestion,
        didNotUpscale: Boolean = false,
        flattenColor: Int? = ImagingDefaults.DEFAULT_FLATTEN_COLOR,
    ): ImageOutcome {
        val written = ImageEncoder.encode(
            bitmap = image.bitmap,
            format = format,
            quality = quality,
            scale = scale,
            flattenColor = flattenColor,
            out = out,
        )
        out.flush()

        val canKeepMetadata =
            metadata == MetadataPolicy.KEEP_ALL && outputFile != null && format == ImageFormat.JPEG
        if (canKeepMetadata) {
            source.openStream().use { stream ->
                ExifPolicy.copyMetadata(ExifInterface(stream), outputFile!!.absolutePath)
            }
        }

        return ImageOutcome(
            bytesWritten = written,
            finalWidth = scaledSide(image.bitmap.width, scale),
            finalHeight = scaledSide(image.bitmap.height, scale),
            qualityUsed = if (format.isLossy) quality else null,
            targetHit = targetHit,
            suggestion = suggestion,
            wasDownsampledForMemory = image.wasDownsampledForMemory,
            didNotUpscale = didNotUpscale,
        )
    }

    private fun scaledSide(value: Int, scale: Float): Int =
        if (scale == 1f) value else (value * scale).toInt().coerceAtLeast(1)

    /** true quando o usuario pediu algo MAIOR que o original — o app nao amplia. */
    private fun askedForMoreThanOriginal(source: Size, spec: ResizeSpec): Boolean = when (spec) {
        is ResizeSpec.Percent -> spec.value > 100
        is ResizeSpec.LongestSide -> spec.pixels > maxOf(source.width, source.height)
        is ResizeSpec.Pixels -> spec.width > source.width && spec.height > source.height
    }
}

/** Garante que o bitmap seja reciclado mesmo quando a operacao falha. */
private inline fun <T> DecodedImage.use(block: (DecodedImage) -> T): T =
    try {
        block(this)
    } finally {
        if (!bitmap.isRecycled) bitmap.recycle()
    }
