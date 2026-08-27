package br.com.tamanhocerto.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

private val LightScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    secondary = SecondaryLight,
    error = ErrorLight,
)

private val DarkScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    secondary = SecondaryDark,
    error = ErrorDark,
)

@Composable
fun TamanhoCertoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val scheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkScheme
        else -> LightScheme
    }
    val extras = if (darkTheme) {
        ExtraColors(warning = WarningDark, success = SuccessDark)
    } else {
        ExtraColors(warning = WarningLight, success = SuccessLight)
    }

    CompositionLocalProvider(LocalExtraColors provides extras) {
        MaterialTheme(
            colorScheme = scheme,
            typography = AppTypography,
            content = content,
        )
    }
}

/**
 * Reveste um trecho da arvore com a cor de destaque de uma ferramenta.
 *
 * Regra de identidade visual fixada pelo responsavel em 2026-08-26: a
 * paleta interna de cada ferramenta corresponde a cor do icone dela na
 * `home`. Ate 2026-08-27 isso era feito ponto a ponto — cada componente
 * recebia a cor por parametro — e por isso alcançava so o botao de acao,
 * o "Voltar" e os chips de "Converter formato". Tudo o mais (chip de
 * tamanho selecionado, aba, controle deslizante, chave, barra de
 * progresso) continuava no `primary` do esquema, que com
 * `dynamicColor = true` vem do papel de parede do aparelho — no emulador,
 * o lilas que aparecia no lugar do coral.
 *
 * Trocar o `primary` do esquema uma vez so, no topo da tela, e o
 * equivalente do `--accent` do mockup (`docs/mockups/index.html`), que e
 * uma variavel CSS trocada por ferramenta e herdada por todos os
 * seletores que a usam.
 *
 * `soft` entra como `secondaryContainer` porque e ele que o `FilterChip`
 * usa no estado selecionado — o mesmo par `--accent-soft` / `--accent`
 * do `.sizechip[data-selected]`.
 */
@Composable
fun ToolAccentTheme(accent: ToolAccent, content: @Composable () -> Unit) {
    val base = MaterialTheme.colorScheme
    MaterialTheme(
        colorScheme = base.copy(
            primary = accent.color,
            onPrimary = OnAccent,
            primaryContainer = accent.soft,
            onPrimaryContainer = accent.color,
            secondaryContainer = accent.soft,
            onSecondaryContainer = accent.color,
        ),
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content,
    )
}
