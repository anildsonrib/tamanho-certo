package br.com.tamanhocerto.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Cores fora do esquema do Material 3 (UI-SPEC secao 1). */
@Immutable
data class ExtraColors(
    val warning: Color,
    val success: Color,
)

val LocalExtraColors = staticCompositionLocalOf {
    ExtraColors(warning = WarningLight, success = SuccessLight)
}

/** Escala de espacamento de 4 dp (UI-SPEC secao 1). */
object Spacing {
    val xs = 4
    val sm = 8
    val md = 16
    val lg = 24
    val xl = 32
}
