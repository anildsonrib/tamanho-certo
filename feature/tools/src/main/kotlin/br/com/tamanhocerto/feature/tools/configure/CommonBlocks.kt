package br.com.tamanhocerto.feature.tools.configure

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.tamanhocerto.core.model.ImageFormat
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import br.com.tamanhocerto.core.ui.component.ActionIconConvertArrows
import br.com.tamanhocerto.core.ui.component.ActionIconFolder
import br.com.tamanhocerto.core.ui.component.ActionIconFolderSize
import br.com.tamanhocerto.core.ui.component.ConvertArrowsHeight
import br.com.tamanhocerto.core.ui.component.ConvertArrowsWidth
import br.com.tamanhocerto.core.ui.component.NoticeKind
import br.com.tamanhocerto.core.ui.component.PrimaryAction
import br.com.tamanhocerto.core.ui.component.SecondaryAction
import br.com.tamanhocerto.core.ui.component.SizeChip
import br.com.tamanhocerto.feature.tools.R
import br.com.tamanhocerto.feature.tools.home.ToolId
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import br.com.tamanhocerto.core.ui.component.ToolIconFileImage
import br.com.tamanhocerto.core.ui.theme.ToolAccent

/**
 * Espacamento entre chips, igual nos dois eixos — o `gap` do `.sizechips`
 * no mockup (`docs/mockups/index.html`).
 */
private val ChipGap = 8.dp

/**
 * Quantas colunas a grade de chips tem. Tres porque os seis atalhos de
 * tamanho estao na ordem 100/300/500 KB e 1/2/5 MB: a quebra cai
 * exatamente entre as unidades, os KB em cima e os MB embaixo (pedido do
 * responsavel em 2026-08-27). Espelha `--sizechip-cols` no mockup.
 */
private const val CHIP_COLUMNS = 3

/** Metricas do cartao de resumo (mesmas de "Converter formato"). */
private val CardPadding = 16.dp
private val CardRadius = 16.dp

/**
 * Grade de chips com **colunas de largura igual**.
 *
 * Substituiu o `FlowRow` em 2026-08-27. O `FlowRow` dava a cada chip a
 * largura do proprio texto — "100 KB" saia maior que "1 MB" — e a segunda
 * linha nao alinhava com a primeira. Aqui cada celula tem `weight(1f)`
 * dentro da linha, entao todas medem o mesmo.
 *
 * Nao usa `LazyVerticalGrid` de proposito: as telas de configuracao ja
 * estao dentro de um `verticalScroll`, e um componente de rolagem
 * aninhado no mesmo eixo nao mede.
 */
@Composable
fun <T> ChipGrid(
    items: List<T>,
    modifier: Modifier = Modifier,
    columns: Int = CHIP_COLUMNS,
    itemContent: @Composable (T, Modifier) -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(ChipGap)) {
        items.chunked(columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(ChipGap)) {
                row.forEach { item -> itemContent(item, Modifier.weight(1f)) }
                // Celulas vazias da ultima linha: sem elas os chips da linha
                // incompleta esticariam e ficariam maiores que os de cima.
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/**
 * Resumo do arquivo de entrada das quatro telas genericas.
 *
 * Ate 2026-08-27 era texto solto (nome + tamanho, sem moldura). Virou o
 * mesmo cartao de "Converter formato" a pedido do responsavel: icone na
 * cor da ferramenta, titulo e linha de apoio. E literalmente o mesmo
 * `FileSummaryCard`, nao uma copia parecida.
 */
@Composable
fun InputSummaryBlock(input: InputSummary, accent: ToolAccent) {
    FileSummaryCard(input = input, accent = accent)
}

/**
 * Botao de acao das cinco telas de configuracao: com a area de arquivos
 * vazia mostra "Selecionar arquivos" e abre o seletor; com pelo menos um
 * arquivo, mostra o verbo da propria ferramenta e executa a operacao. Um
 * segundo botao "Limpar" (com confirmacao) so aparece com arquivo
 * selecionado (pedido do responsavel em 2026-08-25).
 *
 * Os dois icones do botao valem para as cinco ferramentas, como no
 * `actionBar()` do mockup (`docs/mockups/index.html`), que e um bloco so,
 * compartilhado por todas as telas: pasta sem arquivo selecionado, setas
 * opostas com arquivo. Antes de 2026-08-27 so "Converter formato" tinha
 * a pasta, e as setas nao existiam no Kotlin.
 */
@Composable
fun ToolActionBar(
    hasFiles: Boolean,
    actionLabel: String,
    actionEnabled: Boolean,
    onPickFiles: () -> Unit,
    onStart: () -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
    /** Nulo em "PDF para imagem", que recebe um PDF unico. */
    onAddFiles: (() -> Unit)? = null,
    containerColor: Color? = null,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        PrimaryAction(
            text = if (hasFiles) actionLabel else stringResource(R.string.action_select_files),
            onClick = if (hasFiles) onStart else onPickFiles,
            enabled = !hasFiles || actionEnabled,
            icon = if (hasFiles) ActionIconConvertArrows else ActionIconFolder,
            iconSize = if (hasFiles) {
                DpSize(ConvertArrowsWidth, ConvertArrowsHeight)
            } else {
                DpSize(ActionIconFolderSize, ActionIconFolderSize)
            },
            containerColor = containerColor,
            modifier = Modifier.fillMaxWidth(),
        )

        if (hasFiles) {
            var confirming by remember { mutableStateOf(false) }
            // "Adicionar arquivos" ao lado de "Limpar": ate 2026-08-27 nao
            // havia como acrescentar depois de escolher — com arquivo
            // selecionado o botao de cima virava o verbo, e a unica outra
            // saida era "Limpar", que descarta tudo. Achado do responsavel.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                if (onAddFiles != null) {
                    SecondaryAction(
                        text = stringResource(R.string.action_add_files),
                        onClick = onAddFiles,
                    )
                }
                SecondaryAction(
                    text = stringResource(R.string.action_clear),
                    onClick = { confirming = true },
                )
            }
            if (confirming) {
                AlertDialog(
                    onDismissRequest = { confirming = false },
                    title = { Text(stringResource(R.string.clear_confirm_title)) },
                    text = { Text(stringResource(R.string.clear_confirm_body)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                confirming = false
                                onClearAll()
                            },
                        ) {
                            Text(stringResource(R.string.clear_confirm_yes))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { confirming = false }) {
                            Text(stringResource(R.string.action_cancel))
                        }
                    },
                )
            }
        }
    }
}

