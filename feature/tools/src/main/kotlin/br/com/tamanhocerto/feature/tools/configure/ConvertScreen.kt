package br.com.tamanhocerto.feature.tools.configure

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.tamanhocerto.core.model.ImageFormat
import br.com.tamanhocerto.core.ui.component.PrimaryAction
import br.com.tamanhocerto.core.ui.component.ToolIconFileImage
import br.com.tamanhocerto.core.ui.theme.TamanhoCertoTheme
import br.com.tamanhocerto.core.ui.theme.ToolAccent
import br.com.tamanhocerto.core.ui.theme.toolAccents
import br.com.tamanhocerto.feature.tools.R
import br.com.tamanhocerto.feature.tools.home.ToolId

// Metricas da referencia visual aprovada em 2026-08-25
// (`configure_convert_remodelado.html`).
private val CardPadding = 16.dp
private val CardRadius = 16.dp
private val ChipHeight = 54.dp
private val ChipGap = 9.dp

@Composable
fun ConvertScreen(
    state: ConfigureUiState,
    form: OperationForm.Convert,
    onFormChange: (OperationForm.Convert) -> Unit,
    onRemoveFile: (index: Int) -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accents = toolAccents(darkTheme = androidx.compose.foundation.isSystemInDarkTheme())

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(CardPadding),
        verticalArrangement = Arrangement.Center,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            FileSummaryCard(input = state.input, accent = accents[0])

            FormatCard(
                selected = form.format,
                onSelect = { onFormChange(form.copy(format = it)) },
                accents = accents,
            )

            // A pergunta da cor so aparece quando a transparencia seria perdida:
            // o app pergunta em vez de decidir sozinho (PRD secao 3.3). Fora do
            // escopo do redesenho — mantido com o visual anterior, simples.
            if (state.input.hasAlpha && form.format == ImageFormat.JPEG) {
                FlattenColorField(
                    selected = form.flattenColor,
                    onSelect = { onFormChange(form.copy(flattenColor = it)) },
                )
            }

            PrimaryAction(
                text = stringResource(R.string.action_continue),
                onClick = onStart,
                enabled = state.validation is Validation.Ok,
                modifier = Modifier.fillMaxWidth(),
            )

            // Miniaturas dos arquivos selecionados, na area vazia abaixo do
            // botao (pedido do responsavel em 2026-08-25). Alinhadas ao topo
            // desse espaco e centralizadas — nao ao centro vertical da tela,
            // para nao competir com o bloco principal acima.
            if (state.input.items.isNotEmpty()) {
                SelectedFilesGrid(items = state.input.items, onRemove = onRemoveFile)
            }
        }
    }
}

// Miniatura em 9:16, independente da proporcao real do arquivo — pedido do
// responsavel em 2026-08-25, para a grade ficar simetrica mesmo com fotos de
// proporcoes diferentes.
private val ThumbnailWidth = 64.dp
private val ThumbnailHeight = 114.dp // 64 * 16/9
private val ThumbnailBadgeSize = 24.dp

@Composable
private fun SelectedFilesGrid(items: List<InputItem>, onRemove: (index: Int) -> Unit) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items.forEachIndexed { index, item ->
            SelectedFileThumbnail(
                number = index + 1,
                item = item,
                onRemove = { onRemove(index) },
            )
        }
    }
}

@Composable
private fun SelectedFileThumbnail(number: Int, item: InputItem, onRemove: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .width(ThumbnailWidth)
            .height(ThumbnailHeight)
            .semantics(mergeDescendants = true) {
                contentDescription = "$number. ${item.displayName}"
            },
    ) {
        Box(
            modifier = Modifier
                .width(ThumbnailWidth)
                .height(ThumbnailHeight)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, shape)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape),
        ) {
            val bitmap = item.thumbnail
            if (bitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.width(ThumbnailWidth).height(ThumbnailHeight).clip(shape),
                )
            }
        }

        // Nome do arquivo, identificado desde a selecao — nao depende da
        // miniatura ja ter carregado (pedido do responsavel em 2026-08-25).
        // Faixa inferior com scrim para ficar legivel sobre qualquer foto.
        Text(
            text = item.displayName,
            color = androidx.compose.ui.graphics.Color.White,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.45f))
                .padding(horizontal = 6.dp, vertical = 4.dp),
        )

        // Numero de selecao, centralizado — no canto ele disputava espaco
        // com o botao de descartar (pedido do responsavel em 2026-08-25).
        // Scrim atras do numero para continuar legivel sobre foto clara.
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(ThumbnailBadgeSize)
                .clip(RoundedCornerShape(50))
                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number.toString(),
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        // Botao de descartar, no canto superior direito, em vermelho suave
        // (nao saturado) — pedido do responsavel em 2026-08-25.
        val removeDescription = stringResource(R.string.action_remove)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 6.dp, y = (-6).dp)
                .size(ThumbnailBadgeSize)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.errorContainer)
                .border(1.dp, MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(50))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = false),
                    onClick = onRemove,
                )
                .semantics { contentDescription = removeDescription },
            contentAlignment = Alignment.Center,
        ) {
            // Glifo de texto: evita adicionar a dependencia de
            // material-icons-extended (mesmo padrao de ReorderableImageList.kt).
            Text(
                text = "✕",
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun FileSummaryCard(input: InputSummary, accent: ToolAccent) {
    val shape = RoundedCornerShape(CardRadius)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 94.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer, shape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .padding(CardPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(accent.soft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = ToolIconFileImage,
                contentDescription = null,
                tint = accent.color,
                modifier = Modifier.size(26.dp),
            )
            // Pequeno detalhe de destaque no canto, com o anel na cor do
            // cartao (referencia visual: `box-shadow: 0 0 0 3px var(--surface)`).
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 6.dp, y = 6.dp)
                    .size(20.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(accent.color),
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            input.displayName?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 19.sp,
                )
            }
            input.sizeText?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.5.sp,
                    lineHeight = 16.sp,
                )
            }
        }
    }
}

