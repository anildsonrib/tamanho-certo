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
 * preserva o comportamento de toda tela existente).
 *
 * `showTopBar = false` remove a barra de topo inteira — usado apenas pela
 * `home`, cujo titulo/subtitulo passaram a fazer parte do proprio conteudo
 * rolavel (referencia visual aprovada em 2026-08-26, `preview(1).html`,
 * estilo CamScanner), em vez do `CenterAlignedTopAppBar`. O
 * `safeDrawingPadding()` do Scaffold continua protegendo a barra de status
 * mesmo sem topo.
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
    showTopBar: Boolean = true,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier.safeDrawingPadding(),
        containerColor = containerColor,
        topBar = {
            if (showTopBar) {
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
            }
        },
        content = content,
    )
}
