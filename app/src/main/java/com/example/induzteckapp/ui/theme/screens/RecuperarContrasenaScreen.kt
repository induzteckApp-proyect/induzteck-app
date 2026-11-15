package com.example.induzteckapp.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induzteckapp.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecuperarContrasenaScreen(onVolverLogin: () -> Unit, viewModel: LoginViewModel = viewModel()) {
    var correo by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var tokenSolicitado by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var tokenDev by remember { mutableStateOf<String?>(null) }
    var exito by remember { mutableStateOf(false) }

    Scaffold(topBar={ TopAppBar(title={Text("Recuperar contraseña")}, navigationIcon={
        TextButton(onClick=onVolverLogin){Text("Volver")}
    }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement=Arrangement.spacedBy(14.dp)) {
            Text("Solicita un enlace de recuperación. El token es de un solo uso y expira.")
            if (!tokenSolicitado) {
                OutlinedTextField(value=correo,onValueChange={correo=it;mensaje=null},
                    label={Text("Correo registrado")},modifier=Modifier.fillMaxWidth(),singleLine=true,
                    keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(keyboardType=KeyboardType.Email))
                Button(onClick={
                    viewModel.solicitarRecuperacion(correo){ ok,msg,dev ->
                        exito=ok;mensaje=msg;tokenDev=dev;if(ok)tokenSolicitado=true
                    }
                },modifier=Modifier.fillMaxWidth()){Text("Enviar solicitud")}
            } else {
                OutlinedTextField(value=token,onValueChange={token=it},label={Text("Token de recuperación")},
                    modifier=Modifier.fillMaxWidth())
                OutlinedTextField(value=nueva,onValueChange={nueva=it},label={Text("Nueva contraseña")},
                    visualTransformation=PasswordVisualTransformation(),modifier=Modifier.fillMaxWidth())
                OutlinedTextField(value=confirmar,onValueChange={confirmar=it},label={Text("Confirmar contraseña")},
                    visualTransformation=PasswordVisualTransformation(),modifier=Modifier.fillMaxWidth())
                Button(onClick={
                    viewModel.restablecerContrasena(token,nueva,confirmar){ok,msg->exito=ok;mensaje=msg}
                },modifier=Modifier.fillMaxWidth()){Text("Restablecer contraseña")}
            }
            mensaje?.let { Text(it,color=if(exito) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) }
            tokenDev?.let {
                Text("TOKEN DE DESARROLLO (solo cuando MAIL_ENABLED=false):",fontWeight=androidx.compose.ui.text.font.FontWeight.Bold)
                SelectionContainer { Text(it) }
                Text("En producción el token se envía por correo y no se muestra en la aplicación.",
                    style=MaterialTheme.typography.bodySmall)
            }
        }
    }
}
