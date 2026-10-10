
package com.example.induzteckapp.ui.theme.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induzteckapp.R
import com.example.induzteckapp.model.FormularioUiState
import com.example.induzteckapp.model.RolUsuario
import com.example.induzteckapp.ui.theme.components.BotonPrincipal
import com.example.induzteckapp.ui.theme.components.BotonSecundario
import com.example.induzteckapp.ui.theme.components.CampoContrasena
import com.example.induzteckapp.ui.theme.components.CampoTexto
import com.example.induzteckapp.ui.theme.components.CasillaConTexto
import com.example.induzteckapp.ui.theme.components.GrupoRadio
import com.example.induzteckapp.ui.theme.components.ImagenEncabezado
import com.example.induzteckapp.ui.theme.components.InterruptorConTexto
import com.example.induzteckapp.ui.theme.components.TarjetaSeccion
import com.example.induzteckapp.ui.theme.theme.Dimens
import com.example.induzteckapp.ui.theme.theme.Exito
import com.example.induzteckapp.ui.theme.theme.ExitoFondo
import com.example.induzteckapp.viewmodel.FormularioViewModel

// =============================================================
// PANTALLA PRINCIPAL
// =============================================================

@Composable
fun FormularioScreen(
    viewModel: FormularioViewModel = viewModel()
) {
    // Observamos el estado del formulario.
    // Compose actualizará la pantalla cuando cambie el StateFlow.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FormularioContent(
        uiState = uiState,
        onRolSeleccionado = viewModel::onRolSeleccionado,
        onEsEmpresaCambia = viewModel::onEsEmpresaCambia,
        onRutCambia = viewModel::onRutCambia,
        onNombreCambia = viewModel::onNombreCambia,
        onApellidoCambia = viewModel::onApellidoCambia,
        onGiroCambia = viewModel::onGiroCambia,
        onContactoNombreCambia = viewModel::onContactoNombreCambia,
        onContactoCargoCambia = viewModel::onContactoCargoCambia,
        onCorreoCambia = viewModel::onCorreoCambia,
        onTelefonoCambia = viewModel::onTelefonoCambia,
        onContrasenaCambia = viewModel::onContrasenaCambia,
        onConfirmarContrasenaCambia =
            viewModel::onConfirmarContrasenaCambia,
        onAlternarVisibilidad = viewModel::onAlternarVisibilidad,
        onAlternarVisibilidadConfirmacion =
            viewModel::onAlternarVisibilidadConfirmacion,
        onAceptaTerminos = viewModel::onAceptaTerminos,
        onLimpiar = viewModel::onLimpiar,
        onRegistrar = viewModel::onRegistrar
    )
}


