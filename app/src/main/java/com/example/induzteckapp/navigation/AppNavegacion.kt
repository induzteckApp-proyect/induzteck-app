package com.example.induzteckapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.induzteckapp.ui.theme.screens.*

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Ruta.Login.ruta) {
        composable(Ruta.Login.ruta) {
            LoginScreen(
                onLoginExitoso = {
                    navController.navigate(Ruta.Inicio.ruta) {
                        popUpTo(Ruta.Login.ruta) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onIrARecuperar = { navController.navigate(Ruta.RecuperarContrasena.ruta) },
                onIrAActivar = { navController.navigate(Ruta.ActivarCuenta.ruta) }
            )
        }
        composable(Ruta.ActivarCuenta.ruta) {
            ActivarCuentaScreen(onVolver = { navController.popBackStack() })
        }
        composable(Ruta.RecuperarContrasena.ruta) {
            RecuperarContrasenaScreen(onVolverLogin = { navController.popBackStack() })
        }
        composable(Ruta.Inicio.ruta) {
            InicioScreen(
                onCerrarSesion = {
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(Ruta.Inicio.ruta) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onIrANuevoProtocolo = { navController.navigate(Ruta.CrearProtocolo.nueva()) },
                onIrAListaClientes = { navController.navigate(Ruta.ListaClientes.ruta) },
                onIrAListaInstalaciones = { navController.navigate(Ruta.ListaInstalaciones.ruta) },
                onIrAAdministrarUsuarios = { navController.navigate(Ruta.AdminUsuarios.ruta) },
                onIrAPerfil = { navController.navigate(Ruta.Perfil.ruta) }
            )
        }
        composable(Ruta.AdminUsuarios.ruta) {
            AdminUsuariosScreen(onVolver = { navController.popBackStack() })
        }
        composable(
            route = Ruta.CrearProtocolo.ruta,
            arguments = listOf(navArgument("borradorId") { type = NavType.LongType; defaultValue = -1L })
        ) { entrada ->
            CrearProtocoloScreen(
                onVolver = { navController.popBackStack() },
                onIrABorradores = { navController.navigate(Ruta.ListaBorradores.ruta) },
                onIrAHistorial = { navController.navigate(Ruta.HistorialProtocolos.ruta) },
                idBorrador = entrada.arguments?.getLong("borradorId") ?: -1L
            )
        }
        composable(Ruta.HistorialProtocolos.ruta) {
            HistorialProtocolosScreen(onVolver = { navController.popBackStack() })
        }
        composable(Ruta.ListaBorradores.ruta) {
            ListaBorradoresScreen(
                onVolver = { navController.popBackStack() },
                onRecuperar = { id -> navController.navigate(Ruta.CrearProtocolo.recuperar(id)) }
            )
        }
        composable(Ruta.ListaClientes.ruta) {
            ListaClientesScreen(
                onVolver = { navController.popBackStack() },
                onProtocoloClick = { id -> navController.navigate(Ruta.DetalleRespaldo.construir(id)) }
            )
        }
        composable(Ruta.ListaInstalaciones.ruta) {
            ListaInstalacionesScreen(
                onVolver = { navController.popBackStack() },
                onProtocoloClick = { id -> navController.navigate(Ruta.DetalleRespaldo.construir(id)) }
            )
        }
        composable(
            route = Ruta.DetalleRespaldo.ruta,
            arguments = listOf(navArgument("idProtocolo") { type = NavType.IntType })
        ) { entry ->
            DetalleRespaldoDigitalScreen(
                idProtocolo = entry.arguments?.getInt("idProtocolo") ?: -1,
                onVolver = { navController.popBackStack() }
            )
        }
        composable(Ruta.Perfil.ruta) {
            PerfilScreen(onVolver = { navController.popBackStack() })
        }
    }
}
