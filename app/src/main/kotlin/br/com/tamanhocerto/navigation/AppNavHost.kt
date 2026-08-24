package br.com.tamanhocerto.navigation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import br.com.tamanhocerto.core.ads.AdsSettings
import br.com.tamanhocerto.core.files.PickerContracts
import br.com.tamanhocerto.feature.tools.configure.ConfigureRoute
import br.com.tamanhocerto.feature.tools.configure.SelectionViewModel
import br.com.tamanhocerto.feature.tools.home.HomeScreen
import br.com.tamanhocerto.feature.tools.home.ToolId
import br.com.tamanhocerto.legal.AboutScreen
import br.com.tamanhocerto.legal.PolicyScreen
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch

/** Duracao do fade entre telas; 200ms e o padrao do Material Motion para transicoes simples. */
private const val SCREEN_TRANSITION_MS = 200

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Destinations.HOME,
        enterTransition = { fadeIn(tween(SCREEN_TRANSITION_MS)) },
        exitTransition = { fadeOut(tween(SCREEN_TRANSITION_MS)) },
        popEnterTransition = { fadeIn(tween(SCREEN_TRANSITION_MS)) },
        popExitTransition = { fadeOut(tween(SCREEN_TRANSITION_MS)) },
    ) {
        composable(Destinations.HOME) { entry ->
            // Escopo do grafo: a selecao sobrevive a ida para `configure`.
            val selection: SelectionViewModel = hiltViewModel(entry)

            // Tocar num cartao abre IMEDIATAMENTE o seletor do sistema; nao ha
            // tela intermediaria (UI-SPEC secao 3).
            val pickImages = rememberLauncherForActivityResult(
                ActivityResultContracts.PickMultipleVisualMedia(),
            ) { uris -> onPicked(navController, selection, uris, pendingTool) }

            val pickPdf = rememberLauncherForActivityResult(
                ActivityResultContracts.OpenDocument(),
            ) { uri -> onPicked(navController, selection, listOfNotNull(uri), pendingTool) }

            HomeScreen(
                onToolClick = { tool ->
                    pendingTool = tool
                    when (tool) {
                        // PDF entra um por vez; imagem aceita varias, e o
                        // lote passa pelo gate de recompensa (D7).
                        ToolId.PDF_TO_IMAGES -> pickPdf.launch(PickerContracts.PDF_MIME_FILTER)
                        else -> pickImages.launch(imageRequest())
                    }
                },
                onPrivacyClick = { navController.navigate(Destinations.POLICY) },
                onAboutClick = { navController.navigate(Destinations.ABOUT) },
            )
        }

        composable(
            route = Destinations.CONFIGURE,
            arguments = listOf(
                navArgument(Destinations.ARG_OPERATION_ID) { type = NavType.StringType },
            ),
        ) { entry ->
            val homeEntry = rememberHomeEntry(navController, entry)
            val selection: SelectionViewModel = hiltViewModel(homeEntry)
            val uris by selection.selection.collectAsStateWithLifecycle()

            ConfigureRoute(
                uris = uris.map(Uri::parse),
                onBack = { navController.popBackStack() },
                onHome = {
                    navController.popBackStack(Destinations.HOME, inclusive = false)
                },
            )
        }

        composable(Destinations.POLICY) {
            PolicyScreen(onBack = { navController.popBackStack() })
        }

        composable(Destinations.ABOUT) {
            val scope = androidx.compose.runtime.rememberCoroutineScope()
            val settings = adsSettings()
            AboutScreen(
                onBack = { navController.popBackStack() },
                onPrivacyClick = { navController.navigate(Destinations.POLICY) },
                onAdSettingsClick = { scope.launch { settings.openConsentForm() } },
            )
        }
    }
}

/** Ferramenta escolhida enquanto o seletor do sistema esta aberto. */
private var pendingTool: ToolId = ToolId.COMPRESS

private fun imageRequest() =
    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)

/** Cancelar o seletor devolve para `home` sem erro (UI-SPEC secao 3). */
private fun onPicked(
    navController: NavHostController,
    selection: SelectionViewModel,
    uris: List<Uri>,
    tool: ToolId,
) {
    if (uris.isEmpty()) return
    selection.select(uris)
    navController.navigate(Destinations.configure(tool.name))
}

/**
 * A selecao vive no escopo da entrada `home`, que sobrevive a navegacao. A
 * chave e a propria entrada atual, como o lint do Navigation exige.
 */
@Composable
private fun rememberHomeEntry(
    navController: NavHostController,
    entry: androidx.navigation.NavBackStackEntry,
) = androidx.compose.runtime.remember(entry) {
    navController.getBackStackEntry(Destinations.HOME)
}

/**
 * `AdsSettings` vive em `:core:ads`, que so o `:app` conhece. O acesso por
 * EntryPoint evita passar a dependencia por toda a arvore de Composables.
 */
@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface AdsEntryPoint {
    fun adsSettings(): AdsSettings
}

@Composable
private fun adsSettings(): AdsSettings {
    val context = androidx.compose.ui.platform.LocalContext.current
    return androidx.compose.runtime.remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            AdsEntryPoint::class.java,
        ).adsSettings()
    }
}
