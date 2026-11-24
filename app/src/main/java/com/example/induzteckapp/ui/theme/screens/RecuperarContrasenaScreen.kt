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
import com.example.induzteckapp.ui.theme.components.BotonSecundario
import com.example.induzteckapp.ui.theme.components.CampoContrasena
import com.example.induzteckapp.ui.theme.components.CampoTexto
import com.example.induzteckapp.ui.theme.components.ImagenEncabezado
import com.example.induzteckapp.ui.theme.components.TarjetaSeccion
import com.example.induzteckapp.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecuperarContrasenaScreen(onVolverLogin: () -> Unit, viewModel: LoginViewModel = viewModel()) {
    var correo by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var tokenSolicitado by remember { mutableStateOf(false) }
    var nuevaVisible by remember { mutableStateOf(false) }
    var confirmarVisible by remember { mutableStateOf(false) }
    var errorCorreo by remember { mutableStateOf<String?>(null) }
    var errorToken by remember { mutableStateOf<String?>(null) }
    var errorNueva by remember { mutableStateOf<String?>(null) }
    var errorConfirmar by remember { mutableStateOf<String?>(null) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var exito by remember { mutableStateOf(false) }

    fun solicitarCodigo() {
        errorCorreo = when {
            correo.isBlank() -> "El correo electrónico es obligatorio."
            !correo.trim().matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) -> "Ingresa un correo electrónico válido."
            else -> null
        }
        if (errorCorreo != null) return
        viewModel.solicitarRecuperacion(correo.trim()) { ok, texto ->
            exito = ok
            if (ok) {
                tokenSolicitado = true
                mensaje = texto
            } else if (texto.contains("correo", ignoreCase = true)) {
                errorCorreo = texto
            } else {
                mensaje = texto
            }
        }
    }

    fun restablecer() {
        errorToken = if (token.isBlank()) "Ingresa el código recibido por correo." else null
        errorNueva = when {
            nueva.isBlank() -> "La nueva contraseña es obligatoria."
            !LoginViewModel.validarContrasena(nueva) -> "Debe tener 8 a 72 caracteres, mayúscula, minúscula, número y símbolo permitido."
            else -> null
        }
        errorConfirmar = when {
            confirmar.isBlank() -> "Confirma tu nueva contraseña."
            confirmar != nueva -> "Las contraseñas no coinciden."
            else -> null
        }
        if (errorToken != null || errorNueva != null || errorConfirmar != null) return
        viewModel.restablecerContrasena(token.trim(), nueva, confirmar) { ok, texto ->
            exito = ok
            if (ok) {
                mensaje = texto
            } else {
                val detalle = texto.lowercase()
                when {
                    detalle.contains("token") || detalle.contains("código") || detalle.contains("codigo") -> errorToken = texto
                    detalle.contains("contraseña") -> errorNueva = texto
                    else -> mensaje = texto
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar contraseña") },
                navigationIcon = {
                    IconButton(onClick = onVolverLogin) {
                        Icon(painterResource(R.drawable.ic_volver), contentDescription = "Volver al inicio de sesión")
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
                descripcion = "Recuperación de contraseña Induztek",
                titulo = "Recupera tu acceso",
                subtitulo = "Verificaremos tu identidad mediante un código enviado a tu correo"
            )
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TarjetaSeccion(titulo = if (tokenSolicitado) "Verificación de identidad" else "Solicitar código") {
                    if (!tokenSolicitado) {
                        Text(
                            "Ingresa el correo asociado a tu cuenta. Recibirás un código de un solo uso para continuar.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        CampoTexto(
                            valor = correo,
                            onValorCambia = { correo = it; errorCorreo = null; mensaje = null },
                            etiqueta = "Correo electrónico",
                            icono = R.drawable.ic_correo,
                            error = errorCorreo,
                            tipoTeclado = KeyboardType.Email
                        )
                        BotonPrincipal(
                            texto = "Enviar código",
                            onClick = ::solicitarCodigo,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            "Ingresa el código enviado a $correo y define tu nueva contraseña.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        CampoTexto(
                            valor = token,
                            onValorCambia = { token = it; errorToken = null; mensaje = null },
                            etiqueta = "Código de recuperación",
                            icono = R.drawable.ic_candado,
                            error = errorToken,
                            tipoTeclado = KeyboardType.Ascii
                        )
                        CampoContrasena(
                            valor = nueva,
                            onValorCambia = { nueva = it; errorNueva = null; errorConfirmar = null; mensaje = null },
                            etiqueta = "Nueva contraseña",
                            visible = nuevaVisible,
                            onAlternarVisible = { nuevaVisible = !nuevaVisible },
                            error = errorNueva
                        )
                        CampoContrasena(
                            valor = confirmar,
                            onValorCambia = { confirmar = it; errorConfirmar = null; mensaje = null },
                            etiqueta = "Confirmar nueva contraseña",
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
                            texto = "Restablecer contraseña",
                            onClick = ::restablecer,
                            modifier = Modifier.fillMaxWidth()
                        )
                        BotonSecundario(
                            texto = "Usar otro correo",
                            onClick = {
                                tokenSolicitado = false
                                token = ""
                                nueva = ""
                                confirmar = ""
                                errorToken = null
                                errorNueva = null
                                errorConfirmar = null
                                mensaje = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    if (!mensaje.isNullOrBlank()) {
        AlertDialog(
            onDismissRequest = { mensaje = null },
            title = { Text(if (exito) "Operación completada" else "No fue posible continuar") },
            text = { Text(mensaje.orEmpty()) },
            confirmButton = {
                TextButton(onClick = { mensaje = null }) { Text("Aceptar") }
            }
        )
    }
}
