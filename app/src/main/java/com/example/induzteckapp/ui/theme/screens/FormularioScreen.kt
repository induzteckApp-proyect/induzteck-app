// induzteckApp/ui.theme/screens/FormularioScreen.kt

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

import com.example.induzteckapp.model.Usuario

// =============================================================
// PANTALLA REGISTRO
// =============================================================


// Hay bun defecto que noté en estas pantalla,s y es que No existe boton para retroceder. Es algo urgente a corregir
// implementar boton de regresó implementado por el profesor en su proyecto de MiRegistro.

@Composable
fun FormularioScreen(
    correosRegistrados: Set<String> = emptySet(),
    onRegistroExitoso: (Usuario) -> Unit,
    viewModel: FormularioViewModel = viewModel()
) {
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
        onRegistrar = {
            viewModel.onRegistrar(correosRegistrados)?.let { usuario ->
                onRegistroExitoso(usuario)
            }
        }
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

                        // EN ESTE TIPO DE TEXTO HAY UN ERROR!
                        // en el modo claro, se ven de un color celeste/morado MUUY claro
                        // que no se ve nada. Hay que corregir TODOS los textos que usan esto
                        // y cambiarlo por uno de los colores de la aplicación.
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













