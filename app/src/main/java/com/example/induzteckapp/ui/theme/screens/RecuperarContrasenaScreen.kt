//RecuperarContrasenaScreen

package com.example.induzteckapp.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induzteckapp.R
import com.example.induzteckapp.ui.theme.components.BotonPrincipal
import com.example.induzteckapp.ui.theme.components.CampoContrasena
import com.example.induzteckapp.ui.theme.components.CampoTexto
import com.example.induzteckapp.ui.theme.components.ImagenEncabezado
import com.example.induzteckapp.ui.theme.theme.Dimens
import com.example.induzteckapp.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecuperarContrasenaScreen(
    onVolverLogin: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    var correo by remember { mutableStateOf("") }
    var nuevaContrasena by remember { mutableStateOf("") }
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
                            contentDescription = "Volver"
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
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            ImagenEncabezado(
                imagen = R.drawable.banner_registro,
                descripcion = "Recuperación de cuenta",
                titulo = "Restablece tu clave",
                subtitulo = "Ingresa tu correo registrado para actualizar la contraseña"
            )

            CampoTexto(
                valor = correo,
                onValorCambia = { correo = it },
                etiqueta = "Correo electrónico",
                icono = R.drawable.ic_correo,
                tipoTeclado = KeyboardType.Email
            )

            CampoContrasena(
                valor = nuevaContrasena,
                onValorCambia = { nuevaContrasena = it },
                etiqueta = "Nueva contraseña (mín. 8 caracteres)",
                visible = false,
                onAlternarVisible = {}
            )

            mensajeResultado?.let { mensaje ->
                Text(
                    text = mensaje,
                    color = if (esExito) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            BotonPrincipal(
                texto = "Actualizar contraseña",
                onClick = {
                    viewModel.recuperarContrasena(correo, nuevaContrasena) { exito, mensaje ->
                        esExito = exito
                        mensajeResultado = mensaje
                        if (exito) {
                            // Opcional: limpiar campos o dar tiempo antes de volver
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
