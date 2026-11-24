package com.example.induzteckapp.ui.theme.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induzteckapp.R
import com.example.induzteckapp.ui.theme.components.BotonPrincipal
import com.example.induzteckapp.ui.theme.components.CampoContrasena
import com.example.induzteckapp.ui.theme.components.CampoTexto
import com.example.induzteckapp.ui.theme.components.ImagenEncabezado
import com.example.induzteckapp.ui.theme.components.TarjetaSeccion
import com.example.induzteckapp.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivarCuentaScreen(onVolver: () -> Unit, viewModel: LoginViewModel = viewModel()) {
    var token by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var claveVisible by remember { mutableStateOf(false) }
    var confirmarVisible by remember { mutableStateOf(false) }
    var errorToken by remember { mutableStateOf<String?>(null) }
    var errorClave by remember { mutableStateOf<String?>(null) }
    var errorConfirmar by remember { mutableStateOf<String?>(null) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var exito by remember { mutableStateOf(false) }

    fun activar() {
        errorToken = if (token.isBlank()) "Ingresa el código de activación." else null
        errorClave = when {
            clave.isBlank() -> "La contraseña es obligatoria."
            !LoginViewModel.validarContrasena(clave) -> "Debe tener 8 a 72 caracteres, mayúscula, minúscula, número y símbolo permitido."
            else -> null
        }
        errorConfirmar = when {
            confirmar.isBlank() -> "Confirma tu contraseña."
            confirmar != clave -> "Las contraseñas no coinciden."
            else -> null
        }
        if (errorToken != null || errorClave != null || errorConfirmar != null) return
        viewModel.activarCuenta(token.trim(), clave, confirmar) { ok, texto ->
            exito = ok
            if (ok) {
                mensaje = texto
            } else {
                val detalle = texto.lowercase()
                when {
                    detalle.contains("token") || detalle.contains("código") || detalle.contains("codigo") || detalle.contains("invitación") -> errorToken = texto
                    detalle.contains("contraseña") -> errorClave = texto
                    else -> mensaje = texto
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activar cuenta") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(painterResource(R.drawable.ic_volver), contentDescription = "Volver")
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ImagenEncabezado(
                imagen = R.drawable.banner_induztek,
                descripcion = "Activación de cuenta Induztek",
                titulo = "Activa tu cuenta",
                subtitulo = "Define una contraseña para comenzar a utilizar Induztek"
            )
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TarjetaSeccion(titulo = "Datos de activación") {
                    Text(
                        "Ingresa el código de invitación que recibiste por correo electrónico.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    CampoTexto(
                        valor = token,
                        onValorCambia = { token = it; errorToken = null; mensaje = null },
                        etiqueta = "Código de activación",
                        icono = R.drawable.ic_candado,
                        error = errorToken,
                        tipoTeclado = KeyboardType.Ascii
                    )
                    CampoContrasena(
                        valor = clave,
                        onValorCambia = { clave = it; errorClave = null; errorConfirmar = null; mensaje = null },
                        etiqueta = "Nueva contraseña",
                        visible = claveVisible,
                        onAlternarVisible = { claveVisible = !claveVisible },
                        error = errorClave
                    )
                    CampoContrasena(
                        valor = confirmar,
                        onValorCambia = { confirmar = it; errorConfirmar = null; mensaje = null },
                        etiqueta = "Confirmar contraseña",
                        visible = confirmarVisible,
                        onAlternarVisible = { confirmarVisible = !confirmarVisible },
                        error = errorConfirmar
                    )
                    Text(
                        "La contraseña debe incluir mayúscula, minúscula, número y un símbolo permitido.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    BotonPrincipal(
                        texto = "Activar cuenta",
                        onClick = ::activar,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (!mensaje.isNullOrBlank()) {
        AlertDialog(
            onDismissRequest = { mensaje = null },
            title = { Text(if (exito) "Cuenta activada" else "No fue posible activar la cuenta") },
            text = { Text(mensaje.orEmpty()) },
            confirmButton = {
                TextButton(onClick = { mensaje = null }) { Text("Aceptar") }
            }
        )
    }
}
