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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induzteckapp.R
import com.example.induzteckapp.ui.theme.components.BotonPrincipal
import com.example.induzteckapp.ui.theme.components.CampoContrasena
import com.example.induzteckapp.ui.theme.components.CampoTexto
import com.example.induzteckapp.ui.theme.components.ImagenEncabezado
import com.example.induzteckapp.ui.theme.theme.Dimens
import com.example.induzteckapp.ui.theme.theme.MiRegistroTheme
import com.example.induzteckapp.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecuperarContrasenaScreen(
    onVolverLogin: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    var correo by remember { mutableStateOf("") }
    var nuevaContrasena by remember { mutableStateOf("") }
    var nuevaContrasenaVisible by remember { mutableStateOf(false) }
    var mensajeResultado by remember { mutableStateOf<String?>(null) }
    var esExito by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar contraseña") },
                navigationIcon = {
                    IconButton(onClick = onVolverLogin) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = "Volver al inicio de sesión"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding() // Solución al problema del teclado tapando los campos e inputs inferiores
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            ImagenEncabezado(
                imagen = R.drawable.banner_induztek,
                descripcion = "Recuperación de cuenta",
                titulo = "Restablece tu clave",
                subtitulo = "Ingresa tu correo registrado para actualizar la contraseña"
            )

            Text(
                text = "Escribe tu correo institucional o de cliente para asociar tu nueva clave de acceso.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            CampoTexto(
                valor = correo,
                onValorCambia = {
                    correo = it
                    mensajeResultado = null
                },
                etiqueta = "Correo electrónico registrado (ej: usuario@induzteck.cl)",
                icono = R.drawable.ic_correo,
                tipoTeclado = KeyboardType.Email
            )

            Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                CampoContrasena(
                    valor = nuevaContrasena,
                    onValorCambia = {
                        nuevaContrasena = it
                        mensajeResultado = null
                    },
                    etiqueta = "Nueva contraseña",
                    visible = nuevaContrasenaVisible,
                    onAlternarVisible = { nuevaContrasenaVisible = !nuevaContrasenaVisible }
                )

                Text(
                    text = "Debe tener mín. 8 caracteres, incluir mayúscula, minúscula, número y símbolo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Feedback visual animado de éxito o error
            AnimatedVisibility(
                visible = mensajeResultado != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                mensajeResultado?.let { mensaje ->
                    Text(
                        text = mensaje,
                        color = if (esExito) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            BotonPrincipal(
                texto = "Actualizar contraseña",
                onClick = {
                    viewModel.recuperarContrasena(correo, nuevaContrasena) { exito, mensaje ->
                        esExito = exito
                        mensajeResultado = mensaje
                        if (exito) {
                            correo = ""
                            nuevaContrasena = ""
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            TextButton(
                onClick = onVolverLogin,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Volver al inicio de sesión")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RecuperarContrasenaScreenPreview() {
    MiRegistroTheme {
        RecuperarContrasenaScreen(
            onVolverLogin = {}
        )
    }
}
