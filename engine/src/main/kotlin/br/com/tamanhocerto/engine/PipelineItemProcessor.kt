package br.com.tamanhocerto.engine

import br.com.tamanhocerto.core.files.OutputNaming
import br.com.tamanhocerto.core.files.SeekableCopy
import br.com.tamanhocerto.core.files.WorkDir
import br.com.tamanhocerto.core.model.ByteSource
import br.com.tamanhocerto.core.model.FailureReason
import br.com.tamanhocerto.core.model.ImageFormat
import br.com.tamanhocerto.core.model.Operation
import br.com.tamanhocerto.core.model.OperationResult
import br.com.tamanhocerto.core.model.OutputRef
import br.com.tamanhocerto.core.model.RunOptions
import br.com.tamanhocerto.core.model.Stats
import br.com.tamanhocerto.core.model.Suggestion
import br.com.tamanhocerto.imaging.ImageOutcome
import br.com.tamanhocerto.imaging.ImagePipeline
import br.com.tamanhocerto.imaging.ImagingException
import br.com.tamanhocerto.pdf.PdfBuilder
import br.com.tamanhocerto.pdf.PdfException
import br.com.tamanhocerto.pdf.PdfRasterizer
import java.io.File
import javax.inject.Inject

/**
 * Implementacao real do [ItemProcessor]: liga o `:imaging` e o `:pdf` ao laco
 * do engine e grava as saidas em `cacheDir/work`.
 *
 * Sufixos e numeracao de pagina chegam prontos de fora (`suffixes`), porque
 * eles sao traduzidos e este modulo nao tem recurso de string
 * (ENGINE-SPEC secao 6).
 */
