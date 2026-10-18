// induzteckApp/ui.theme/screens/LoginScreen.kt

package com.example.induzteckapp.ui.theme.screens

import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induzteckapp.R
import com.example.induzteckapp.model.Usuario
import com.example.induzteckapp.ui.theme.components.BotonPrincipal
import com.example.induzteckapp.ui.theme.components.CampoContrasena
import com.example.induzteckapp.ui.theme.components.CampoTexto
import com.example.induzteckapp.ui.theme.components.ImagenEncabezado
import com.example.induzteckapp.ui.theme.theme.Dimens
import com.example.induzteckapp.ui.theme.theme.MiRegistroTheme
import com.example.induzteckapp.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    usuarios: List<Usuario>,
    onLoginExitoso: (Usuario) -> Unit,
    onIrARegistro: () -> Unit,
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
            viewModel.validarCredenciales(usuarios)?.let { usuario ->
                onLoginExitoso(usuario)
            }
        },
        onIrARegistro = onIrARegistro
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
    onIrARegistro: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Iniciar sesión")
                },
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
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(
                Dimens.espacioMedio
            )
        ) {
            ImagenEncabezado(
                imagen = R.drawable.banner_registro,
                descripcion = "Encabezado de Induzteck",
                titulo = "Bienvenido a Induzteck",
                subtitulo = "Ingresa con tu cuenta para continuar"
            )

            Text(
                text = "Accede a la información correspondiente a tu cuenta.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            CampoTexto(
                valor = correo,
                onValorCambia = onCorreoCambia,
                etiqueta = "Correo electrónico",
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

            if (mensajeError != null) {
                Text(
                    text = mensajeError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            BotonPrincipal(
                texto = "Iniciar sesión",
                onClick = onIniciarSesion,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "¿Todavía no tienes cuenta?",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            TextButton(
                onClick = onIrARegistro,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrarme")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    MiRegistroTheme {
        LoginContent(
            correo = "",
            contrasena = "",
            contrasenaVisible = false,
            errorCorreo = null,
            errorContrasena = null,
            mensajeError = null,
            onCorreoCambia = {},
            onContrasenaCambia = {},
            onAlternarVisibilidad = {},
            onIniciarSesion = {},
            onIrARegistro = {}
        )
    }
}