@Composable
fun FormatPicker(
    label: String,
    selected: ImageFormat,
    onSelect: (ImageFormat) -> Unit,
    hints: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, style = MaterialTheme.typography.titleMedium)
        ChipGrid(ImageFormat.entries) { format, chipModifier ->
            SizeChip(
                label = format.name,
                selected = format == selected,
                onClick = { onSelect(format) },
                modifier = chipModifier,
            )
        }
        if (hints) {
            Text(
                text = stringResource(hintFor(selected)),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

internal fun hintFor(format: ImageFormat): Int = when (format) {
    ImageFormat.JPEG -> R.string.convert_jpeg_hint
    ImageFormat.PNG -> R.string.convert_png_hint
    ImageFormat.WEBP -> R.string.convert_webp_hint
}

/** Bloqueio de formulario ja resolvido em texto, com o argumento quando houver. */
@Composable
fun blockedText(blocked: Validation.Blocked): String =
    when (val arg = blocked.formatArg) {
        null -> stringResource(blocked.reason)
        else -> stringResource(blocked.reason, arg)
    }

fun NoticeKindUi.toComponent(): NoticeKind = when (this) {
    NoticeKindUi.INFO -> NoticeKind.INFO
    NoticeKindUi.WARNING -> NoticeKind.WARNING
    NoticeKindUi.ERROR -> NoticeKind.ERROR
}

/** Estado de exemplo dos `@Preview`. */
internal fun previewState(tool: ToolId = ToolId.COMPRESS) = ConfigureUiState(
    tool = tool,
    input = InputSummary(
        fileCount = 1,
        displayName = "foto.jpg",
        dimensionsText = "4032 × 3024 pixels",
        sizeText = "Tamanho atual: 3,8 MB",
        sizeBytes = 3_800_000,
    ),
    form = OperationForm.forTool(tool),
)

// Miniatura em 9:16, independente da proporcao real do arquivo — pedido do
// responsavel em 2026-08-25, para a grade ficar simetrica mesmo com fotos de
// proporcoes diferentes.
private val ThumbnailWidth = 64.dp
private val ThumbnailHeight = 114.dp // 64 * 16/9
private val ThumbnailBadgeSize = 24.dp

@Composable
fun FileSummaryCard(input: InputSummary, accent: ToolAccent) {
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
            // Icone de pagina com dobra no canto (referencia visual aprovada
            // em 2026-08-26, mockup enviado pelo responsavel) — sem o
            // emblema de destaque separado que havia antes no canto.
            Icon(
                imageVector = ToolIconFileImage,
                contentDescription = null,
                tint = accent.color,
                modifier = Modifier.size(26.dp),
            )
        }
        // Com mais de um arquivo selecionado, o cartao passa a resumir o
        // lote inteiro — nome/tamanho do primeiro arquivo, sozinhos, nao
        // representam a selecao (pedido do responsavel em 2026-08-25). Sem
        // nenhum arquivo, mostra "Nenhum arquivo selecionado" — a tela
        // inteira ja entra vazia, sem tela intermediaria.
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            val title = input.multiCountText ?: input.displayName
                ?: stringResource(R.string.input_empty_title)
            title.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 19.sp,
                )
            }
            val subtitle = input.multiCountText?.let { input.multiSizeText } ?: input.sizeText
            subtitle?.let {
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
fun SelectedFilesGrid(items: List<InputItem>, onRemove: (index: Int) -> Unit) {
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
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.width(ThumbnailWidth).height(ThumbnailHeight).clip(shape),
                )
            }
        }

        // Nome do arquivo, identificado desde a selecao — nao depende da
        // miniatura ja ter carregado (pedido do responsavel em 2026-08-25).
        // Faixa baixa (padding vertical minimo), so a altura do texto — a
        // opacidade volta ao valor original; o ajuste pedido foi de altura,
        // nao de opacidade (correcao em 2026-08-25).
        Text(
            text = item.displayName,
            color = Color.White,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                .background(Color.Black.copy(alpha = 0.45f))
                .padding(horizontal = 6.dp, vertical = 1.5.dp),
        )

        // Numero de selecao, dentro de um circulo pequeno que so cobre o
        // proprio numero — nao escurece a miniatura em volta (pedido do
        // responsavel em 2026-08-25, revertendo o "sem fundo" anterior por
        // falta de visibilidade).
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(22.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.Black.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number.toString(),
                color = Color.White,
                fontSize = 13.sp,
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