class PipelineItemProcessor @Inject constructor(
    private val workDir: WorkDir,
    private val seekableCopy: SeekableCopy,
) : ItemProcessor {

    /** Sufixos ja traduzidos, vindos da camada de UI. */
    data class Suffixes(
        val compressed: String = "-menor",
        val resized: String = "-redimensionada",
        val converted: String = "-convertida",
        val pdf: String = "-documento",
        val page: String = "-pagina",
        val fallbackBase: String = "arquivo",
    )

    var suffixes: Suffixes = Suffixes()

    private val usedNames = mutableSetOf<String>()

    override suspend fun process(
        index: Int,
        input: ByteSource,
        operation: Operation,
        options: RunOptions,
        onProgress: suspend (percent: Int?) -> Unit,
    ): OperationResult = try {
        // Nenhuma etapa da v1 tem progresso mensuravel item a item: a busca de
        // qualidade nao expoe fracao. Reportar null e dizer a verdade.
        onProgress(null)
        when (operation) {
            is Operation.Compress -> imageResult(
                input = input,
                format = operation.format,
                suffix = suffixes.compressed,
            ) { out, file ->
                ImagePipeline.compress(
                    source = input,
                    target = operation.target,
                    format = operation.format,
                    metadata = options.metadata,
                    allowDownscale = options.allowDownscale,
                    out = out,
                    outputFile = file,
                )
            }

            is Operation.Resize -> imageResult(
                input = input,
                format = operation.format,
                suffix = suffixes.resized,
            ) { out, file ->
                ImagePipeline.resize(
                    source = input,
                    spec = operation.spec,
                    format = operation.format,
                    metadata = options.metadata,
                    out = out,
                    outputFile = file,
                )
            }

            is Operation.Convert -> imageResult(
                input = input,
                format = operation.format,
                suffix = suffixes.converted,
            ) { out, file ->
                ImagePipeline.convert(
                    source = input,
                    format = operation.format,
                    flattenColor = operation.flattenColor,
                    metadata = options.metadata,
                    out = out,
                    outputFile = file,
                )
            }

            is Operation.ImagesToPdf -> imagesToPdf(input, operation, options)

            is Operation.PdfToImages -> pdfToImages(input, operation)
        }
    } catch (imaging: ImagingException) {
        OperationResult.Failed(imaging.reason)
    } catch (pdf: PdfException) {
        OperationResult.Failed(pdf.reason)
    }

    private suspend fun imageResult(
        input: ByteSource,
        format: ImageFormat,
        suffix: String,
        block: suspend (java.io.OutputStream, File) -> ImageOutcome,
    ): OperationResult {
        val file = allocate(input.displayName, suffix, format.extension)
        val outcome = file.outputStream().use { out -> block(out, file) }

        if (outcome.suggestion == Suggestion.TRY_LOSSY_FORMAT) {
            file.delete()
            return OperationResult.TargetMissed(
                output = OutputRef(file, file.name, format.mimeType),
                stats = statsOf(input, outcome),
                suggestion = Suggestion.TRY_LOSSY_FORMAT,
            )
        }

        val ref = OutputRef(file, file.name, format.mimeType)
        val stats = statsOf(input, outcome)
        return if (outcome.targetHit) {
            OperationResult.Success(ref, stats)
        } else {
            OperationResult.TargetMissed(ref, stats, outcome.suggestion ?: Suggestion.NONE)
        }
    }

    private suspend fun imagesToPdf(
        input: ByteSource,
        operation: Operation.ImagesToPdf,
        options: RunOptions,
    ): OperationResult {
        val file = allocate(input.displayName, suffixes.pdf, "pdf")
        val outcome = file.outputStream().use { out ->
            PdfBuilder.build(
                sources = listOf(input),
                spec = operation.page,
                target = operation.target,
                metadata = options.metadata,
                out = out,
            )
        }

        val ref = OutputRef(file, file.name, "application/pdf")
        val stats = Stats(
            bytesBefore = input.byteSize ?: -1L,
            bytesAfter = outcome.bytesWritten,
            qualityUsed = outcome.embedQualityUsed,
        )
        return if (outcome.targetHit) {
            OperationResult.Success(ref, stats)
        } else {
            OperationResult.TargetMissed(ref, stats, outcome.suggestion ?: Suggestion.NONE)
        }
    }

    private suspend fun pdfToImages(
        input: ByteSource,
        operation: Operation.PdfToImages,
    ): OperationResult {
        // PdfRenderer exige arquivo local pesquisavel; a copia so acontece aqui.
        val localPdf = seekableCopy.of(input)
        var first: File? = null
        var total = 0L

        PdfRasterizer.rasterize(
            file = localPdf,
            range = operation.pages,
            density = operation.density,
            format = operation.format,
            quality = PAGE_QUALITY,
        ) { index, bytes ->
            val page = allocate(
                originalName = input.displayName,
                suffix = suffixes.page,
                extension = operation.format.extension,
                pageNumber = index + 1,
            )
            page.writeBytes(bytes)
            total += bytes.size
            if (first == null) first = page
        }

        val output = first ?: return OperationResult.Failed(FailureReason.UNKNOWN)
        return OperationResult.Success(
            output = OutputRef(output, output.name, operation.format.mimeType),
            stats = Stats(bytesBefore = input.byteSize ?: -1L, bytesAfter = total),
        )
    }

    private suspend fun allocate(
        originalName: String?,
        suffix: String,
        extension: String,
        pageNumber: Int? = null,
    ): File {
        val name = OutputNaming.nameFor(
            originalName = originalName,
            suffix = suffix,
            extension = extension,
            existingNames = usedNames,
            fallbackBase = suffixes.fallbackBase,
            pageNumber = pageNumber,
        )
        usedNames += name
        return workDir.allocate(name)
    }

    private fun statsOf(input: ByteSource, outcome: ImageOutcome) = Stats(
        bytesBefore = input.byteSize ?: -1L,
        bytesAfter = outcome.bytesWritten,
        finalWidth = outcome.finalWidth,
        finalHeight = outcome.finalHeight,
        qualityUsed = outcome.qualityUsed,
        wasDownsampledForMemory = outcome.wasDownsampledForMemory,
        didNotUpscale = outcome.didNotUpscale,
    )

    private companion object {
        /** Qualidade das paginas rasterizadas; a densidade e que decide o tamanho. */
        const val PAGE_QUALITY = 90
    }
}