// =============================================================
// CONTENIDO VISUAL DEL FORMULARIO
// =============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioContent(
    uiState: FormularioUiState,

    onRolSeleccionado: (RolUsuario) -> Unit,
    onEsEmpresaCambia: (Boolean) -> Unit,

    onRutCambia: (String) -> Unit,
    onNombreCambia: (String) -> Unit,
    onApellidoCambia: (String) -> Unit,

    onGiroCambia: (String) -> Unit,
    onContactoNombreCambia: (String) -> Unit,
    onContactoCargoCambia: (String) -> Unit,

    onCorreoCambia: (String) -> Unit,
    onTelefonoCambia: (String) -> Unit,

    onContrasenaCambia: (String) -> Unit,
    onConfirmarContrasenaCambia: (String) -> Unit,

    onAlternarVisibilidad: () -> Unit,
    onAlternarVisibilidadConfirmacion: () -> Unit,

    onAceptaTerminos: (Boolean) -> Unit,
    onLimpiar: () -> Unit,
    onRegistrar: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Registro de usuario")
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
        ) {

            // -------------------------------------------------
            // ENCABEZADO
            // -------------------------------------------------

            ImagenEncabezado(
                imagen = R.drawable.banner_registro,
                descripcion = "Registro de usuario de Induzteck",
                titulo = "Bienvenido a Induzteck",
                subtitulo = "Crea tu cuenta para acceder a nuestros servicios"
            )

            Column(
                modifier = Modifier.padding(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(
                    Dimens.espacioMedio
                )
            ) {

                // -------------------------------------------------
                // CARD 1: TIPO DE USUARIO
                // -------------------------------------------------

                TarjetaSeccion(
                    titulo = "Tipo de usuario"
                ) {
                    Text(
                        text = "Selecciona el tipo de cuenta que deseas crear.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    GrupoRadio(
                        opciones = RolUsuario.entries,
                        seleccionada = uiState.rol,
                        onSeleccionar = { rol ->
                            if (rol != null) {
                                onRolSeleccionado(rol)
                            }
                        },
                        textoDe = { rol ->
                            when (rol) {
                                RolUsuario.CLIENTE -> "Cliente"
                                RolUsuario.TECNICO_INGENIERO ->
                                    "Técnico / Ingeniero"
                                null -> "Selecciona un rol"
                            }
                        }
                    )

                    uiState.errorRol?.let { mensaje ->
                        ErrorFormulario(mensaje)
                    }
                }


                // -------------------------------------------------
                // CARD 2: IDENTIFICACIÓN
                // -------------------------------------------------

                TarjetaSeccion(
                    titulo = "Datos de identificación"
                ) {

                    // El RUT se solicita a todos los usuarios.
                    CampoTexto(
                        valor = uiState.rut,
                        onValorCambia = onRutCambia,
                        etiqueta = "RUT (sin puntos)",
                        icono = R.drawable.ic_persona,
                        error = uiState.errorRut
                    )

                    // Para empresas, este campo representa la
                    // razón social. Para personas, el nombre.
                    CampoTexto(
                        valor = uiState.nombre,
                        onValorCambia = onNombreCambia,
                        etiqueta = if (uiState.esEmpresa) {
                            "Razón social"
                        } else {
                            "Nombre"
                        },
                        icono = R.drawable.ic_persona,
                        error = uiState.errorNombre
                    )

                    // La opción empresa solo se presenta a clientes.
                    if (uiState.rol == RolUsuario.CLIENTE) {

                        InterruptorConTexto(
                            texto = "Registrar una empresa",
                            activado = uiState.esEmpresa,
                            onCambio = onEsEmpresaCambia
                        )
                    }

                    // Las personas naturales y los técnicos necesitan
                    // apellido; las empresas utilizan razón social.
                    if (
                        uiState.rol == RolUsuario.TECNICO_INGENIERO ||
                        (
                                uiState.rol == RolUsuario.CLIENTE &&
                                        !uiState.esEmpresa
                                )
                    ) {
                        CampoTexto(
                            valor = uiState.apellido,
                            onValorCambia = onApellidoCambia,
                            etiqueta = "Apellido",
                            icono = R.drawable.ic_persona,
                            error = uiState.errorApellido
                        )
                    }

                    // Datos comerciales exclusivos de una empresa cliente.
                    if (
                        uiState.rol == RolUsuario.CLIENTE &&
                        uiState.esEmpresa
                    ) {

                        CampoTexto(
                            valor = uiState.giro,
                            onValorCambia = onGiroCambia,
                            etiqueta = "Giro comercial",
                            icono = R.drawable.ic_persona,
                            error = uiState.errorGiro
                        )

                        CampoTexto(
                            valor = uiState.contactoNombre,
                            onValorCambia = onContactoNombreCambia,
                            etiqueta = "Nombre del contacto",
                            icono = R.drawable.ic_persona,
                            error = uiState.errorContactoNombre
                        )

                        CampoTexto(
                            valor = uiState.contactoCargo,
                            onValorCambia = onContactoCargoCambia,
                            etiqueta = "Cargo del contacto",
                            icono = R.drawable.ic_persona,
                            error = uiState.errorContactoCargo
                        )
                    }
                }


                // -------------------------------------------------
                // CARD 3: DATOS DE CONTACTO
                // -------------------------------------------------

                TarjetaSeccion(
                    titulo = "Datos de contacto"
                ) {

                    CampoTexto(
                        valor = uiState.correo,
                        onValorCambia = onCorreoCambia,
                        etiqueta = "Correo electrónico",
                        icono = R.drawable.ic_correo,
                        error = uiState.errorCorreo,
                        tipoTeclado = KeyboardType.Email
                    )

                    CampoTexto(
                        valor = uiState.telefono,
                        onValorCambia = onTelefonoCambia,
                        etiqueta = "Teléfono (solo números)",
                        icono = R.drawable.ic_persona,
                        error = uiState.errorTelefono,
                        tipoTeclado = KeyboardType.Phone
                    )
                }


                // -------------------------------------------------
                // CARD 4: SEGURIDAD DE LA CUENTA
                // -------------------------------------------------

                TarjetaSeccion(
                    titulo = "Seguridad de la cuenta"
                ) {

                    CampoContrasena(
                        valor = uiState.contrasena,
                        onValorCambia = onContrasenaCambia,
                        etiqueta = "Contraseña",
                        visible = uiState.contrasenaVisible,
                        onAlternarVisible = onAlternarVisibilidad,
                        error = uiState.errorContrasena
                    )

                    Text(
                        text = "Debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un símbolo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    CampoContrasena(
                        valor = uiState.confirmarContrasena,
                        onValorCambia = onConfirmarContrasenaCambia,
                        etiqueta = "Confirmar contraseña",
                        visible = uiState.confirmarContrasenaVisible,
                        onAlternarVisible =
                            onAlternarVisibilidadConfirmacion,
                        error = uiState.errorConfirmarContrasena
                    )
                }


                // -------------------------------------------------
                // TÉRMINOS Y CONDICIONES
                // -------------------------------------------------

                Column(
                    verticalArrangement = Arrangement.spacedBy(
                        Dimens.espacioChico
                    )
                ) {

                    CasillaConTexto(
                        texto = "Acepto los términos y condiciones",
                        marcado = uiState.aceptaTerminos,
                        onCambio = onAceptaTerminos
                    )

                    uiState.errorTermino?.let { mensaje ->
                        ErrorFormulario(mensaje)
                    }
                }


                // -------------------------------------------------
                // BOTONES DE ACCIÓN
                // -------------------------------------------------

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        Dimens.espacioMedio
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    BotonSecundario(
                        texto = "Limpiar",
                        onClick = onLimpiar,
                        modifier = Modifier.weight(1f)
                    )

                    BotonPrincipal(
                        texto = "Registrar",
                        onClick = onRegistrar,
                        icono = R.drawable.ic_check,
                        modifier = Modifier.weight(1f)
                    )
                }


                // -------------------------------------------------
                // RESULTADO DE LA VALIDACIÓN
                // -------------------------------------------------

                if (uiState.registroExitoso) {
                    MensajeExito(
                        nombre = uiState.nombre
                    )
                }
            }
        }
    }
}


