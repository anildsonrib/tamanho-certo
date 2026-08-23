package br.com.tamanhocerto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import br.com.tamanhocerto.core.ui.theme.TamanhoCertoTheme
import br.com.tamanhocerto.di.OptInDialog
import br.com.tamanhocerto.di.OptInGateway
import br.com.tamanhocerto.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var optInGateway: OptInGateway

    @Inject
    lateinit var workDir: br.com.tamanhocerto.core.files.WorkDir

    override fun onCreate(savedInstanceState: Bundle?) {
        // Android 16: edge-to-edge nao tem opt-out. O padding fica no
        // AppScaffold, por safeDrawingPadding().
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TamanhoCertoTheme {
                AppNavHost(navController = rememberNavController())
                OptInDialog(optInGateway)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Limpeza dos temporarios ao sair (ENGINE-SPEC secao 4). O prepare()
        // da proxima operacao limpa de novo — sao dois pontos de proposito.
        runCatching { workDir.dir.listFiles()?.forEach { it.deleteRecursively() } }
    }
}
