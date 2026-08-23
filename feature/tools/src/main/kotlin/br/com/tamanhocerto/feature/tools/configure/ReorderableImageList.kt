package br.com.tamanhocerto.feature.tools.configure

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import br.com.tamanhocerto.core.ui.theme.TamanhoCertoTheme
import br.com.tamanhocerto.feature.tools.R

/**
 * Lista reordenavel por arrasto, com miniatura, posicao e botao de remover
 * (UI-SPEC secao 4.3).
 *
 * Implementacao propria e sem lista virtualizada de proposito: o teto de
 * itens do lote e pequeno (`EngineDefaults.MAX_BATCH_ITEMS`), a tela inteira
 * ja rola num `Column` só, e uma `LazyColumn` aninhada em `verticalScroll`
 * exigiria altura travada. O arrasto comeca por toque longo na alca, para nao
 * competir com a rolagem da tela.
 */
@Composable
fun ReorderableImageList(
    items: List<InputItem>,
    onMove: (from: Int, to: Int) -> Unit,
    onRemove: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    // Topo (em px) de cada linha, preenchido via onGloballyPositioned.
    val rowTops = remember { mutableStateMapOf<Int, Float>() }
    var rowHeightPx by remember { mutableFloatStateOf(0f) }

    Column(modifier = modifier.fillMaxWidth()) {
        items.forEachIndexed { index, item ->
            val isDragged = draggedIndex == index
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(if (isDragged) 1f else 0f)
                    .graphicsLayer { translationY = if (isDragged) dragOffsetY else 0f }
                    .onGloballyPositioned { coordinates ->
                        rowTops[index] = coordinates.positionInParent().y
                        rowHeightPx = coordinates.size.height.toFloat()
                    }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = DRAG_HANDLE_GLYPH,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .semantics { contentDescription = "" }
                        .pointerInput(item, items.size) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    draggedIndex = index
                                    dragOffsetY = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffsetY += dragAmount.y
                                },
                                onDragEnd = {
                                    val from = draggedIndex
                                    if (from != null && rowHeightPx > 0f) {
                                        val draggedTop = (rowTops[from] ?: 0f) + dragOffsetY
                                        val draggedCenter = draggedTop + rowHeightPx / 2f
                                        val target = rowTops.entries
                                            .minByOrNull {
                                                kotlin.math.abs(it.value + rowHeightPx / 2f - draggedCenter)
                                            }?.key ?: from
                                        if (target != from) onMove(from, target)
                                    }
                                    draggedIndex = null
                                    dragOffsetY = 0f
                                },
                                onDragCancel = {
                                    draggedIndex = null
                                    dragOffsetY = 0f
                                },
                            )
                        },
                )

                ImageThumbnail(item)

                Text(
                    text = "${index + 1}. ${item.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )

                val removeDescription = stringResource(R.string.action_remove)
                Text(
                    text = REMOVE_GLYPH,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .semantics { contentDescription = removeDescription }
                        .clickableRemove { onRemove(index) },
                )
            }
        }
    }
}

@Composable
private fun ImageThumbnail(item: InputItem) {
    val bitmap = item.thumbnail
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp)),
            )
        }
    }
}

/** Glifos de texto: evitam adicionar a dependencia de material-icons-extended. */
private const val DRAG_HANDLE_GLYPH = "≡"
private const val REMOVE_GLYPH = "✕"

private fun Modifier.clickableRemove(onClick: () -> Unit): Modifier =
    this.then(
        Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = { onClick() })
        },
    )

@Preview(name = "Lista reordenavel", showBackground = true)
@Composable
private fun ReorderableImageListPreview() {
    TamanhoCertoTheme(dynamicColor = false) {
        ReorderableImageList(
            items = listOf(
                InputItem("foto1.jpg"),
                InputItem("foto2.jpg"),
                InputItem("foto3.jpg"),
            ),
            onMove = { _, _ -> },
            onRemove = {},
        )
    }
}
