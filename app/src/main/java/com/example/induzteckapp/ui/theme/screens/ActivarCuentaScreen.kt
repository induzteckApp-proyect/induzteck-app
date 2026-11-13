package com.example.induzteckapp.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induzteckapp.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivarCuentaScreen(onVolver:()->Unit, viewModel:LoginViewModel=viewModel()) {
    var token by remember { mutableStateOf("") }; var clave by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }; var mensaje by remember { mutableStateOf<String?>(null) }
    var exito by remember { mutableStateOf(false) }
    Scaffold(topBar={TopAppBar(title={Text("Activar cuenta")},navigationIcon={TextButton(onClick=onVolver){Text("Volver")}})}){padding->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement=Arrangement.spacedBy(14.dp)){
            Text("Ingresa el token de invitación enviado por el administrador y define tu contraseña.")
            OutlinedTextField(token,{token=it},label={Text("Token de invitación")},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(clave,{clave=it},label={Text("Nueva contraseña")},visualTransformation=PasswordVisualTransformation(),modifier=Modifier.fillMaxWidth())
            OutlinedTextField(confirmar,{confirmar=it},label={Text("Confirmar contraseña")},visualTransformation=PasswordVisualTransformation(),modifier=Modifier.fillMaxWidth())
            Button(onClick={viewModel.activarCuenta(token,clave,confirmar){ok,msg->exito=ok;mensaje=msg}},modifier=Modifier.fillMaxWidth()){Text("Activar cuenta")}
            mensaje?.let{Text(it,color=if(exito) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)}
        }
    }
}
