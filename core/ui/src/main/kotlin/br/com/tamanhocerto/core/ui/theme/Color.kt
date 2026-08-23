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
