package br.com.tamanhocerto.core.ui.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/** UI-SPEC secao 1: alvo de toque minimo, sem excecao. */
private const val MIN_TOUCH_TARGET_DP = 48

/**
 * Altura do botao primario: `--action-height` do mockup
 * (`docs/mockups/index.html`). Maior que os 48dp do alvo de toque minimo,
 * que continua valendo como piso do botao secundario.
 */
private val ActionHeight = 54.dp

/** Tamanho padrao do icone dentro do botao, quando ele existe. */
private val DefaultActionIconSize = DpSize(18.dp, 18.dp)

/**
 * Raio dos cantos dos botoes de acao. `24dp` e metade da altura minima
 * (48dp), ou seja, a capsula — o mesmo formato que o Material 3 aplica por
 * padrao, agora explicito para poder ser ajustado num lugar so.
 *
 * Vale para os botoes primarios de todo o app (as cinco ferramentas, a
 * tela de resultado e a de processamento). Diminua para cantos menos
 * arredondados: 16, 12, 8… 0 deixa o botao quadrado.
 */
private val ActionCornerRadius = 10.dp

/**
 * `icon` e aditivo (default `null` preserva todo uso existente): so a
 * tela "Converter formato" passa um icone, para reproduzir a referencia
 * visual aprovada em 2026-08-26 (mockup enviado pelo responsavel).
 */
@Composable
fun PrimaryAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    iconSize: DpSize = DefaultActionIconSize,
    containerColor: Color? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(ActionCornerRadius),
        colors = if (containerColor != null) {
            ButtonDefaults.buttonColors(containerColor = containerColor)
        } else {
            ButtonDefaults.buttonColors()
        },
        modifier = modifier.heightIn(min = ActionHeight),
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(iconSize))
            Spacer(Modifier.width(8.dp))
        }
        Text(text)
    }
}

@Composable
fun SecondaryAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    contentColor: Color? = null,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        colors = if (contentColor != null) {
            ButtonDefaults.textButtonColors(contentColor = contentColor)
        } else {
            ButtonDefaults.textButtonColors()
        },
        modifier = modifier.heightIn(min = MIN_TOUCH_TARGET_DP.dp),
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(text)
    }
}
