package br.com.tamanhocerto.legal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.tamanhocerto.BuildConfig
import br.com.tamanhocerto.R
import br.com.tamanhocerto.core.ui.component.AppScaffold
import br.com.tamanhocerto.core.ui.component.SecondaryAction
import br.com.tamanhocerto.core.ui.R as UiR

/** Contato de privacidade publicado na politica (LGPD art. 9o, IV). */
private const val PRIVACY_CONTACT = "anildson.rib@gmail.com"

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onPrivacyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        title = stringResource(UiR.string.nav_about),
        modifier = modifier,
        navigationIcon = {
            SecondaryAction(text = stringResource(UiR.string.nav_back), onClick = onBack)
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(R.string.about_package, BuildConfig.APPLICATION_ID),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(R.string.about_contact, PRIVACY_CONTACT),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(R.string.about_local_processing),
                style = MaterialTheme.typography.bodyMedium,
            )
            SecondaryAction(
                text = stringResource(R.string.about_privacy_link),
                onClick = onPrivacyClick,
            )
        }
    }
}
