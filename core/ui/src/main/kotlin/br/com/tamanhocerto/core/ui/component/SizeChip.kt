package br.com.tamanhocerto.core.ui.component

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Atalho de tamanho (100 KB, 500 KB...). O estado selecionado e anunciado
 * pelo leitor de tela: cor nunca e o unico sinal (UI-SPEC secao 9).
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
        label = { Text(label) },
        modifier = modifier
            .heightIn(min = 48.dp)
            .semantics { this.selected = selected },
    )
}
