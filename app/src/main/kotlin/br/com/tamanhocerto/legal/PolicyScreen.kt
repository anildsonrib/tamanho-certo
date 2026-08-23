package br.com.tamanhocerto.legal

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.tamanhocerto.R
import br.com.tamanhocerto.core.ui.component.AppScaffold
import br.com.tamanhocerto.core.ui.component.SecondaryAction
import br.com.tamanhocerto.core.ui.R as UiR

/**
 * Renderiza `res/raw/privacy_policy.md`, copia de `docs/PRIVACY-POLICY.md`.
 * Nao abre navegador e nao faz requisicao de rede (SKELETON-SPEC secao 9).
 */
@Composable
fun PolicyScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val resources = LocalResources.current
    val text = remember {
        resources.openRawResource(R.raw.privacy_policy)
            .bufferedReader()
            .use { it.readText() }
    }

    AppScaffold(
        title = stringResource(UiR.string.nav_privacy),
        modifier = modifier,
        navigationIcon = {
            SecondaryAction(text = stringResource(UiR.string.nav_back), onClick = onBack)
        },
    ) { padding ->
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
        )
    }
}
