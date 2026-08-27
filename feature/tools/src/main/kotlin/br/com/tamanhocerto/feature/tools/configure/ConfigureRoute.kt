package br.com.tamanhocerto.feature.tools.configure

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tamanhocerto.core.files.PickerContracts
import br.com.tamanhocerto.core.model.ImageFormat
import br.com.tamanhocerto.core.ui.component.AppScaffold
import br.com.tamanhocerto.core.ui.component.NavIconBackChevron
import br.com.tamanhocerto.core.ui.component.SecondaryAction
import br.com.tamanhocerto.core.ui.theme.ToolAccent
import br.com.tamanhocerto.core.ui.theme.toolAccents
import br.com.tamanhocerto.feature.tools.R
import br.com.tamanhocerto.feature.tools.home.ToolId
import br.com.tamanhocerto.feature.tools.result.ResultScreen
import br.com.tamanhocerto.core.ui.R as UiR

/**
 * UNICO ponto com Hilt e navegacao. Trocar o visual mexe nas telas; nunca
 * aqui (UI-SPEC secao 10b.1).
 */
@Composable
fun ConfigureRoute(
    onBack: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConfigureViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val result by viewModel.result.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // As cinco ferramentas entram direto no proprio layout e pedem o arquivo
    // por dentro dele (pedido do responsavel em 2026-08-25, revertendo o
    // gesto unico de UI-SPEC secao 3 para todas — comecou so em Converter).
    val pickImagesLauncher = rememberLauncherForActivityResult(
        PickerContracts.pickImages(),
    ) { picked -> if (picked.isNotEmpty()) viewModel.onInputSelected(picked) }

    val pickPdfLauncher = rememberLauncherForActivityResult(
        PickerContracts.openPdf(),
    ) { picked -> if (picked != null) viewModel.onInputSelected(listOf(picked)) }

    val saveLauncher = rememberLauncherForActivityResult(
        contract = PickerContracts.createDocument(
            result.items.firstOrNull()?.mimeType ?: DEFAULT_MIME,
        ),
    ) { destination ->
        val source = result.items.firstOrNull()?.file
        if (destination != null && source != null) {
            viewModel.saveTo(context.contentResolver, source, destination)
        }
    }

    // Titulo centralizado so na tela de Converter (referencia visual aprovada
    // em 2026-08-25, `configure_convert_remodelado.html`) — as outras quatro
    // telas de configuracao mantem o `TopAppBar` padrao, sem alteracao.
    val centerTitle = state.tool == ToolId.CONVERT
    AppScaffold(
        title = stringResource(state.tool.titleRes()),
        modifier = modifier,
        navigationIcon = {
            SecondaryAction(
                text = stringResource(UiR.string.nav_back),
                onClick = onBack,
                // A seta so existe no visual novo de Converter; a cor,
                // essa vale para as cinco (paleta interna = icone da
                // ferramenta na `home`).
                icon = if (centerTitle) NavIconBackChevron else null,
                contentColor = state.tool.accent().color,
            )
        },
        centerTitle = centerTitle,
        titleStyle = if (centerTitle) {
            androidx.compose.ui.text.TextStyle(
                fontSize = 22.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                letterSpacing = (-0.25).sp,
            )
        } else {
            null
        },
    ) { padding ->
        val content = Modifier.padding(padding)

        when (val phase = state.phase) {
            is Phase.Running -> ProcessingContent(
                phase = phase,
                onCancel = viewModel::onCancel,
                modifier = content,
            )

            Phase.Done -> ResultScreen(
                state = result,
                onSave = { saveLauncher.launch(result.items.firstOrNull()?.name.orEmpty()) },
                onShare = { viewModel.share(context) },
                onRedo = viewModel::onRedo,
                onHome = onHome,
                onDownscaleAccept = viewModel::onDownscaleAccepted,
                onDownscaleDecline = viewModel::onDownscaleDeclined,
                modifier = content,
            )

            Phase.Idle -> when (val form = state.form) {
                is OperationForm.Compress -> CompressScreen(
                    state = state,
                    form = form,
                    onFormChange = viewModel::onFormChanged,
                    onSwitchToJpeg = {
                        viewModel.onFormChanged(form.copy(format = ImageFormat.JPEG))
                    },
                    onStart = { viewModel.onStart() },
                    onPickFiles = { pickImagesLauncher.launch(imagePickRequest()) },
                    onClearAll = viewModel::onClearAll,
                    modifier = content,
                )

                is OperationForm.Resize -> ResizeScreen(
                    state = state,
                    form = form,
                    onFormChange = viewModel::onFormChanged,
                    onStart = { viewModel.onStart() },
                    onPickFiles = { pickImagesLauncher.launch(imagePickRequest()) },
                    onClearAll = viewModel::onClearAll,
                    modifier = content,
                )

                is OperationForm.ImagesToPdf -> ImagesToPdfScreen(
                    state = state,
                    form = form,
                    onFormChange = viewModel::onFormChanged,
                    onMoveImage = viewModel::onReorderImages,
                    onRemoveImage = viewModel::onRemoveImage,
                    onStart = { viewModel.onStart() },
                    onPickFiles = { pickImagesLauncher.launch(imagePickRequest()) },
                    onClearAll = viewModel::onClearAll,
                    modifier = content,
                )

                is OperationForm.PdfToImages -> PdfToImagesScreen(
                    state = state,
                    form = form,
                    onFormChange = viewModel::onFormChanged,
                    onStart = { viewModel.onStart() },
                    onPickFiles = { pickPdfLauncher.launch(PickerContracts.PDF_MIME_FILTER) },
                    onClearAll = viewModel::onClearAll,
                    modifier = content,
                )

                is OperationForm.Convert -> ConvertScreen(
                    state = state,
                    form = form,
                    onFormChange = viewModel::onFormChanged,
                    onRemoveFile = viewModel::onRemoveImage,
                    onStart = { viewModel.onStart() },
                    onPickFiles = { pickImagesLauncher.launch(imagePickRequest()) },
                    onClearAll = viewModel::onClearAll,
                    modifier = content,
                )
            }
        }
    }
}

