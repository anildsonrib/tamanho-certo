package br.com.tamanhocerto.core.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.tamanhocerto.core.ui.theme.HomePaletteDark
import br.com.tamanhocerto.core.ui.theme.HomePaletteLight
import br.com.tamanhocerto.core.ui.theme.ToolAccent

// Metricas da referencia visual aprovada em 2026-08-26 (`preview(1).html`),
// ajustadas no mesmo dia (pedido do responsavel): tamanho do cartao nao e
// mais fixo aqui — quem decide largura/altura (proporcao 3:4, cabendo as
// cinco ferramentas na tela sem rolagem) e o `HomeScreen`, via `modifier`.
private val CardRadius = 18.dp
private val IconBadgeSize = 44.dp
private val IconBadgeRadius = 10.dp
private val IconSize = 24.dp
private val ChipSize = 9.dp
private val ChevronSize = 11.dp
private const val PRESSED_SCALE = 0.985f
private const val PRESS_ANIMATION_MS = 150

/**
 * Cartao da tela inicial: icone com cor de destaque propria, titulo,
 * descricao e chevron indicando navegacao. Reproduz a referencia visual
 * aprovada pelo responsavel em 2026-08-26 (`preview(1).html`, estilo
 * CamScanner) — usado somente na `home`, por isso usa a paleta dedicada
 * `HomePalette` em vez do esquema dinamico do Material 3 (divergencia
 * registrada em `TASKS.md`).
 *
 * `horizontal = true` reproduz o quinto cartao ("Converter formato"),
 * que ocupa a largura toda com icone a esquerda, texto ao centro e
 * chevron a direita (`.card.full` no HTML de referencia).
 */
@Composable
fun ToolCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: ToolAccent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    horizontal: Boolean = false,
) {
    val palette = if (isSystemInDarkTheme()) HomePaletteDark else HomePaletteLight
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) PRESSED_SCALE else 1f,
        animationSpec = tween(durationMillis = PRESS_ANIMATION_MS),
        label = "toolCardScale",
    )
    val shape = RoundedCornerShape(CardRadius)

    val cardModifier = modifier
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clip(shape)
        .background(palette.surface, shape)
        .border(1.dp, palette.outline, shape)
        .clickable(
            interactionSource = interactionSource,
            indication = ripple(color = accent.color),
            onClick = onClick,
        )
        .semantics { contentDescription = "$title. $subtitle" }

    if (horizontal) {
        Row(
            modifier = cardModifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ToolIconBadge(icon, accent)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                CardTitle(title, palette.text)
                Spacer(Modifier.height(4.dp))
                CardSubtitle(subtitle, palette.textSoft)
            }
            Spacer(Modifier.width(8.dp))
            Chevron(tint = palette.textSoft)
        }
    } else {
        Box(modifier = cardModifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 14.dp),
            ) {
                ToolIconBadge(icon, accent)
                Spacer(Modifier.height(12.dp))
                CardTitle(title, palette.text)
                Spacer(Modifier.height(4.dp))
                CardSubtitle(subtitle, palette.textSoft)
            }
            Chevron(
                tint = palette.textSoft,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 14.dp, bottom = 14.dp),
            )
        }
    }
}

@Composable
private fun ToolIconBadge(icon: ImageVector, accent: ToolAccent) {
    Box(
        modifier = Modifier
            .size(IconBadgeSize)
            .clip(RoundedCornerShape(IconBadgeRadius))
            .background(accent.soft),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent.color,
            modifier = Modifier.size(IconSize),
        )
        // Reproduz o "recorte" colorido no canto inferior direito do
        // icon-box (::after do HTML): cantos assimetricos, encostado no
        // canto, sem preencher o quadrado inteiro.
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(ChipSize)
                .clip(
                    RoundedCornerShape(
                        topStart = 7.dp,
                        topEnd = 0.dp,
                        bottomEnd = 10.dp,
                        bottomStart = 0.dp,
                    ),
                )
                .background(accent.color),
        )
    }
}

@Composable
private fun CardTitle(title: String, color: Color) {
    Text(
        text = title,
        color = color,
        fontSize = 16.sp,
        fontWeight = FontWeight.ExtraBold,
        lineHeight = 19.sp,
        letterSpacing = (-0.2).sp,
    )
}

@Composable
private fun CardSubtitle(subtitle: String, color: Color) {
    Text(
        text = subtitle,
        color = color,
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 17.sp,
    )
}

/**
 * Seta discreta indicando navegacao, desenhada como no HTML de referencia
 * (duas bordas rotacionadas 45deg formando um "›"), em vez de um icone de
 * seta cheio — mantem o traco fino da referencia.
 */
@Composable
private fun Chevron(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(ChevronSize)) {
        val stroke = Stroke(
            width = 2.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        val path = Path().apply {
            moveTo(size.width * 0.25f, size.height * 0.12f)
            lineTo(size.width * 0.8f, size.height * 0.5f)
            lineTo(size.width * 0.25f, size.height * 0.88f)
        }
        drawPath(path, color = tint, style = stroke)
    }
}