@Composable
private fun FormatCard(
    selected: ImageFormat,
    onSelect: (ImageFormat) -> Unit,
    accents: List<ToolAccent>,
) {
    val shape = RoundedCornerShape(CardRadius)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer, shape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .padding(start = CardPadding, top = 18.dp, end = CardPadding, bottom = CardPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = stringResource(R.string.convert_target),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(ChipGap)) {
            // Ordem = ImageFormat.entries (JPEG, PNG, WEBP), mesma da
            // referencia; as tres cores de destaque sao as mesmas ja usadas
            // pelos cartoes da `home` (indices 3=azul, 2=verde, 4=roxo).
            formatChipAccent(ImageFormat.JPEG, accents).let { accent ->
                FormatChip(
                    label = ImageFormat.JPEG.name,
                    selected = selected == ImageFormat.JPEG,
                    accent = accent,
                    onClick = { onSelect(ImageFormat.JPEG) },
                    modifier = Modifier.weight(1f),
                )
            }
            formatChipAccent(ImageFormat.PNG, accents).let { accent ->
                FormatChip(
                    label = ImageFormat.PNG.name,
                    selected = selected == ImageFormat.PNG,
                    accent = accent,
                    onClick = { onSelect(ImageFormat.PNG) },
                    modifier = Modifier.weight(1f),
                )
            }
            formatChipAccent(ImageFormat.WEBP, accents).let { accent ->
                FormatChip(
                    label = ImageFormat.WEBP.name,
                    selected = selected == ImageFormat.WEBP,
                    accent = accent,
                    onClick = { onSelect(ImageFormat.WEBP) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Box(modifier = Modifier.heightIn(min = 32.dp), contentAlignment = Alignment.CenterStart) {
            Text(
                text = stringResource(hintFor(selected)),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            )
        }
    }
}

/** Azul para JPEG, verde para PNG, roxo para WEBP (referencia visual). */
private fun formatChipAccent(format: ImageFormat, accents: List<ToolAccent>): ToolAccent = when (format) {
    ImageFormat.JPEG -> accents[3]
    ImageFormat.PNG -> accents[2]
    ImageFormat.WEBP -> accents[4]
}

@Composable
private fun FormatChip(
    label: String,
    selected: Boolean,
    accent: ToolAccent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 140),
        label = "formatChipScale",
    )
    val shape = RoundedCornerShape(12.dp)
    val (background, border, textColor) = if (selected) {
        Triple(accent.soft, accent.color, accent.color)
    } else {
        Triple(
            MaterialTheme.colorScheme.surfaceContainerHigh,
            MaterialTheme.colorScheme.outlineVariant,
            MaterialTheme.colorScheme.onSurface,
        )
    }

    Box(
        modifier = modifier
            .height(ChipHeight)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .background(background, shape)
            .border(1.dp, border, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = accent.color),
                onClick = onClick,
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.1.sp,
        )
    }
}

@Composable
private fun FlattenColorField(selected: Int, onSelect: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.convert_flatten_label),
            style = MaterialTheme.typography.titleMedium,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                OperationForm.WHITE to R.string.convert_flatten_white,
                OperationForm.BLACK to R.string.convert_flatten_black,
                OperationForm.GRAY to R.string.convert_flatten_gray,
            ).forEach { (color, labelRes) ->
                br.com.tamanhocerto.core.ui.component.SizeChip(
                    label = stringResource(labelRes),
                    selected = selected == color,
                    onClick = { onSelect(color) },
                )
            }
        }
    }
}

@Preview(name = "Converter — com transparencia", showBackground = true)
@Composable
private fun ConvertPreview() {
    TamanhoCertoTheme(dynamicColor = false) {
        ConvertScreen(
            state = previewState(ToolId.CONVERT).copy(
                input = previewState().input.copy(hasAlpha = true),
            ),
            form = OperationForm.Convert(),
            onFormChange = {},
            onRemoveFile = {},
            onStart = {},
        )
    }
}