private const val DEFAULT_MIME = "application/octet-stream"

private fun imagePickRequest() =
    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)

internal fun ToolId.titleRes(): Int = when (this) {
    ToolId.COMPRESS -> R.string.tool_compress_title
    ToolId.RESIZE -> R.string.tool_resize_title
    ToolId.IMAGES_TO_PDF -> R.string.tool_img2pdf_title
    ToolId.PDF_TO_IMAGES -> R.string.tool_pdf2img_title
    ToolId.CONVERT -> R.string.tool_convert_title
}

/**
 * Accent da ferramenta = o mesmo do cartao dela na `home` (pedido do
 * responsavel em 2026-08-26: a paleta interna da tela corresponde ao icone
 * que a pessoa tocou, para a identidade nao mudar no meio do caminho).
 * `ToolId` esta na mesma ordem de `TOOLS` (`HomeScreen`) e de
 * `toolAccents()`, por isso o `ordinal` indexa direto.
 */
@Composable
internal fun ToolId.accent(): ToolAccent =
    toolAccents(darkTheme = isSystemInDarkTheme())[ordinal]

/** Rotulo do botao de acao quando ha arquivo selecionado — verbo por ferramenta. */
internal fun ToolId.actionRes(): Int = when (this) {
    ToolId.COMPRESS -> R.string.compress_action
    ToolId.RESIZE -> R.string.resize_action
    ToolId.IMAGES_TO_PDF -> R.string.img2pdf_action
    ToolId.PDF_TO_IMAGES -> R.string.pdf2img_action
    ToolId.CONVERT -> R.string.convert_action
}
