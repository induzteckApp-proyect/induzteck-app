/* nota:
* Debido a la inegración de microservicios, algunas cosas de este Screen quedaron pendientes de ligeras modificaciones de compatibilidad
* SIN EMBARGO, es el diseño de este Screen que se debe usar para cuando el ADMIN cree las cuentas de usuario, solo hay que adaptarlo a los nuevos requisitos
*
* */

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
import com.example.induzteckapp.ui.theme.theme.MiRegistroTheme
import com.example.induzteckapp.viewmodel.FormularioViewModel

@Composable
fun FormularioScreen(
    onVolver: () -> Unit,
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
        onConfirmarContrasenaCambia = viewModel::onConfirmarContrasenaCambia,
        onAlternarVisibilidad = viewModel::onAlternarVisibilidad,
        onAlternarVisibilidadConfirmacion = viewModel::onAlternarVisibilidadConfirmacion,
        onAceptaTerminos = viewModel::onAceptaTerminos,
        onLimpiar = viewModel::onLimpiar,
        onRegistrar = viewModel::onRegistrar,
        onVolver = onVolver
    )
}

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
    onRegistrar: () -> Unit,
    onVolver: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear cuenta de usuario") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = "Volver a administración"
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
                .imePadding() 
                .verticalScroll(rememberScrollState())
        ) {
            ImagenEncabezado(
                imagen = R.drawable.banner_induztek,
                descripcion = "Formulario de creación de cuentas Induztek",
                titulo = "Crear cuenta en Induztek",
                subtitulo = "El usuario recibirá una invitación para activar su cuenta y definir su contraseña"
            )

            Column(
                modifier = Modifier.padding(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                
                
                
                TarjetaSeccion(titulo = "Tipo de usuario") {
                    Text(
                        text = "Selecciona el perfil de cuenta que deseas registrar:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    GrupoRadio(
                        opciones = RolUsuario.entries,
                        seleccionada = uiState.rol,
                        onSeleccionar = { rol -> if (rol != null) onRolSeleccionado(rol) },
                        textoDe = { rol ->
                            when (rol) {
                                RolUsuario.ADMIN -> "Administrador"
                                RolUsuario.CLIENTE -> "Cliente"
                                RolUsuario.TECNICO_INGENIERO -> "Técnico / Ingeniero"
                                else -> ""
                            }
                        }
                    )

                    AnimatedVisibility(
                        visible = uiState.errorRol != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        uiState.errorRol?.let { ErrorFormulario(it) }
                    }
                }

                
                
                
                TarjetaSeccion(titulo = "Datos de identificación") {
                    CampoTexto(
                        valor = uiState.rut,
                        onValorCambia = onRutCambia,
                        etiqueta = "RUT (ej: 12345678-9)",
                        icono = R.drawable.ic_persona,
                        error = uiState.errorRut
                    )

                    CampoTexto(
                        valor = uiState.nombre,
                        onValorCambia = onNombreCambia,
                        etiqueta = if (uiState.esEmpresa) "Razón social (ej: Induzteck SpA)" else "Nombre (ej: Juan)",
                        icono = R.drawable.ic_persona,
                        error = uiState.errorNombre
                    )

                    if (uiState.rol == RolUsuario.CLIENTE) {
                        InterruptorConTexto(
                            texto = "Registrar como persona jurídica (Empresa)",
                            activado = uiState.esEmpresa,
                            onCambio = onEsEmpresaCambia
                        )
                    }

                    if (uiState.rol != null && !(uiState.rol == RolUsuario.CLIENTE && uiState.esEmpresa)) {
                        CampoTexto(
                            valor = uiState.apellido,
                            onValorCambia = onApellidoCambia,
                            etiqueta = "Apellido (ej: Pérez)",
                            icono = R.drawable.ic_persona,
                            error = uiState.errorApellido
                        )
                    }

                    if (uiState.rol == RolUsuario.CLIENTE && uiState.esEmpresa) {
                        CampoTexto(
                            valor = uiState.giro,
                            onValorCambia = onGiroCambia,
                            etiqueta = "Giro comercial (ej: Servicios Eléctricos)",
                            icono = R.drawable.ic_persona,
                            error = uiState.errorGiro
                        )
                        CampoTexto(
                            valor = uiState.contactoNombre,
                            onValorCambia = onContactoNombreCambia,
                            etiqueta = "Nombre del contacto (ej: Pedro González)",
                            icono = R.drawable.ic_persona,
                            error = uiState.errorContactoNombre
                        )
                        CampoTexto(
                            valor = uiState.contactoCargo,
                            onValorCambia = onContactoCargoCambia,
                            etiqueta = "Cargo del contacto (ej: Administrador de Contrato)",
                            icono = R.drawable.ic_persona,
                            error = uiState.errorContactoCargo
                        )
                    }
                }

                
                
                
                TarjetaSeccion(titulo = "Datos de contacto") {
                    CampoTexto(
                        valor = uiState.correo,
                        onValorCambia = onCorreoCambia,
                        etiqueta = "Correo electrónico (ej: usuario@empresa.cl)",
                        icono = R.drawable.ic_correo,
                        error = uiState.errorCorreo,
                        tipoTeclado = KeyboardType.Email
                    )
                    CampoTexto(
                        valor = uiState.telefono,
                        onValorCambia = onTelefonoCambia,
                        etiqueta = "Teléfono de contacto (ej: 56912345678)",
                        icono = R.drawable.ic_persona,
                        error = uiState.errorTelefono,
                        tipoTeclado = KeyboardType.Phone
                    )
                }

                
                
                
                Text(
                    text = "Por seguridad, la contraseña no se crea aquí. El usuario recibirá un correo de activación y deberá definir una contraseña nueva.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMedio),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BotonSecundario(
                        texto = "Limpiar",
                        onClick = onLimpiar,
                        modifier = Modifier.weight(1f)
                    )
                    BotonPrincipal(
                        texto = if (uiState.guardando) "Creando..." else "Crear cuenta",
                        onClick = onRegistrar,
                        habilitado = !uiState.guardando,
                        icono = R.drawable.ic_check,
                        modifier = Modifier.weight(1f)
                    )
                }

                
                
                
                AnimatedVisibility(
                    visible = uiState.registroExitoso,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    MensajeExito(
                        mensaje = uiState.mensajeExito ?: "La cuenta fue creada correctamente.",
                        tokenDesarrollo = uiState.tokenDesarrollo
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorFormulario(mensaje: String) {
    Text(
        text = mensaje,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(start = Dimens.espacioMedio, top = 4.dp)
    )
}

@Composable
private fun MensajeExito(mensaje: String, tokenDesarrollo: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ExitoFondo, contentColor = Exito)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.espacioMedio),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = Exito,
                modifier = Modifier.size(Dimens.tamanoIcono)
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = mensaje, style = MaterialTheme.typography.bodyMedium)
                if (!tokenDesarrollo.isNullOrBlank()) {
                    Text(
                        text = "Token de activación de desarrollo: $tokenDesarrollo",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}




@Preview(showBackground = true)
@Composable
private fun FormularioScreenPreview() {
    MiRegistroTheme {
        FormularioContent(
            uiState = FormularioUiState(
                nombre = "Juan",
                apellido = "Pérez",
                rut = "12345678-9",
                correo = "juan.perez@induzteck.cl"
            ),
            onRolSeleccionado = {},
            onEsEmpresaCambia = {},
            onRutCambia = {},
            onNombreCambia = {},
            onApellidoCambia = {},
            onGiroCambia = {},
            onContactoNombreCambia = {},
            onContactoCargoCambia = {},
            onCorreoCambia = {},
            onTelefonoCambia = {},
            onContrasenaCambia = {},
            onConfirmarContrasenaCambia = {},
            onAlternarVisibilidad = {},
            onAlternarVisibilidadConfirmacion = {},
            onAceptaTerminos = {},
            onLimpiar = {},
            onRegistrar = {},
            onVolver = {}
        )
    }
}

