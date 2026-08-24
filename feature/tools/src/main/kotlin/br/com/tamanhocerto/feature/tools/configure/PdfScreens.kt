package br.com.tamanhocerto.feature.tools.configure

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.tamanhocerto.core.model.PageMargin
import br.com.tamanhocerto.core.model.PageOrientation
import br.com.tamanhocerto.core.model.PageSize
import br.com.tamanhocerto.core.model.RenderDensity
import br.com.tamanhocerto.core.ui.component.NoticeCard
import br.com.tamanhocerto.core.ui.component.NoticeKind
import br.com.tamanhocerto.core.ui.component.PrimaryAction
import br.com.tamanhocerto.core.ui.component.SizeChip
import br.com.tamanhocerto.core.ui.theme.TamanhoCertoTheme
import br.com.tamanhocerto.feature.tools.R
import br.com.tamanhocerto.feature.tools.home.ToolId

@Composable
fun ImagesToPdfScreen(
    state: ConfigureUiState,
    form: OperationForm.ImagesToPdf,
    onFormChange: (OperationForm.ImagesToPdf) -> Unit,
    onMoveImage: (from: Int, to: Int) -> Unit,
    onRemoveImage: (index: Int) -> Unit,
    onStart: () -> Unit,
    onPickFiles: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Entra direto no layout, sem o seletor do sistema abrir sozinho
        // (pedido do responsavel em 2026-08-25, mesmo comportamento aplicado
        // a "Converter formato" antes).
        if (state.input.fileCount < 1) {
            EmptySelectionBlock(subtitleRes = R.string.img2pdf_empty_subtitle, onPickFiles = onPickFiles)
            return@Column
        }

        InputSummaryBlock(state.input)
        Text(
            text = stringResource(R.string.pdf_reorder_hint),
            style = MaterialTheme.typography.bodySmall,
        )
        ReorderableImageList(
            items = state.input.items,
            onMove = onMoveImage,
            onRemove = onRemoveImage,
        )

        ChipGroup(
            label = stringResource(R.string.pdf_page_size),
            options = listOf(
                PageSize.A4 to R.string.pdf_page_a4,
                PageSize.Letter to R.string.pdf_page_letter,
                PageSize.FitImage to R.string.pdf_page_fit,
            ),
            selected = form.pageSize,
            onSelect = { onFormChange(form.copy(pageSize = it)) },
        )

        ChipGroup(
            label = stringResource(R.string.pdf_orientation),
            options = listOf(
                PageOrientation.AUTO to R.string.pdf_orientation_auto,
                PageOrientation.PORTRAIT to R.string.pdf_orientation_portrait,
                PageOrientation.LANDSCAPE to R.string.pdf_orientation_landscape,
            ),
            selected = form.orientation,
            onSelect = { onFormChange(form.copy(orientation = it)) },
        )

        ChipGroup(
            label = stringResource(R.string.pdf_margin),
            options = listOf(
                PageMargin.NONE to R.string.pdf_margin_none,
                PageMargin.SMALL to R.string.pdf_margin_small,
                PageMargin.MEDIUM to R.string.pdf_margin_medium,
            ),
            selected = form.margin,
            onSelect = { onFormChange(form.copy(margin = it)) },
        )

        Text(
            text = stringResource(R.string.pdf_target),
            style = MaterialTheme.typography.titleMedium,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SizeShortcuts.values.forEach { bytes ->
                SizeChip(
                    label = sizeShortcutLabel(bytes),
                    selected = form.targetBytes == bytes,
                    onClick = {
                        val next = if (form.targetBytes == bytes) null else bytes
                        onFormChange(form.copy(targetBytes = next))
                    },
                )
            }
        }

        (state.validation as? Validation.Blocked)?.let {
            NoticeCard(text = blockedText(it), kind = NoticeKind.ERROR)
        }

        PrimaryAction(
            text = stringResource(R.string.action_continue),
            onClick = onStart,
            enabled = state.validation is Validation.Ok,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun PdfToImagesScreen(
    state: ConfigureUiState,
    form: OperationForm.PdfToImages,
    onFormChange: (OperationForm.PdfToImages) -> Unit,
    onStart: () -> Unit,
    onPickFiles: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Entra direto no layout, sem o seletor do sistema abrir sozinho
        // (pedido do responsavel em 2026-08-25, mesmo comportamento aplicado
        // a "Converter formato" antes).
        if (state.input.fileCount < 1) {
            EmptySelectionBlock(subtitleRes = R.string.pdf2img_empty_subtitle, onPickFiles = onPickFiles)
            return@Column
        }

        InputSummaryBlock(state.input)

        Text(
            text = stringResource(R.string.raster_pages),
            style = MaterialTheme.typography.titleMedium,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SizeChip(
                label = stringResource(R.string.raster_pages_all),
                selected = form.allPages,
                onClick = { onFormChange(form.copy(allPages = true)) },
            )
            SizeChip(
                label = stringResource(R.string.raster_pages_range),
                selected = !form.allPages,
                onClick = { onFormChange(form.copy(allPages = false)) },
            )
        }

        if (!form.allPages) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = form.from,
                    onValueChange = { onFormChange(form.copy(from = it)) },
                    label = { Text(stringResource(R.string.raster_from)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = form.to,
                    onValueChange = { onFormChange(form.copy(to = it)) },
                    label = { Text(stringResource(R.string.raster_to)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        ChipGroup(
            label = stringResource(R.string.raster_quality),
            options = listOf(
                RenderDensity.LOW to R.string.raster_quality_low,
                RenderDensity.MEDIUM to R.string.raster_quality_medium,
                RenderDensity.HIGH to R.string.raster_quality_high,
            ),
            selected = form.density,
            onSelect = { onFormChange(form.copy(density = it)) },
        )

        FormatPicker(
            label = stringResource(R.string.compress_format_label),
            selected = form.format,
            onSelect = { onFormChange(form.copy(format = it)) },
        )

        (state.validation as? Validation.Blocked)?.let {
            NoticeCard(text = blockedText(it), kind = NoticeKind.ERROR)
        }

        PrimaryAction(
            text = stringResource(R.string.action_continue),
            onClick = onStart,
            enabled = state.validation is Validation.Ok,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Mesmos atalhos da tela de comprimir, com a unidade visivel (UI-SPEC 4.3). */
@Composable
private fun sizeShortcutLabel(bytes: Long): String {
    val kb = 1024L
    val mb = kb * kb
    return if (bytes >= mb) {
        "${bytes / mb} " + stringResource(R.string.compress_unit_mb)
    } else {
        "${bytes / kb} " + stringResource(R.string.compress_unit_kb)
    }
}

@Composable
private fun <T> ChipGroup(
    label: String,
    options: List<Pair<T, Int>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (value, labelRes) ->
                SizeChip(
                    label = stringResource(labelRes),
                    selected = value == selected,
                    onClick = { onSelect(value) },
                )
            }
        }
    }
}

@Preview(name = "Imagem para PDF — lista reordenavel", showBackground = true)
@Composable
private fun ImagesToPdfPreview() {
    TamanhoCertoTheme(dynamicColor = false) {
        val state = previewState(ToolId.IMAGES_TO_PDF)
        ImagesToPdfScreen(
            state = state.copy(
                input = state.input.copy(
                    fileCount = 3,
                    items = listOf(
                        InputItem("foto1.jpg"),
                        InputItem("foto2.jpg"),
                        InputItem("foto3.jpg"),
                    ),
                ),
            ),
            form = OperationForm.ImagesToPdf(),
            onFormChange = {},
            onMoveImage = { _, _ -> },
            onRemoveImage = {},
            onStart = {},
            onPickFiles = {},
        )
    }
}

@Preview(name = "PDF para imagem — intervalo invalido", showBackground = true)
@Composable
private fun PdfToImagesBlockedPreview() {
    TamanhoCertoTheme(dynamicColor = false) {
        PdfToImagesScreen(
            state = previewState(ToolId.PDF_TO_IMAGES).copy(
                validation = Validation.Blocked(R.string.invalid_range, 4),
            ),
            form = OperationForm.PdfToImages(allPages = false, from = "2", to = "9"),
            onFormChange = {},
            onStart = {},
            onPickFiles = {},
        )
    }
}