// =============================================================
// MENSAJE DE ERROR REUTILIZABLE
// =============================================================

@Composable
private fun ErrorFormulario(
    mensaje: String
) {
    Text(
        text = mensaje,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(
            start = Dimens.espacioMedio
        )
    )
}


// =============================================================
// MENSAJE DE REGISTRO EXITOSO
// =============================================================

@Composable
private fun MensajeExito(
    nombre: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ExitoFondo,
            contentColor = Exito
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.espacioMedio),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                Dimens.espacioChico
            )
        ) {

            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = Exito,
                modifier = Modifier.size(Dimens.tamanoIcono)
            )

            Text(
                text = "¡Registro validado correctamente, $nombre!",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}



/*
package com.example.induzteckapp.ui.theme.screens

// com.example.induzteckapp/screens/FormularioScreen.kt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induzteckapp.R
import com.example.induzteckapp.model.FormularioUiState
import com.example.induzteckapp.model.NivelExperiencia
import com.example.induzteckapp.ui.theme.components.BotonPrincipal
import com.example.induzteckapp.ui.theme.components.BotonSecundario
import com.example.induzteckapp.ui.theme.components.CampoContrasena
import com.example.induzteckapp.ui.theme.components.CampoTexto
import com.example.induzteckapp.ui.theme.components.CasillaConTexto
import com.example.induzteckapp.ui.theme.components.GrupoRadio
import com.example.induzteckapp.ui.theme.components.ImagenEncabezado
import com.example.induzteckapp.ui.theme.components.InterruptorConTexto
import com.example.induzteckapp.ui.theme.components.TarjetaSeccion
import com.example.induzteckapp.ui.theme.theme.Dimens
import com.example.induzteckapp.ui.theme.theme.Exito
import com.example.induzteckapp.ui.theme.theme.ExitoFondo
import com.example.induzteckapp.viewmodel.FormularioViewModel


// Constructor: Instanciará la interfaz :)

@Composable
fun FormularioScreen(
    viewModel: FormularioViewModel = viewModel() // Se cree una vez y sobreviva
){

    // Stateflow y cada vez que lo mute / redibujar pantalla
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FormularioContent(

        uiState = uiState,
        onNombreCambia = viewModel:: onNombreCambia,
        onCorreoCambia = viewModel:: onCorreoCambia,
        onContrasenaCambia  = viewModel:: onContrasenaCambia,
        onAlternarVisibilidad = viewModel:: onAlternarVisibilidad,
        onNivelSeleccionado = viewModel:: onNivelSeleccionado,
        onNoticiasCambia = viewModel:: onNoticiasCambia,
        onAceptaTerminos = viewModel:: onAceptaTerminos,
        onLimpiar = viewModel:: onLimpiar,
        onRegistrar  = viewModel:: onRegistrar

    )

}


// Esta función creará interfaz grafica:
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioContent(

    uiState: FormularioUiState,

    // con las parentesis, indicamos que la variable esl del tipo Función
    // Unit : equivalente a void, significa que no retorna nada

    onNombreCambia: (String) -> Unit,
    onCorreoCambia: (String) -> Unit,
    onContrasenaCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onNivelSeleccionado: (NivelExperiencia) -> Unit,
    onNoticiasCambia: (Boolean) -> Unit,
    onAceptaTerminos: (Boolean) -> Unit,
    onLimpiar: () -> Unit,
    onRegistrar: () -> Unit

){
    // Scaffold: Nos permite organizar los elementos en pantalla:
    // auotmaticamente dibujará un header y un footer
    // ajustará los elementos dentro de ese panel

    Scaffold(

        topBar = {

            TopAppBar(
                title = { Text(stringResource(R.string.titulo_formulario)) },
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

        ) {

            ImagenEncabezado(

                imagen = R.drawable.banner_registro,
                descripcion = stringResource(R.string.cd_banner),
                titulo = stringResource(R.string.banner_titulo),
                subtitulo = stringResource(R.string.banner_subtitulo),

            )

            Column(
                modifier = Modifier.padding(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)

            ) {

                // CARD 1 - DATOS PERSONALES

                TarjetaSeccion(titulo = stringResource(R.string.seccion_datos)) {

                    CampoTexto(

                        valor = uiState.nombre,
                        onValorCambia = onNombreCambia,
                        etiqueta = stringResource(R.string.campo_nombre),
                        icono = R.drawable.ic_persona,
                        error = uiState.errorNombre

                    )

                    CampoTexto(

                        valor = uiState.correo,
                        onValorCambia = onCorreoCambia,
                        etiqueta = stringResource(R.string.campo_correo),
                        icono = R.drawable.ic_correo,
                        error = uiState.errorCorreo,
                        tipoTeclado = KeyboardType.Email

                    )

                    CampoContrasena(

                        valor = uiState.contrasena,
                        onValorCambia = onContrasenaCambia,
                        etiqueta = stringResource(R.string.campo_contrasena),
                        visible = uiState.contrasenaVisible,
                        onAlternarVisible = onAlternarVisibilidad,
                        error = uiState.errorContrasena
                    )

                }

                // CARD 2 - PREFERENCIAS
                TarjetaSeccion(titulo = stringResource(R.string.seccion_preferencias)) {

                    Text(
                        text = stringResource(R.string.seccion_preferencias),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    GrupoRadio(

                        opciones = NivelExperiencia.entries,
                        seleccionada = uiState.nivel,
                        onSeleccionar = onNivelSeleccionado,
                        textoDe = {it.etiqueta}

                    )

                    InterruptorConTexto(

                        texto = stringResource(R.string.opcion_noticias),
                        activado = uiState.recibirNoticias,
                        onCambio = onNoticiasCambia

                    )
                }

                // Terminos y Condiciones:

                Column() {
                    CasillaConTexto(

                        texto = stringResource(R.string.opcion_terminos),
                        marcado = uiState.aceptaTerminos,
                        onCambio = onAceptaTerminos
                    )
                    uiState.errorTermino?.let{mensaje ->
                        Text(

                            text = mensaje,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = Dimens.espacioMedio)

                        )
                    }
                }

                // Botones
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)) {

                    BotonSecundario(

                        texto = stringResource(R.string.btn_limpiar),
                        onClick = onLimpiar,
                        modifier = Modifier.weight(1f)
                    )

                    BotonPrincipal(

                        texto = stringResource(R.string.btn_registrar),
                        onClick = onRegistrar,
                        icono = R.drawable.ic_check,
                        modifier = Modifier.weight(1f)
                    )
                }

                if(uiState.registroExitoso){
                    MensajeExito(uiState.nombre)
                }

            }
        }
    }

}


@Composable
private fun MensajeExito(nombre : String){

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ExitoFondo, contentColor = Exito)

    ){
        Row(
            modifier = Modifier.padding(start = Dimens.espacioChico),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(

                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = Exito,
                modifier = Modifier.size(Dimens.tamanoIcono)
            )

            Text(

                text = stringResource(R.string.registro_exitoso, nombre),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = Dimens.espacioChico)

            )
        }

    }


}


*/













