package br.com.tamanhocerto.feature.tools.configure

import android.content.Context
import android.net.Uri
import android.text.format.Formatter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tamanhocerto.core.files.ContentByteSource
import br.com.tamanhocerto.core.model.ByteSource
import br.com.tamanhocerto.core.model.ImageFormat
import br.com.tamanhocerto.core.model.Operation
import br.com.tamanhocerto.core.model.OperationResult
import br.com.tamanhocerto.core.model.PageRange
import br.com.tamanhocerto.core.model.PageSpec
import br.com.tamanhocerto.core.model.ResizeSpec
import br.com.tamanhocerto.core.model.RunOptions
import br.com.tamanhocerto.core.model.MetadataPolicy
import br.com.tamanhocerto.core.model.SizeTarget
import br.com.tamanhocerto.core.model.Suggestion
import br.com.tamanhocerto.engine.JobEvent
import br.com.tamanhocerto.core.model.RewardGate
import br.com.tamanhocerto.engine.BatchPolicy
import br.com.tamanhocerto.engine.OperationEngine
import br.com.tamanhocerto.feature.tools.R
import br.com.tamanhocerto.feature.tools.home.ToolId
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Uma fonte de verdade por tela. Toda formatacao de texto acontece aqui: o
 * Composable recebe pronto (UI-SPEC secao 10b.4).
 */
