package br.com.tamanhocerto.core.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta de fallback quando nao ha cores dinamicas (UI-SPEC secao 1).
internal val PrimaryLight = Color(0xFF2C6E49)
internal val OnPrimaryLight = Color(0xFFFFFFFF)
internal val SecondaryLight = Color(0xFF3A5A78)
internal val ErrorLight = Color(0xFFB3261E)

internal val PrimaryDark = Color(0xFF7CC49B)
internal val OnPrimaryDark = Color(0xFF00341C)
internal val SecondaryDark = Color(0xFFA5C8E8)
internal val ErrorDark = Color(0xFFF2B8B5)

// warning e success nao existem no Material 3 e entram como extensao do
// esquema (UI-SPEC secao 1): sao necessarios para o aviso de "alvo nao
// atingido", que nao e erro nem sucesso.
val WarningLight = Color(0xFF8A5A00)
val WarningDark = Color(0xFFF0C070)
val SuccessLight = Color(0xFF2C6E49)
val SuccessDark = Color(0xFF7CC49B)

/**
 * Paleta exclusiva da tela `home`, fora do esquema dinamico do Material 3.
 * Reproduz a referencia visual aprovada pelo responsavel em 2026-08-25
 * (`tamanho_certo_home_centralizado.html`) e so e usada pelo `ToolCard` e
 * pelo cabecalho/rodape de `HomeScreen` — nenhuma outra tela do app depende
 * dela. Registrado como divergencia da UI-SPEC secao 1 em `TASKS.md`.
 */
data class HomePalette(
    val background: Color,
    val surface: Color,
    val text: Color,
    val textSoft: Color,
    val outline: Color,
    val footer: Color,
)

val HomePaletteLight = HomePalette(
    background = Color(0xFFF7F8FB),
    surface = Color(0xFFFFFFFF),
    text = Color(0xFF171A21),
    textSoft = Color(0xFF707784),
    outline = Color(0xFFE6E8EE),
    footer = Color(0xFF5F7898),
)

val HomePaletteDark = HomePalette(
    background = Color(0xFF111318),
    surface = Color(0xFF1B1E24),
    text = Color(0xFFF1F2F4),
    textSoft = Color(0xFFB7BBC3),
    outline = Color(0xFF363A42),
    footer = Color(0xFFAAC7E0),
)

/** Cor de destaque de cada cartao da `home`: identidade visual por funcao. */
data class ToolAccent(val color: Color, val soft: Color)

private val Accent1 = Color(0xFFFF694F)
private val Accent2 = Color(0xFFE79A39)
private val Accent3 = Color(0xFF6FB35B)
private val Accent4 = Color(0xFF4D85D6)
private val Accent5 = Color(0xFFAF4CA8)

private val ToolAccentsLight = listOf(
    ToolAccent(Accent1, Color(0xFFFFE3DC)),
    ToolAccent(Accent2, Color(0xFFFFF0D6)),
    ToolAccent(Accent3, Color(0xFFE5F3DF)),
    ToolAccent(Accent4, Color(0xFFDFEBFB)),
    ToolAccent(Accent5, Color(0xFFF1DFF0)),
)

// O tom do "soft" muda no escuro; o tom principal do accent se mantem, como
// na referencia (so as variaveis --accent-N-soft sao redefinidas no
// @media prefers-color-scheme: dark).
private val ToolAccentsDark = listOf(
    ToolAccent(Accent1, Color(0xFF4D2A24)),
    ToolAccent(Accent2, Color(0xFF493621)),
    ToolAccent(Accent3, Color(0xFF2E4128)),
    ToolAccent(Accent4, Color(0xFF26374F)),
    ToolAccent(Accent5, Color(0xFF482C40)),
)

fun toolAccents(darkTheme: Boolean): List<ToolAccent> =
    if (darkTheme) ToolAccentsDark else ToolAccentsLight
