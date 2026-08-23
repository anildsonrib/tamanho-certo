package br.com.tamanhocerto.feature.tools.configure

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tamanhocerto.core.files.PickerContracts
import br.com.tamanhocerto.core.model.ImageFormat
import br.com.tamanhocerto.core.ui.component.AppScaffold
import br.com.tamanhocerto.core.ui.component.SecondaryAction
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
    uris: List<Uri>,
    onBack: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConfigureViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val result by viewModel.result.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uris) { if (uris.isNotEmpty()) viewModel.onInputSelected(uris) }

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

    AppScaffold(
        title = stringResource(state.tool.titleRes()),
        modifier = modifier,
        navigationIcon = {
            SecondaryAction(text = stringResource(UiR.string.nav_back), onClick = onBack)
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
                    modifier = content,
                )

                is OperationForm.Resize -> ResizeScreen(
                    state = state,
                    form = form,
                    onFormChange = viewModel::onFormChanged,
                    onStart = { viewModel.onStart() },
                    modifier = content,
                )

                is OperationForm.ImagesToPdf -> ImagesToPdfScreen(
                    state = state,
                    form = form,
                    onFormChange = viewModel::onFormChanged,
                    onStart = { viewModel.onStart() },
                    modifier = content,
                )

                is OperationForm.PdfToImages -> PdfToImagesScreen(
                    state = state,
                    form = form,
                    onFormChange = viewModel::onFormChanged,
                    onStart = { viewModel.onStart() },
                    modifier = content,
                )

                is OperationForm.Convert -> ConvertScreen(
                    state = state,
                    form = form,
                    onFormChange = viewModel::onFormChanged,
                    onStart = { viewModel.onStart() },
                    modifier = content,
                )
            }
        }
    }
}

private const val DEFAULT_MIME = "application/octet-stream"

internal fun ToolId.titleRes(): Int = when (this) {
    ToolId.COMPRESS -> R.string.tool_compress_title
    ToolId.RESIZE -> R.string.tool_resize_title
    ToolId.IMAGES_TO_PDF -> R.string.tool_img2pdf_title
    ToolId.PDF_TO_IMAGES -> R.string.tool_pdf2img_title
    ToolId.CONVERT -> R.string.tool_convert_title
}
