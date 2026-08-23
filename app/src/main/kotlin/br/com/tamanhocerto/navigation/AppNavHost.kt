package br.com.tamanhocerto.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import br.com.tamanhocerto.legal.AboutScreen
import br.com.tamanhocerto.legal.PolicyScreen
import br.com.tamanhocerto.feature.tools.home.HomeScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Destinations.HOME) {
        composable(Destinations.HOME) {
            HomeScreen(
                // As telas de configuracao sao da fase 6: nesta fase o cartao
                // ainda nao navega.
                onToolClick = { },
                onPrivacyClick = { navController.navigate(Destinations.POLICY) },
                onAboutClick = { navController.navigate(Destinations.ABOUT) },
            )
        }
        composable(
            route = Destinations.CONFIGURE,
            arguments = listOf(navArgument(Destinations.ARG_OPERATION_ID) { type = NavType.StringType }),
        ) {
            // Fase 6.
        }
        composable(Destinations.RESULT) {
            // Fase 6.
        }
        composable(Destinations.POLICY) {
            PolicyScreen(onBack = { navController.popBackStack() })
        }
        composable(Destinations.ABOUT) {
            AboutScreen(
                onBack = { navController.popBackStack() },
                onPrivacyClick = { navController.navigate(Destinations.POLICY) },
            )
        }
    }
}