@HiltViewModel
class ConfigureViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val engine: OperationEngine,
    private val rewardGate: RewardGate,
    private val fileSaver: br.com.tamanhocerto.core.files.FileSaver,
    private val fileSharer: br.com.tamanhocerto.core.files.FileSharer,
    savedState: SavedStateHandle,
) : ViewModel() {

    private val tool: ToolId =
        ToolId.fromRouteArg(savedState.get<String>(ARG_OPERATION_ID)) ?: ToolId.COMPRESS

    private val _state = MutableStateFlow(
        ConfigureUiState(tool = tool, form = OperationForm.forTool(tool)),
    )
    val state: StateFlow<ConfigureUiState> = _state.asStateFlow()

    private val _result = MutableStateFlow(ResultUiState())
    val result: StateFlow<ResultUiState> = _result.asStateFlow()

    private var sources: List<ByteSource> = emptyList()
    private var runningJob: Job? = null
    private var lastTargetBytes: Long? = null

    /**
     * `append = true` soma a selecao atual em vez de substitui-la — e o
     * "Adicionar arquivos" novo em 2026-08-27. Antes, escolher de novo
     * descartava o que ja estava selecionado, e a unica outra saida era
     * "Limpar".
     */
    fun onInputSelected(uris: List<Uri>, append: Boolean = false) {
        viewModelScope.launch {
            val picked = uris.map { ContentByteSource.from(context.contentResolver, it) }
            sources = if (append) sources + picked else picked
            val first = sources.firstOrNull()
            val items = sources.map { InputItem(displayName = it.displayName ?: "") }
            val totalBytes = sources.map { it.byteSize }
                .takeIf { it.isNotEmpty() && it.all { size -> size != null } }
                ?.sumOf { it!! }
            _state.update { current ->
                current.copy(
                    input = InputSummary(
                        fileCount = sources.size,
                        displayName = first?.displayName,
                        sizeBytes = first?.byteSize,
                        sizeText = first?.byteSize?.let {
                            context.getString(R.string.input_size, formatSize(it))
                        },
                        multiCountText = sources.size.takeIf { it > 1 }?.let {
                            context.getString(R.string.input_multi_count, it)
                        },
                        multiSizeText = totalBytes?.takeIf { sources.size > 1 }?.let {
                            context.getString(R.string.input_multi_size, formatSize(it))
                        },
                        items = items,
                    ),
                )
            }
            // "Comprimir" e "Redimensionar" perderam o seletor de formato em
            // 2026-08-27 (pedido do responsavel: a saida mantem a extensao
            // original; quem quiser trocar vai para "Converter formato"). O
            // campo `format` do formulario continua existindo — passa a ser
            // preenchido aqui, a partir do arquivo, em vez de pela tela.
            adoptSourceFormat(uris.firstOrNull())

            revalidate()

            // Miniaturas: lista reordenavel (imagem->PDF) e grade de
            // selecionados (converter desde 2026-08-25; comprimir e
            // redimensionar desde 2026-08-27). Decodificadas depois, para
            // nao atrasar a entrada na tela (UI-SPEC secao 4.3).
            if (tool != ToolId.PDF_TO_IMAGES) loadThumbnails()
        }
    }

    /**
     * Deduz o formato de saida do arquivo de entrada. Le o MIME do
     * `ContentResolver` e cai na extensao do nome quando o provedor nao
     * informa. Sem os dois, mantem o que ja estava — ausencia de dado
     * significa "nao sei", nunca um padrao permissivo.
     */
    private fun adoptSourceFormat(uri: Uri?) {
        if (tool != ToolId.COMPRESS && tool != ToolId.RESIZE) return
        val mime = uri?.let { context.contentResolver.getType(it) }
        val name = sources.firstOrNull()?.displayName
        val format = when {
            mime == "image/png" -> ImageFormat.PNG
            mime == "image/webp" -> ImageFormat.WEBP
            mime == "image/jpeg" -> ImageFormat.JPEG
            name?.endsWith(".png", ignoreCase = true) == true -> ImageFormat.PNG
            name?.endsWith(".webp", ignoreCase = true) == true -> ImageFormat.WEBP
            name?.endsWith(".jpg", ignoreCase = true) == true -> ImageFormat.JPEG
            name?.endsWith(".jpeg", ignoreCase = true) == true -> ImageFormat.JPEG
            // HEIC e afins nao tem codificador de saida: JPEG e o destino
            // natural, e e o que o app ja fazia por padrao.
            else -> ImageFormat.JPEG
        }
        _state.update { current ->
            when (val f = current.form) {
                is OperationForm.Compress -> current.copy(form = f.copy(format = format))
                is OperationForm.Resize -> current.copy(form = f.copy(format = format))
                else -> current
            }
        }
    }

    private suspend fun loadThumbnails() {
        val snapshot = sources
        for ((index, source) in snapshot.withIndex()) {
            if (sources !== snapshot) return // a selecao mudou; descarta o resto
            val bitmap = decodeThumbnail(source) ?: continue
            _state.update { current ->
                val items = current.input.items.toMutableList()
                if (index !in items.indices) return@update current
                items[index] = items[index].copy(thumbnail = bitmap)
                current.copy(input = current.input.copy(items = items))
            }
        }
    }

    /**
     * Miniatura simples via `BitmapFactory`, sem correcao de orientacao EXIF
     * — e so uma previa da lista, e nao afeta o resultado final, que passa
     * pelo `:imaging` de verdade. `:feature:tools` nao depende de `:imaging`
     * (ARCHITECTURE.md secao 2), por isso o decode aqui e direto da API do
     * Android, e nao pelo pipeline.
     */
    private suspend fun decodeThumbnail(source: ByteSource): ImageBitmap? =
        decodeDownsampled(THUMBNAIL_MAX_PX) { source.openStream() }

    /** Previa da tela de resultado: maior que a miniatura da lista. */
    private suspend fun decodePreview(file: File): ImageBitmap? =
        decodeDownsampled(PREVIEW_MAX_PX) { file.inputStream() }

    private suspend fun decodePreview(source: ByteSource): ImageBitmap? =
        decodeDownsampled(PREVIEW_MAX_PX) { source.openStream() }

    private suspend fun decodeDownsampled(
        maxPx: Int,
        open: suspend () -> java.io.InputStream,
    ): ImageBitmap? = withContext(Dispatchers.IO) {
        runCatching {
            val bounds = android.graphics.BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            open().use { android.graphics.BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null

            var sample = 1
            while (bounds.outWidth / sample > maxPx || bounds.outHeight / sample > maxPx) {
                sample *= 2
            }
            val options = android.graphics.BitmapFactory.Options().apply { inSampleSize = sample }
            val bitmap = open().use { android.graphics.BitmapFactory.decodeStream(it, null, options) }
                ?: return@runCatching null
            bitmap.asImageBitmap()
        }.getOrNull()
    }

    /** Reordena `sources` e a lista visivel juntas: sao a mesma ordem. */
    fun onReorderImages(from: Int, to: Int) {
        if (from == to || from !in sources.indices || to !in sources.indices) return
        sources = sources.toMutableList().apply { add(to, removeAt(from)) }
        _state.update { current ->
            val items = current.input.items.toMutableList().apply { add(to, removeAt(from)) }
            current.copy(input = current.input.copy(items = items))
        }
    }

    fun onRemoveImage(index: Int) {
        if (index !in sources.indices) return
        sources = sources.toMutableList().apply { removeAt(index) }
        val totalBytes = sources.map { it.byteSize }
            .takeIf { it.isNotEmpty() && it.all { size -> size != null } }
            ?.sumOf { it!! }
        _state.update { current ->
            val items = current.input.items.toMutableList().apply { removeAt(index) }
            current.copy(
                input = current.input.copy(
                    fileCount = items.size,
                    items = items,
                    multiCountText = items.size.takeIf { it > 1 }?.let {
                        context.getString(R.string.input_multi_count, it)
                    },
                    multiSizeText = totalBytes?.takeIf { items.size > 1 }?.let {
                        context.getString(R.string.input_multi_size, formatSize(it))
                    },
                ),
            )
        }
        revalidate()
    }

    /**
     * Limpa a selecao inteira (botao "Limpar", com confirmacao na tela —
     * pedido do responsavel em 2026-08-25). A tela volta ao estado sem
     * arquivo, com o mesmo formulario de antes.
     */
    fun onClearAll() {
        sources = emptyList()
        _state.update { current -> current.copy(input = InputSummary()) }
        revalidate()
    }

    fun onFormChanged(form: OperationForm) {
        _state.update { it.copy(form = form) }
        revalidate()
    }

    /** Start executa o que o formulario descreve. O gate de anuncio e da fase 7. */
    fun onStart(allowDownscale: Boolean = false) {
        val current = _state.value
        if (current.validation !is Validation.Ok || sources.isEmpty()) return

        val operation = current.form.toOperation() ?: return
        val options = RunOptions(
            allowDownscale = allowDownscale,
            metadata = if (current.form.keepsMetadata()) {
                MetadataPolicy.KEEP_ALL
            } else {
                MetadataPolicy.STRIP_ALL
            },
        )
        lastTargetBytes = (current.form as? OperationForm.Compress)?.targetBytes
        suggestionWasDownscale = false
        totalBefore = 0L
        totalAfter = 0L

        runningJob = viewModelScope.launch {
            // Um arquivo por vez e sempre livre; o lote passa pelo gate. Quem
            // decide e a UI, nunca o engine (ENGINE-SPEC secao 9).
            if (BatchPolicy.requiresReward(sources.size) && !rewardGate.requestUnlock()) {
                // Recusa volta para a configuracao, sem erro.
                return@launch
            }

            val items = mutableListOf<ResultItem>()
            _state.update {
                it.copy(phase = Phase.Running(percent = null, total = sources.size))
            }

            engine.run(sources, operation, options).collect { event ->
                when (event) {
                    is JobEvent.Started -> _state.update {
                        it.copy(phase = Phase.Running(null, 0, event.total))
                    }

                    is JobEvent.Progress -> _state.update { state ->
                        val running = state.phase as? Phase.Running
                        state.copy(
                            phase = Phase.Running(
                                percent = event.percent,
                                index = event.index,
                                total = event.total,
                                currentName = sources.getOrNull(event.index)?.displayName,
                                anyItemDone = running?.anyItemDone ?: false,
                            ),
                        )
                    }

                    is JobEvent.ItemDone -> {
                        items += event.result.toItem(event.name)
                        _state.update { state ->
                            val running = state.phase as? Phase.Running
                            state.copy(
                                phase = Phase.Running(
                                    percent = running?.percent,
                                    index = event.index,
                                    total = running?.total ?: sources.size,
                                    currentName = event.name,
                                    anyItemDone = true,
                                ),
                            )
                        }
                    }

                    is JobEvent.Finished -> {
                        _result.value = buildResult(items, event)
                        _state.update { it.copy(phase = Phase.Done) }
                    }
                }
            }
        }
    }

    /** Cancelar preserva o que ja terminou (ENGINE-SPEC secao 9). */
    fun onCancel() {
        runningJob?.cancel()
        runningJob = null
        _state.update { it.copy(phase = Phase.Idle) }
    }

    /** Resposta do usuario ao diálogo de alvo nao atingido. */
    fun onDownscaleAccepted() {
        _result.update { it.copy(downscalePrompt = null) }
        onStart(allowDownscale = true)
    }

    fun onDownscaleDeclined() {
        _result.update { it.copy(downscalePrompt = null) }
    }

    /** Gravacao pelo SAF: quem escolhe onde o arquivo vai e o usuario (D18). */
    fun saveTo(resolver: android.content.ContentResolver, from: java.io.File, to: Uri) {
        viewModelScope.launch { fileSaver.save(resolver, from, to) }
    }

    /** Compartilhamento por FileProvider, com URI temporaria. */
    fun share(context: Context) {
        val items = _result.value.items.mapNotNull { it.file }
        if (items.isEmpty()) return
        val mime = _result.value.items.firstOrNull()?.mimeType ?: FALLBACK_MIME
        val intent = fileSharer.shareIntent(items, mime)
            .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun onRedo() {
        _result.value = ResultUiState()
        _state.update { it.copy(phase = Phase.Idle) }
    }

    // --- validacao e formatacao ----------------------------------------

    private fun revalidate() {
        _state.update { current ->
            current.copy(
                validation = current.validate(),
                notice = current.noticeFor(),
            )
        }
    }

    private fun ConfigureUiState.validate(): Validation {
        val originalSize = input.sizeBytes
        return when (val form = form) {
            is OperationForm.Compress -> when {
                form.qualityMode -> Validation.Ok
                form.targetBytes == null || form.targetBytes <= 0 ->
                    Validation.Blocked(R.string.invalid_target_zero)

                // So bloqueia com UM arquivo. `originalSize` e o tamanho do
                // PRIMEIRO da lista: num lote, barrar por ele impediria de
                // comprimir os outros, que podem ser bem maiores. Achado do
                // responsavel em 2026-08-27 — com tres arquivos, o app
                // bloqueava os tres por causa do primeiro. Quem ja cabe no
                // limite sai intacto: o motor resolve com `AlreadySmaller`.
                input.fileCount <= 1 && originalSize != null &&
                    form.targetBytes >= originalSize ->
                    Validation.Blocked(R.string.invalid_target_too_big, formatSize(originalSize))

                else -> Validation.Ok
            }

            is OperationForm.PdfToImages -> {
                val pages = input.pageCount
                val from = form.from.toIntOrNull()
                val to = form.to.toIntOrNull()
                when {
                    form.allPages -> Validation.Ok
                    from == null || to == null || from < 1 || from > to ->
                        Validation.Blocked(R.string.invalid_range, pages ?: 1)

                    pages != null && to > pages ->
                        Validation.Blocked(R.string.invalid_range, pages)

                    else -> Validation.Ok
                }
            }

            is OperationForm.ImagesToPdf -> when {
                input.fileCount < 1 -> Validation.Blocked(R.string.invalid_no_images)
                else -> Validation.Ok
            }

            // Passa a valer quando a grade de miniaturas ganhou o botao de
            // descartar (2026-08-25): sem isso, remover todos os arquivos
            // deixava "Continuar" habilitado sem nada para processar.
            is OperationForm.Convert -> when {
                input.fileCount < 1 -> Validation.Blocked(R.string.invalid_no_images)
                else -> Validation.Ok
            }

            else -> Validation.Ok
        }
    }

    /**
     * PNG com alvo em bytes so avisa: a acao continua habilitada, porque a
     * escolha e do usuario (UI-SPEC secao 4.1).
     */
    private fun ConfigureUiState.noticeFor(): NoticeState? {
        val form = form as? OperationForm.Compress ?: return null
        val pngWithByteTarget = form.format == ImageFormat.PNG && !form.qualityMode
        return if (pngWithByteTarget) {
            NoticeState(
                message = R.string.notice_png_lossless,
                kind = NoticeKindUi.WARNING,
                // Sem botao de acao desde 2026-08-27: o app nao propoe mais
                // trocar de formato. Mantem a extensao e reduz as dimensoes,
                // avisando antes — decisao do responsavel.
            )
        } else {
            null
        }
    }

    private fun OperationForm.toOperation(): Operation? {
        return when (this) {
        is OperationForm.Compress -> Operation.Compress(
            target = if (qualityMode) {
                SizeTarget.Quality(quality)
            } else {
                SizeTarget.Bytes(targetBytes ?: return null)
            },
            format = format,
        )

        is OperationForm.Resize -> Operation.Resize(spec = toSpec() ?: return null, format = format)

        is OperationForm.Convert -> Operation.Convert(format = format, flattenColor = flattenColor)

        is OperationForm.ImagesToPdf -> Operation.ImagesToPdf(
            page = PageSpec(pageSize, orientation, margin),
            target = targetBytes?.let(SizeTarget::Bytes),
        )

        is OperationForm.PdfToImages -> Operation.PdfToImages(
            pages = if (allPages) {
                PageRange.All
            } else {
                PageRange.Interval(from.toIntOrNull() ?: 1, to.toIntOrNull() ?: 1)
            },
            density = density,
            format = format,
        )
        }
    }

    private fun OperationForm.Resize.toSpec(): ResizeSpec? {
        return when (mode) {
        ResizeMode.PIXELS -> ResizeSpec.Pixels(
            width = width.toIntOrNull() ?: return null,
            height = height.toIntOrNull() ?: return null,
            lockAspect = lockAspect,
        )

        ResizeMode.PERCENT -> ResizeSpec.Percent(percent)
        ResizeMode.LONGEST_SIDE -> ResizeSpec.LongestSide(longestSide.toIntOrNull() ?: return null)
        }
    }

    private fun OperationForm.keepsMetadata(): Boolean =
        (this as? OperationForm.Compress)?.keepMetadata == true

    private fun OperationResult.toItem(name: String): ResultItem = when (this) {
        is OperationResult.Success -> {
            totalBefore += stats.bytesBefore.coerceAtLeast(0)
            totalAfter += stats.bytesAfter.coerceAtLeast(0)
            ResultItem(
            name = name,
            beforeAfterText = beforeAfter(stats.bytesBefore, stats.bytesAfter),
            state = ItemState.SUCCESS,
            file = output.file,
            mimeType = output.mimeType,
            )
        }

        is OperationResult.TargetMissed -> {
            totalBefore += stats.bytesBefore.coerceAtLeast(0)
            totalAfter += stats.bytesAfter.coerceAtLeast(0)
            if (suggestsDownscale()) suggestionWasDownscale = true
            ResultItem(
            name = name,
            beforeAfterText = beforeAfter(stats.bytesBefore, stats.bytesAfter),
            state = ItemState.WARNING,
            file = output.file,
            mimeType = output.mimeType,
            )
        }

        is OperationResult.Failed -> ResultItem(
            name = name,
            beforeAfterText = "",
            state = ItemState.FAILED,
            errorMessage = reason.messageRes(),
        )
    }

    private suspend fun buildResult(
        items: List<ResultItem>,
        finished: JobEvent.Finished,
    ): ResultUiState {
        val first = items.firstOrNull()
        val missed = items.any { it.state == ItemState.WARNING }
        val allFailed = items.isNotEmpty() && items.all { it.state == ItemState.FAILED }

        val needsDownscale = lastTargetBytes != null && suggestionWasDownscale

        // Previa: so com um item de saida em imagem. Imagem->PDF nao tem
        // previa aqui — o resultado e um PDF, e este modulo nao conhece
        // `:pdf` (ARCHITECTURE.md secao 2).
        val previewFile = first?.file
        val isImageOutput = first?.mimeType?.startsWith("image/") == true
        val previewBitmap = if (items.size == 1 && isImageOutput && previewFile != null) {
            decodePreview(previewFile)
        } else {
            null
        }
        val originalBitmap = if (previewBitmap != null) {
            sources.firstOrNull()?.let { decodePreview(it) }
        } else {
            null
        }

        return ResultUiState(
            items = items,
            beforeAfterText = first?.beforeAfterText,
            reductionText = if (totalBefore > 0 && totalAfter in 1..totalBefore) {
                context.getString(
                    R.string.result_reduction,
                    reductionPercent(totalBefore, totalAfter),
                )
            } else {
                null
            },
            batchSummary = if (items.size > 1) {
                context.resources.getQuantityString(
                    R.plurals.result_batch_summary,
                    finished.succeeded,
                    finished.succeeded,
                    items.size,
                )
            } else {
                null
            },
            banner = when {
                allFailed -> ResultBanner.ERROR
                missed -> ResultBanner.WARNING
                else -> ResultBanner.SUCCESS
            },
            bannerText = when {
                allFailed -> null
                missed && lastTargetBytes != null -> context.getString(
                    R.string.notice_target_missed,
                    formatSize(lastTargetBytes ?: 0),
                    formatSize(totalAfter),
                )

                else -> context.getString(R.string.result_success)
            },
            downscalePrompt = if (needsDownscale) {
                DownscalePrompt(formatSize(lastTargetBytes ?: 0))
            } else {
                null
            },
            previewBitmap = previewBitmap,
            originalBitmap = originalBitmap,
        )
    }

    /** Preenchidos a cada ItemDone; usados para montar a tela de resultado. */
    private var suggestionWasDownscale: Boolean = false
    private var totalBefore: Long = 0L
    private var totalAfter: Long = 0L

    private fun beforeAfter(before: Long, after: Long): String =
        context.getString(R.string.result_before_after, formatSize(before), formatSize(after))

    /**
     * `Formatter` ja respeita o idioma do aparelho: nunca formatar tamanho a
     * mao (STRINGS.md secao 16).
     */
    private fun formatSize(bytes: Long): String =
        Formatter.formatShortFileSize(context, bytes.coerceAtLeast(0))

    fun reductionPercent(before: Long, after: Long): Int =
        if (before <= 0) 0 else (100.0 * (before - after) / before).roundToInt()

    companion object {
        const val ARG_OPERATION_ID = "operationId"
        private const val FALLBACK_MIME = "application/octet-stream"

        /** Lado maior da miniatura da lista reordenavel, em pixels. */
        private const val THUMBNAIL_MAX_PX = 160

        /** Lado maior da previa na tela de resultado, em pixels. */
        private const val PREVIEW_MAX_PX = 640
    }
}

/** Marca o resultado que pede o diálogo de reducao de dimensoes. */
internal fun OperationResult.suggestsDownscale(): Boolean =
    this is OperationResult.TargetMissed && suggestion == Suggestion.NEEDS_DOWNSCALE
