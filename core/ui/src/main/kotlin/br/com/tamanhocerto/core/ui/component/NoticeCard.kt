package br.com.tamanhocerto.core.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.tamanhocerto.core.ui.theme.LocalExtraColors

/** Variantes de aviso. A cor nunca e o unico sinal: o texto sempre diz o que houve. */
enum class NoticeKind { INFO, WARNING, ERROR }

@Composable
fun NoticeCard(
    text: String,
    kind: NoticeKind,
    modifier: Modifier = Modifier,
) {
    val extras = LocalExtraColors.current
    val accent: Color = when (kind) {
        NoticeKind.INFO -> MaterialTheme.colorScheme.secondary
        NoticeKind.WARNING -> extras.warning
        NoticeKind.ERROR -> MaterialTheme.colorScheme.error
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Text(
            text = text,
            color = accent,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp),
        )
    }
}
