package br.com.tamanhocerto.core.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

/**
 * Previa com toque longo para comparar com o original (UI-SPEC secao 1).
 * Sem bitmap, mostra o texto de apoio — nunca uma area vazia sem explicacao.
 */
@Composable
fun ImagePreview(
    result: ImageBitmap?,
    original: ImageBitmap?,
    contentDescription: String,
    emptyLabel: String,
    modifier: Modifier = Modifier,
) {
    var showingOriginal by remember { mutableStateOf(false) }
    val shown = if (showingOriginal && original != null) original else result

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp, max = 320.dp)
            .pointerInput(original) {
                detectTapGestures(
                    onPress = {
                        showingOriginal = true
                        tryAwaitRelease()
                        showingOriginal = false
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        if (shown == null) {
            Text(text = emptyLabel, style = MaterialTheme.typography.bodyMedium)
        } else {
            Image(
                bitmap = shown,
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
