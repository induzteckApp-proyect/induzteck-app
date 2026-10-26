// com.example.induzteckapp/navigation/AppNavegacion.kt

package com.example.induzteckapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.induzteckapp.ui.theme.screens.FormularioScreen
import com.example.induzteckapp.ui.theme.screens.InicioScreen
import com.example.induzteckapp.ui.theme.screens.LoginScreen
import com.example.induzteckapp.ui.theme.screens.RecuperarContrasenaScreen
import com.example.induzteckapp.ui.theme.screens.CrearProtocoloScreen
import com.example.induzteckapp.ui.theme.screens.ListaClientesScreen
import com.example.induzteckapp.ui.theme.screens.ListaInstalacionesScreen
import com.example.induzteckapp.ui.theme.screens.DetalleRespaldoDigitalScreen
@Composable
fun AppNavegacion() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Ruta.Login.ruta
    ) {

        composable(Ruta.Login.ruta) {
            LoginScreen(
                onLoginExitoso = {
                    navController.navigate(Ruta.Inicio.ruta) {
                        popUpTo(Ruta.Login.ruta) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onIrARegistro = { navController.navigate(Ruta.Registro.ruta) },
                onIrARecuperar = { navController.navigate(Ruta.RecuperarContrasena.ruta) }
            )
        }

        composable(Ruta.Registro.ruta) {
            FormularioScreen(
                onRegistroExitoso = { navController.popBackStack() },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Ruta.RecuperarContrasena.ruta) {
            RecuperarContrasenaScreen(
                onVolverLogin = { navController.popBackStack() }
            )
        }

        composable(Ruta.Inicio.ruta) {
            InicioScreen(
                onCerrarSesion = {
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(Ruta.Inicio.ruta) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onIrANuevoProtocolo = {
                    navController.navigate(Ruta.CrearProtocolo.ruta)
                },
                onIrAListaClientes = {
                    navController.navigate(Ruta.ListaClientes.ruta)
                },
                onIrAListaInstalaciones = {
                    navController.navigate(Ruta.ListaInstalaciones.ruta) // <--- ¡FALTABA CONECTAR ESTA RUTA!
                }
            )
        }

        composable(Ruta.CrearProtocolo.ruta) {
            CrearProtocoloScreen(
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Ruta.ListaClientes.ruta) {
            ListaClientesScreen(
                onVolver = { navController.popBackStack() }
            )
        }

        // -----------------------------------------------------
        // VISTA DE INSTALACIONES (Exclusiva Cliente)
        // -----------------------------------------------------
        composable(Ruta.ListaInstalaciones.ruta) {
            ListaInstalacionesScreen(
                onVolver = { navController.popBackStack() },
                onProtocoloClick = { idProtocolo ->
                    navController.navigate(Ruta.DetalleRespaldo.construir(idProtocolo))
                }
            )
        }

        composable(
            route = Ruta.DetalleRespaldo.ruta,
            arguments = listOf(navArgument("idProtocolo") { type = NavType.IntType })
        ) { backStackEntry ->
            val idProtocolo = backStackEntry.arguments?.getInt("idProtocolo")
            if (idProtocolo != null) {
                DetalleRespaldoDigitalScreen(
                    idProtocolo = idProtocolo,
                    onVolver = { navController.popBackStack() }
                )
            } else {
                navController.popBackStack()
            }
        }

    }
}
