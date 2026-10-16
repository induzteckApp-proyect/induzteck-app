package com.example.induzteckapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.induzteckapp.ui.theme.screens.FormularioScreen
import com.example.induzteckapp.ui.theme.screens.InicioScreen
import com.example.induzteckapp.ui.theme.screens.LoginScreen
import com.example.induzteckapp.viewmodel.UsuariosViewModel

@Composable
fun AppNavegacion() {

    val navController = rememberNavController()

    // ViewModel compartido entre las pantallas.
    val usuariosViewModel: UsuariosViewModel = viewModel()

    val usuarios by usuariosViewModel.usuarios.collectAsStateWithLifecycle()
    val usuarioActual by usuariosViewModel.usuarioActual.collectAsStateWithLifecycle()

    // Normalizamos los correos para evitar duplicados
    // por diferencias entre mayúsculas y minúsculas.
    val correosRegistrados = usuarios
        .map { it.correo.trim().lowercase() }
        .toSet()

    NavHost(
        navController = navController,
        startDestination = Ruta.Login.ruta
    ) {

        // -----------------------------------------------------
        // LOGIN
        // -----------------------------------------------------
        composable(Ruta.Login.ruta) {
            LoginScreen(
                usuarios = usuarios,

                onLoginExitoso = { usuario ->
                    usuariosViewModel.iniciarSesion(usuario)

                    navController.navigate(Ruta.Inicio.ruta) {
                        // Evita regresar al Login con el botón Atrás
                        // después de haber iniciado sesión.
                        popUpTo(Ruta.Login.ruta) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },

                onIrARegistro = {
                    navController.navigate(Ruta.Registro.ruta)
                }
            )
        }

        // -----------------------------------------------------
        // REGISTRO
        // -----------------------------------------------------
        composable(Ruta.Registro.ruta) {
            FormularioScreen(
                correosRegistrados = correosRegistrados,

                onRegistroExitoso = { usuario ->
                    val registrado = usuariosViewModel.registrar(usuario)

                    if (registrado) {
                        // El registro no inicia sesión.
                        // Regresamos al Login para ingresar las credenciales.
                        navController.popBackStack()
                    }
                }
            )
        }

        // -----------------------------------------------------
        // INICIO
        // -----------------------------------------------------
        composable(Ruta.Inicio.ruta) {
            InicioScreen(
                usuario = usuarioActual,

                onCerrarSesion = {
                    usuariosViewModel.cerrarSesion()

                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(Ruta.Inicio.ruta) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
