package br.com.tamanhocerto.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import br.com.tamanhocerto.core.ads.AdsSettings
import br.com.tamanhocerto.feature.tools.configure.ConfigureRoute
import br.com.tamanhocerto.feature.tools.home.HomeScreen
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
        composable(Destinations.HOME) {
            // Tocar num cartao entra direto no layout da ferramenta; e a
            // propria tela que pede o arquivo, por dentro do layout (pedido
            // do responsavel em 2026-08-25, revertendo o gesto unico de
            // UI-SPEC secao 3 para as cinco ferramentas — comecou so em
            // Converter formato).
            HomeScreen(
                onToolClick = { tool -> navController.navigate(Destinations.configure(tool.name)) },
                onPrivacyClick = { navController.navigate(Destinations.POLICY) },
                onAboutClick = { navController.navigate(Destinations.ABOUT) },
            )
        }

        composable(
            route = Destinations.CONFIGURE,
            arguments = listOf(
                navArgument(Destinations.ARG_OPERATION_ID) { type = NavType.StringType },
            ),
        ) {
            ConfigureRoute(
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
