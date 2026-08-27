package br.com.tamanhocerto.core.ui.component

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Raio de `--sizechip` no mockup (`docs/mockups/index.html`). O
 * `FilterChip` do Material 3 usa 8dp por padrao; explicito aqui para ter
 * um ponto de controle, como ja acontece com `ActionCornerRadius`.
 */
private val ChipCornerRadius = 8.dp

/**
 * Atalho de tamanho (100 KB, 500 KB...). O estado selecionado e anunciado
 * pelo leitor de tela: cor nunca e o unico sinal (UI-SPEC secao 9).
 *
 * Selecionado, usa o par `secondaryContainer`/`onSecondaryContainer` mais
 * uma borda da propria cor — o `.sizechip[data-selected]` do mockup, que
 * e `background: var(--accent-soft); border-color: var(--accent); color:
 * var(--accent)`. Quem define esse par e o `ToolAccentTheme` em volta da
 * tela, entao o chip fica na cor da ferramenta sem receber parametro.
 *
 * A altura minima continua em 48dp (alvo de toque, garantia 5 da fase
 * 7.5) e nao nos 36px do mockup, que e menor que o minimo acessivel.
 */
@Composable
fun SizeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        shape = RoundedCornerShape(ChipCornerRadius),
        label = {
            Text(
                text = label,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            )
        },
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            selectedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer,
            selectedBorderWidth = 1.dp,
        ),
        modifier = modifier
            .heightIn(min = 48.dp)
            .semantics { this.selected = selected },
    )
}
