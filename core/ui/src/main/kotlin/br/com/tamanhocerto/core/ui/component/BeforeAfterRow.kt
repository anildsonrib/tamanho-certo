package br.com.tamanhocerto.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import br.com.tamanhocerto.core.ui.theme.LocalExtraColors

/**
 * Antes -> depois, ja formatado pelo ViewModel: a formatacao de tamanho e de
 * porcentagem nao mora no Composable (UI-SPEC secao 10b.4).
 */
@Composable
fun BeforeAfterRow(
    beforeAfterText: String,
    reductionText: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = beforeAfterText, style = MaterialTheme.typography.titleMedium)
        if (reductionText != null) {
            Text(
                text = reductionText,
                style = MaterialTheme.typography.bodyMedium,
                color = LocalExtraColors.current.success,
            )
        }
    }
}
