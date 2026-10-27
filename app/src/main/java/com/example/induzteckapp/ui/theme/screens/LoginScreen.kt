package com.example.induzteckapp.ui.theme.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induzteckapp.R
import com.example.induzteckapp.model.RolUsuario
import com.example.induzteckapp.model.Usuario
import com.example.induzteckapp.ui.theme.components.BotonPrincipal
import com.example.induzteckapp.ui.theme.components.BotonSecundario
import com.example.induzteckapp.ui.theme.components.CampoContrasena
import com.example.induzteckapp.ui.theme.components.CampoTexto
import com.example.induzteckapp.ui.theme.components.ImagenEncabezado
import com.example.induzteckapp.ui.theme.theme.Dimens
import com.example.induzteckapp.ui.theme.theme.MiRegistroTheme
import com.example.induzteckapp.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoginExitoso: (Usuario) -> Unit,
    onIrARegistro: () -> Unit,
    onIrARecuperar: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LoginContent(
        correo = uiState.correo,
        contrasena = uiState.contrasena,
        contrasenaVisible = uiState.contrasenaVisible,
        errorCorreo = uiState.errorCorreo,
        errorContrasena = uiState.errorContrasena,
        mensajeError = uiState.mensajeError,
        onCorreoCambia = viewModel::onCorreoCambia,
        onContrasenaCambia = viewModel::onContrasenaCambia,
        onAlternarVisibilidad = viewModel::onAlternarVisibilidadContrasena,
        onIniciarSesion = {
            viewModel.validarCredenciales()?.let { usuario ->
                onLoginExitoso(usuario)
            }
        },
        onIrARegistro = onIrARegistro,
        onIrARecuperar = onIrARecuperar
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginContent(
    correo: String,
    contrasena: String,
    contrasenaVisible: Boolean,
    errorCorreo: String?,
    errorContrasena: String?,
    mensajeError: String?,
    onCorreoCambia: (String) -> Unit,
    onContrasenaCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onIniciarSesion: () -> Unit,
    onIrARegistro: () -> Unit,
    onIrARecuperar: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Iniciar sesión") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding() 
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            ImagenEncabezado(
                imagen = R.drawable.banner_induztek,
                descripcion = "Encabezado de Induztek",
                titulo = "Bienvenido a Induztek",
                subtitulo = "Ingresa con tu cuenta para continuar"
            )

            Text(
                text = "Accede a la información correspondiente a tu cuenta institucional o de cliente.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            CampoTexto(
                valor = correo,
                onValorCambia = onCorreoCambia,
                etiqueta = "Correo electrónico (ej: usuario@induzteck.cl)",
                icono = R.drawable.ic_correo,
                error = errorCorreo,
                tipoTeclado = KeyboardType.Email
            )

            CampoContrasena(
                valor = contrasena,
                onValorCambia = onContrasenaCambia,
                etiqueta = "Contraseña",
                visible = contrasenaVisible,
                onAlternarVisible = onAlternarVisibilidad,
                error = errorContrasena
            )

            TextButton(
                onClick = onIrARecuperar,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("¿Olvidaste tu contraseña?")
            }

            
            AnimatedVisibility(
                visible = mensajeError != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                mensajeError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            
            BotonPrincipal(
                texto = "Iniciar sesión",
                onClick = onIniciarSesion,
                modifier = Modifier.fillMaxWidth()
            )

            

            

            

            
            Text(
                text = "¿Aún no tienes cuenta registrada en Induzteck?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            BotonSecundario(
                texto = "Registrar nueva cuenta",
                onClick = onIrARegistro,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    MiRegistroTheme {
        LoginContent(
            correo = "tecnico@induzteck.cl",
            contrasena = "",
            contrasenaVisible = false,
            errorCorreo = null,
            errorContrasena = null,
            mensajeError = null,
            onCorreoCambia = {},
            onContrasenaCambia = {},
            onAlternarVisibilidad = {},
            onIniciarSesion = {},
            onIrARegistro = {},
            onIrARecuperar = {}
        )
    }
}

