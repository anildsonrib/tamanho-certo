package br.com.tamanhocerto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import br.com.tamanhocerto.core.ui.theme.TamanhoCertoTheme
import br.com.tamanhocerto.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Android 16: edge-to-edge nao tem opt-out. O padding fica no
        // AppScaffold, por safeDrawingPadding().
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TamanhoCertoTheme {
                AppNavHost(navController = rememberNavController())
            }
        }
    }
}
