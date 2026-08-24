package br.com.tamanhocerto.core.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

/**
 * Estrutura comum de tela: barra de topo, safeDrawingPadding() e slot de
 * conteudo. O safeDrawingPadding() e obrigatorio no Android 16, porque o
 * edge-to-edge nao tem mais opt-out (ARCHITECTURE.md secao 9).
 *
 * `centerTitle`, `titleStyle` e `containerColor` sao aditivos (default
 * preserva o comportamento de toda tela existente) — usados apenas pela
 * `home`, para reproduzir a referencia visual aprovada em 2026-08-25 com o
 * `CenterAlignedTopAppBar` nativo do Material 3, em vez de duplicar a barra.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    centerTitle: Boolean = false,
    titleStyle: TextStyle? = null,
    containerColor: Color = MaterialTheme.colorScheme.background,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier.safeDrawingPadding(),
        containerColor = containerColor,
        topBar = {
            val titleText: @Composable () -> Unit = {
                if (titleStyle != null) Text(title, style = titleStyle) else Text(title)
            }
            if (centerTitle) {
                CenterAlignedTopAppBar(
                    title = titleText,
                    navigationIcon = navigationIcon,
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = containerColor,
                    ),
                )
            } else {
                TopAppBar(
                    title = titleText,
                    navigationIcon = navigationIcon,
                )
            }
        },
        content = content,
    )
}
