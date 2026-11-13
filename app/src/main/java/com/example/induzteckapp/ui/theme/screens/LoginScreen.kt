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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induzteckapp.R
import com.example.induzteckapp.model.Usuario
import com.example.induzteckapp.ui.theme.components.*
import com.example.induzteckapp.ui.theme.theme.Dimens
import com.example.induzteckapp.ui.theme.theme.MiRegistroTheme
import com.example.induzteckapp.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoginExitoso: (Usuario) -> Unit,
    onIrARegistro: () -> Unit = {},
    onIrARecuperar: () -> Unit,
    onIrAActivar: () -> Unit = {},
    viewModel: LoginViewModel = viewModel()
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    LoginContent(
        correo=ui.correo, contrasena=ui.contrasena, visible=ui.contrasenaVisible,
        errorCorreo=ui.errorCorreo, errorClave=ui.errorContrasena, mensaje=ui.mensajeError,
        cargando=ui.cargando, onCorreo=viewModel::onCorreoCambia,
        onClave=viewModel::onContrasenaCambia, onVisible=viewModel::onAlternarVisibilidadContrasena,
        onLogin={ viewModel.iniciarSesion { usuario -> usuario?.let(onLoginExitoso) } },
        onRecuperar=onIrARecuperar, onActivar=onIrAActivar
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginContent(
    correo: String, contrasena: String, visible: Boolean,
    errorCorreo: String?, errorClave: String?, mensaje: String?, cargando: Boolean,
    onCorreo: (String)->Unit, onClave: (String)->Unit, onVisible: ()->Unit,
    onLogin: ()->Unit, onRecuperar: ()->Unit, onActivar: ()->Unit
) {
    Scaffold(topBar={ TopAppBar(title={Text("Iniciar sesión")}, colors=TopAppBarDefaults.topAppBarColors(
        containerColor=MaterialTheme.colorScheme.primary,titleContentColor=MaterialTheme.colorScheme.onPrimary)) },
        containerColor=MaterialTheme.colorScheme.background) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState())
            .padding(Dimens.espacioPantalla), verticalArrangement=Arrangement.spacedBy(Dimens.espacioMedio)) {
            ImagenEncabezado(imagen=R.drawable.banner_induztek,descripcion="Encabezado de Induztek",
                titulo="Bienvenido a Induztek",subtitulo="Ingresa con tu cuenta para continuar")
            Text("Accede a la información correspondiente a tu cuenta institucional o de cliente.",
                style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
            CampoTexto(valor=correo,onValorCambia=onCorreo,etiqueta="Correo electrónico",
                icono=R.drawable.ic_correo,error=errorCorreo,tipoTeclado=KeyboardType.Email)
            CampoContrasena(valor=contrasena,onValorCambia=onClave,etiqueta="Contraseña",
                visible=visible,onAlternarVisible=onVisible,error=errorClave)
            TextButton(onClick=onRecuperar,modifier=Modifier.align(Alignment.End)){Text("¿Olvidaste tu contraseña?")}
            AnimatedVisibility(visible=mensaje!=null,enter=fadeIn()+expandVertically(),exit=fadeOut()+shrinkVertically()){
                mensaje?.let { Text(it,color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodyMedium) }
            }
            BotonPrincipal(texto=if(cargando) "Conectando…" else "Iniciar sesión",onClick=onLogin,
                modifier=Modifier.fillMaxWidth(),habilitado=!cargando)
            if(cargando) CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            TextButton(onClick=onActivar,modifier=Modifier.align(Alignment.CenterHorizontally)){
                Text("Tengo un token de invitación para activar mi cuenta")
            }
            Text("Las cuentas son creadas por un administrador de Induztek.",
                style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant,
                modifier=Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Preview(showBackground=true)
@Composable private fun LoginScreenPreview(){
    MiRegistroTheme {
        LoginContent("tecnico@induztek.cl","",false,null,null,null,false,{}, {}, {}, {}, {}, {})
    }
}
