package com.example.induzteckapp.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induzteckapp.data.UsuarioRepository
import com.example.induzteckapp.model.RolUsuario
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsuariosScreen(onVolver:()->Unit) {
    val context=androidx.compose.ui.platform.LocalContext.current
    val scope=rememberCoroutineScope()
    val repo=remember { UsuarioRepository(context) }
    var rut by remember{mutableStateOf("")};var nombre by remember{mutableStateOf("")}
    var apellido by remember{mutableStateOf("")};var correo by remember{mutableStateOf("")}
    var telefono by remember{mutableStateOf("")};var giro by remember{mutableStateOf("")}
    var contacto by remember{mutableStateOf("")};var cargo by remember{mutableStateOf("")}
    var rol by remember{mutableStateOf(RolUsuario.CLIENTE)}
    var mensaje by remember{mutableStateOf<String?>(null)}
    var esExito by remember{mutableStateOf(false)}
    var tokenDev by remember{mutableStateOf<String?>(null)}
    var cargando by remember{mutableStateOf(false)}

    Scaffold(topBar={TopAppBar(title={Text("Administrar usuarios")},navigationIcon={
        TextButton(onClick=onVolver){Text("Volver")}
    })}){padding->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(18.dp),
            verticalArrangement=Arrangement.spacedBy(10.dp)){
            Text("Solo ADMIN puede crear cuentas. El usuario define su propia contraseña al activar la invitación.",
                style=MaterialTheme.typography.bodyMedium)
            OutlinedTextField(rut,{rut=it},label={Text("RUT")},modifier=Modifier.fillMaxWidth(),singleLine=true)
            OutlinedTextField(nombre,{nombre=it},label={Text("Nombre o razón social")},modifier=Modifier.fillMaxWidth(),singleLine=true)
            OutlinedTextField(apellido,{apellido=it},label={Text("Apellido (opcional)")},modifier=Modifier.fillMaxWidth(),singleLine=true)
            OutlinedTextField(correo,{correo=it},label={Text("Correo electrónico")},modifier=Modifier.fillMaxWidth(),singleLine=true,
                keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(keyboardType=KeyboardType.Email))
            OutlinedTextField(telefono,{telefono=it.filter(Char::isDigit)},label={Text("Teléfono (8 a 12 dígitos)")},modifier=Modifier.fillMaxWidth(),singleLine=true,
                keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(keyboardType=KeyboardType.Phone))
            Text("Rol de la cuenta", style=MaterialTheme.typography.labelLarge)
            RolUsuario.entries.forEach { opcion ->
                Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected=rol==opcion,onClick={rol=opcion})
                    Text(opcion.etiqueta)
                }
            }
            if(rol==RolUsuario.CLIENTE){
                OutlinedTextField(giro,{giro=it},label={Text("Giro / rubro *")},modifier=Modifier.fillMaxWidth())
                OutlinedTextField(contacto,{contacto=it},label={Text("Nombre de contacto *")},modifier=Modifier.fillMaxWidth())
                OutlinedTextField(cargo,{cargo=it},label={Text("Cargo del contacto *")},modifier=Modifier.fillMaxWidth())
            }
            Button(enabled=!cargando,onClick={
                cargando=true;mensaje=null;tokenDev=null
                scope.launch {
                    try {
                        val r=repo.crearUsuario(rut,nombre,apellido,correo,telefono,rol.name,giro,contacto,cargo)
                        mensaje=r.mensaje;tokenDev=r.tokenDesarrollo;esExito=true
                        rut="";nombre="";apellido="";correo="";telefono="";giro="";contacto="";cargo=""
                    }catch(e:Exception){mensaje=e.message ?: "No se pudo crear la cuenta.";esExito=false}
                    finally{cargando=false}
                }
            },modifier=Modifier.fillMaxWidth()){Text(if(cargando)"Creando cuenta…" else "Crear cuenta e invitar")}
            mensaje?.let{Text(it,color=if(esExito) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)}
            tokenDev?.let{
                Text("TOKEN DE DESARROLLO: copia y entrega este código al usuario para que active su cuenta.",
                    style=MaterialTheme.typography.bodySmall)
                SelectionContainer { Text(it) }
            }
        }
    }
}
