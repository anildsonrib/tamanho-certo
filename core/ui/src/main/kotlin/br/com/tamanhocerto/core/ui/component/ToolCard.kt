package br.com.tamanhocerto.core.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

// Alvo de toque minimo (UI-SPEC secao 1, 48dp): a altura fixa abaixo ja excede.
private val CardHeight = 176.dp
private val CardRadius = 15.dp
private val IconBadgeSize = 38.dp
private val IconBadgeRadius = 5.dp
private val IconSize = 22.dp
private val ChipSize = 13.dp
private val ChipRadius = 3.dp
private const val PRESSED_SCALE = 0.965f
private const val PRESS_ANIMATION_MS = 150

/**
 * Cartao da tela inicial: icone com cor de destaque propria, titulo e
 * subtitulo. Reproduz a referencia visual aprovada pelo responsavel em
 * 2026-08-25 (`tamanho_certo_home_centralizado.html`) — usado somente na
 * `home`, por isso usa a paleta dedicada `HomePalette` em vez do esquema
 * dinamico do Material 3 (divergencia registrada em `TASKS.md`).
 */
@Composable
fun ToolCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: ToolAccent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
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

    Column(
        modifier = modifier
            .height(CardHeight)
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
            .padding(16.dp),
    ) {
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
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 5.dp, y = 5.dp)
                    .size(ChipSize)
                    .clip(RoundedCornerShape(ChipRadius))
                    .background(accent.color.copy(alpha = 0.92f)),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            color = palette.text,
            fontSize = 15.5.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 19.sp,
            letterSpacing = (-0.15).sp,
        )
        Spacer(Modifier.height(7.dp))
        Text(
            text = subtitle,
            color = palette.textSoft,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 16.sp,
        )
    }
}
