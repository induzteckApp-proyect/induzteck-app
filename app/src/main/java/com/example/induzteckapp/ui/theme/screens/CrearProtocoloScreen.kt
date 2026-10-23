// com.example.induzteckapp/ui/theme/screens/CrearProtocoloScreen.kt
package com.example.induzteckapp.ui.theme.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induzteckapp.R
import com.example.induzteckapp.model.ClienteProtocolo
import com.example.induzteckapp.model.Comuna
import com.example.induzteckapp.model.CrearProtocoloUiState
import com.example.induzteckapp.model.Equipo
import com.example.induzteckapp.model.Instalacion
import com.example.induzteckapp.model.TipoEquipo
import com.example.induzteckapp.model.TipoPrueba
import com.example.induzteckapp.ui.theme.components.BotonPrincipal
import com.example.induzteckapp.ui.theme.components.BotonSecundario
import com.example.induzteckapp.ui.theme.components.TarjetaSeccion
import com.example.induzteckapp.ui.theme.theme.MiRegistroTheme
import com.example.induzteckapp.viewmodel.CrearProtocoloViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearProtocoloScreen(
    onVolver: () -> Unit
) {
    // El ViewModel se conserva mientras viva esta entrada de navegación.
    val viewModel: CrearProtocoloViewModel = viewModel()
    val estado by viewModel.uiState.collectAsState()

    CrearProtocoloContenido(
        estado = estado,
        onVolver = onVolver,
        onSeleccionarCliente = viewModel::seleccionarCliente,
        onSeleccionarInstalacion = viewModel::seleccionarInstalacion,
        onSeleccionarEquipo = viewModel::seleccionarEquipo,
        onSeleccionarPrueba = viewModel::seleccionarPrueba,
        onTemperaturaCambia = viewModel::actualizarTemperatura,
        onHumedadCambia = viewModel::actualizarHumedad,
        onAvanzar = viewModel::avanzarPaso,
        onRetroceder = viewModel::retrocederPaso,
        onLimpiarMensajes = viewModel::limpiarMensajes,
        onCrearInstalacion = viewModel::crearInstalacion,
        onCrearEquipo = viewModel::crearEquipo
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CrearProtocoloContenido(
    estado: CrearProtocoloUiState,
    onVolver: () -> Unit,
    onSeleccionarCliente: (ClienteProtocolo) -> Unit,
    onSeleccionarInstalacion: (Instalacion) -> Unit,
    onSeleccionarEquipo: (Equipo) -> Unit,
    onSeleccionarPrueba: (TipoPrueba) -> Unit,
    onTemperaturaCambia: (String) -> Unit,
    onHumedadCambia: (String) -> Unit,
    onAvanzar: () -> Unit,
    onRetroceder: () -> Unit,
    onLimpiarMensajes: () -> Unit,
    onCrearInstalacion: (String, String?, Long?, String?) -> Unit,
    onCrearEquipo: (
        String, Long?, String?, String?, String?,
        Double?, Double?, Double?, Double?
    ) -> Unit
) {
    var mostrarNuevaInstalacion by rememberSaveable {
        mutableStateOf(false)
    }

    var mostrarNuevoEquipo by rememberSaveable {
        mutableStateOf(false)
    }

    val titulosPasos = listOf(
        "Contexto de terreno",
        "Condiciones ambientales",
        "Mediciones"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Nuevo protocolo")
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = "Volver al inicio"
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
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Encabezado.
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⚡ Registro de pruebas eléctricas",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Completa los datos del terreno y registra las mediciones del equipo.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Progreso vinculado al estado real del ViewModel.
            TarjetaSeccion(titulo = "Progreso del protocolo") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    titulosPasos.forEachIndexed { indice, titulo ->
                        val numeroPaso = indice + 1
                        val actual = estado.pasoActual == numeroPaso
                        val completado = estado.pasoActual > numeroPaso

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (completado) {
                                    "✓"
                                } else {
                                    numeroPaso.toString()
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (actual || completado) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.padding(4.dp)
                            )

                            HorizontalDivider(
                                color = if (actual || completado) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                },
                                thickness = if (actual) 3.dp else 1.dp
                            )

                            Text(
                                text = titulo,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (actual) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                fontWeight = if (actual) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                },
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Estado de carga y mensajes del ViewModel.
            if (estado.cargando || estado.guardando) {
                Text(
                    text = "Procesando información…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            estado.mensajeError?.let { mensaje ->
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            estado.mensajeExito?.let { mensaje ->
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Contenido correspondiente al paso actual.
            when (estado.pasoActual) {
                1 -> {
                    TarjetaSeccion(
                        titulo = "1. Contexto de terreno"
                    ) {
                        Text(
                            text = "Identifica al cliente y selecciona la instalación y el equipo que se someterá a prueba.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        HorizontalDivider()

                        SelectorProtocolo(
                            etiqueta = "Cliente",
                            seleccionada = estado.clienteSeleccionado
                                ?.nombreDesplegable.orEmpty(),
                            opciones = estado.clientes,
                            texto = { it.nombreDesplegable },
                            habilitado = !estado.cargando &&
                                    !estado.guardando,
                            alSeleccionar = onSeleccionarCliente
                        )

                        SelectorProtocolo(
                            etiqueta = "Instalación",
                            seleccionada = estado.instalacionSeleccionada?.let {
                                "${it.nombre} — ${it.nombreComuna}"
                            }.orEmpty(),
                            opciones = estado.instalaciones,
                            texto = {
                                "${it.nombre} — ${it.nombreComuna}"
                            },
                            habilitado = estado.clienteSeleccionado != null &&
                                    !estado.cargando &&
                                    !estado.guardando,
                            alSeleccionar = onSeleccionarInstalacion
                        )

                        BotonSecundario(
                            texto = "+ Nueva instalación",
                            onClick = {
                                onLimpiarMensajes()
                                mostrarNuevaInstalacion = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        SelectorProtocolo(
                            etiqueta = "Equipo",
                            seleccionada = estado.equipoSeleccionado?.let {
                                "${it.tag} — ${it.nombreTipoEquipo}"
                            }.orEmpty(),
                            opciones = estado.equipos,
                            texto = {
                                "${it.tag} — ${it.nombreTipoEquipo}"
                            },
                            habilitado = estado.instalacionSeleccionada != null &&
                                    !estado.cargando &&
                                    !estado.guardando,
                            alSeleccionar = onSeleccionarEquipo
                        )

                        BotonSecundario(
                            texto = "+ Nuevo equipo",
                            onClick = {
                                onLimpiarMensajes()
                                mostrarNuevoEquipo = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        estado.equipoSeleccionado?.let { equipo ->
                            HorizontalDivider()

                            Text(
                                text = "Equipo seleccionado",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "${equipo.tag} — ${equipo.nombreTipoEquipo}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            equipo.marca?.let {
                                Text(
                                    text = "Marca: $it",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            equipo.modelo?.let {
                                Text(
                                    text = "Modelo: $it",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                2 -> {
                    TarjetaSeccion(
                        titulo = "2. Condiciones ambientales"
                    ) {
                        Text(
                            text = "La fecha y la hora se registrarán al guardar el protocolo.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = estado.temperaturaC,
                            onValueChange = onTemperaturaCambia,
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text("Temperatura ambiente (°C)")
                            },
                            supportingText = {
                                Text("Rango permitido: -10,0 a 60,0 °C")
                            },
                            singleLine = true,
                            enabled = !estado.cargando &&
                                    !estado.guardando
                        )

                        OutlinedTextField(
                            value = estado.humedadPct,
                            onValueChange = onHumedadCambia,
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text("Humedad relativa (%)")
                            },
                            supportingText = {
                                Text("Rango permitido: 0,0 a 100,0 %")
                            },
                            singleLine = true,
                            enabled = !estado.cargando &&
                                    !estado.guardando
                        )
                    }
                }

                3 -> {
                    TarjetaSeccion(
                        titulo = "3. Mediciones eléctricas"
                    ) {
                        Text(
                            text = "Selecciona la prueba que se realizará al equipo.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        estado.equipoSeleccionado?.let { equipo ->
                            Text(
                                text = "Equipo: ${equipo.tag} — ${equipo.nombreTipoEquipo}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        SelectorProtocolo(
                            etiqueta = "Tipo de prueba",
                            seleccionada = estado.pruebaSeleccionada
                                ?.nombreDesplegable.orEmpty(),
                            opciones = estado.pruebasCompatibles,
                            texto = { it.nombreDesplegable },
                            habilitado = !estado.cargando &&
                                    !estado.guardando,
                            alSeleccionar = onSeleccionarPrueba
                        )

                        if (estado.equipoSeleccionado != null &&
                            estado.pruebasCompatibles.isEmpty()
                        ) {
                            Text(
                                text = "No hay pruebas compatibles disponibles para este equipo.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Text(
                            text = "Los campos de medición y sus validaciones se incorporarán según el tipo de equipo y la prueba elegida.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Navegación entre pasos mediante el ViewModel.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (estado.pasoActual == 1) {
                    BotonSecundario(
                        texto = "Cancelar",
                        onClick = onVolver,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    BotonSecundario(
                        texto = "Anterior",
                        onClick = {
                            onLimpiarMensajes()
                            onRetroceder()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (estado.pasoActual < 3) {
                    BotonPrincipal(
                        texto = "Continuar",
                        onClick = onAvanzar,
                        modifier = Modifier.weight(1f),
                        habilitado = !estado.cargando &&
                                !estado.guardando
                    )
                } else {
                    BotonPrincipal(
                        texto = "Confirmar protocolo",
                        onClick = {
                            // El guardado transaccional se implementará después.
                        },
                        modifier = Modifier.weight(1f),
                        habilitado = false
                    )
                }
            }

            Text(
                text = "Los datos del protocolo se validarán antes de guardarse localmente.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Diálogo para crear una instalación desde el protocolo.
    if (mostrarNuevaInstalacion) {
        DialogoNuevaInstalacion(
            comunas = estado.comunas,
            alCancelar = {
                mostrarNuevaInstalacion = false
            },
            alGuardar = { nombre, ubicacion, idComuna, observaciones ->
                mostrarNuevaInstalacion = false
                onCrearInstalacion(
                    nombre,
                    ubicacion,
                    idComuna,
                    observaciones
                )
            }
        )
    }

    // Diálogo para crear un equipo en la instalación seleccionada.
    if (mostrarNuevoEquipo) {
        DialogoNuevoEquipo(
            tiposEquipo = estado.tiposEquipo,
            alCancelar = {
                mostrarNuevoEquipo = false
            },
            alGuardar = { tag, tipo, marca, modelo, serie,
                          potencia, tensiones, corriente, maxContactos ->
                mostrarNuevoEquipo = false

                onCrearEquipo(
                    tag,
                    tipo,
                    marca,
                    modelo,
                    serie,
                    potencia,
                    tensiones,
                    corriente,
                    maxContactos
                )
            }
        )
    }
}

@Composable
private fun <T> SelectorProtocolo(
    etiqueta: String,
    seleccionada: String,
    opciones: List<T>,
    texto: (T) -> String,
    habilitado: Boolean = true,
    alSeleccionar: (T) -> Unit
) {
    var expandido by remember {
        mutableStateOf(false)
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = seleccionada,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = habilitado) {
                    expandido = true
                },
            label = {
                Text(etiqueta)
            },
            readOnly = true,
            enabled = habilitado,
            trailingIcon = {
                TextButton(
                    onClick = {
                        expandido = !expandido
                    },
                    enabled = habilitado
                ) {
                    Text("⌄")
                }
            }
        )

        DropdownMenu(
            expanded = expandido,
            onDismissRequest = {
                expandido = false
            },
            modifier = Modifier.heightIn(max = 280.dp)
        ) {
            if (opciones.isEmpty()) {
                DropdownMenuItem(
                    text = {
                        Text("No hay opciones disponibles")
                    },
                    onClick = {
                        expandido = false
                    },
                    enabled = false
                )
            } else {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = {
                            Text(texto(opcion))
                        },
                        onClick = {
                            expandido = false
                            alSeleccionar(opcion)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DialogoNuevaInstalacion(
    comunas: List<Comuna>,
    alCancelar: () -> Unit,
    alGuardar: (String, String?, Long?, String?) -> Unit
) {
    var nombre by rememberSaveable {
        mutableStateOf("")
    }

    var ubicacion by rememberSaveable {
        mutableStateOf("")
    }

    var comunaSeleccionada by remember {
        mutableStateOf<Comuna?>(null)
    }

    var observaciones by rememberSaveable {
        mutableStateOf("")
    }

    AlertDialog(
        onDismissRequest = alCancelar,
        title = {
            Text("Nueva instalación")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                    },
                    label = {
                        Text("Nombre de la instalación *")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = {
                        ubicacion = it
                    },
                    label = {
                        Text("Dirección o ubicación")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                SelectorProtocolo(
                    etiqueta = "Comuna *",
                    seleccionada = comunaSeleccionada
                        ?.nombreDesplegable.orEmpty(),
                    opciones = comunas,
                    texto = {
                        it.nombreDesplegable
                    },
                    alSeleccionar = {
                        comunaSeleccionada = it
                    }
                )

                OutlinedTextField(
                    value = observaciones,
                    onValueChange = {
                        observaciones = it
                    },
                    label = {
                        Text("Observaciones de acceso")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "* Campos obligatorios",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nombre.isNotBlank() &&
                        comunaSeleccionada != null
                    ) {
                        alGuardar(
                            nombre.trim(),
                            ubicacion.trim().ifBlank { null },
                            comunaSeleccionada?.idComuna,
                            observaciones.trim().ifBlank { null }
                        )
                    }
                },
                enabled = nombre.isNotBlank() &&
                        comunaSeleccionada != null
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = alCancelar) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun DialogoNuevoEquipo(
    tiposEquipo: List<TipoEquipo>,
    alCancelar: () -> Unit,
    alGuardar: (
        String, Long?, String?, String?, String?,
        Double?, Double?, Double?, Double?
    ) -> Unit
) {
    var tag by rememberSaveable {
        mutableStateOf("")
    }

    var tipoSeleccionado by remember {
        mutableStateOf<TipoEquipo?>(null)
    }

    var marca by rememberSaveable {
        mutableStateOf("")
    }

    var modelo by rememberSaveable {
        mutableStateOf("")
    }

    var numeroSerie by rememberSaveable {
        mutableStateOf("")
    }

    var potencia by rememberSaveable {
        mutableStateOf("")
    }

    var tensiones by rememberSaveable {
        mutableStateOf("")
    }

    var corriente by rememberSaveable {
        mutableStateOf("")
    }

    var maxContactos by rememberSaveable {
        mutableStateOf("")
    }

    var errorNumerico by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    AlertDialog(
        onDismissRequest = alCancelar,
        title = {
            Text("Nuevo equipo")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = tag,
                    onValueChange = {
                        tag = it
                    },
                    label = {
                        Text("TAG del equipo *")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                SelectorProtocolo(
                    etiqueta = "Tipo de equipo *",
                    seleccionada = tipoSeleccionado?.nombre.orEmpty(),
                    opciones = tiposEquipo,
                    texto = {
                        it.nombre
                    },
                    alSeleccionar = {
                        tipoSeleccionado = it
                    }
                )

                OutlinedTextField(
                    value = marca,
                    onValueChange = {
                        marca = it
                    },
                    label = {
                        Text("Marca")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = modelo,
                    onValueChange = {
                        modelo = it
                    },
                    label = {
                        Text("Modelo")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = numeroSerie,
                    onValueChange = {
                        numeroSerie = it
                    },
                    label = {
                        Text("Número de serie")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = potencia,
                    onValueChange = {
                        potencia = it
                        errorNumerico = null
                    },
                    label = {
                        Text("Potencia (kVA)")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = tensiones,
                    onValueChange = {
                        tensiones = it
                        errorNumerico = null
                    },
                    label = {
                        Text("Tensiones de placa")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = corriente,
                    onValueChange = {
                        corriente = it
                        errorNumerico = null
                    },
                    label = {
                        Text("Corriente de placa")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = maxContactos,
                    onValueChange = {
                        maxContactos = it
                        errorNumerico = null
                    },
                    label = {
                        Text("Resistencia máxima de contactos (µΩ)")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                errorNumerico?.let { mensaje ->
                    Text(
                        text = mensaje,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    text = "* Campos obligatorios",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    fun decimalOpcional(valor: String): Double? {
                        if (valor.isBlank()) return null

                        return valor.trim()
                            .replace(',', '.')
                            .toDoubleOrNull()
                    }

                    val camposNumericos = listOf(
                        potencia,
                        tensiones,
                        corriente,
                        maxContactos
                    )

                    val hayNumeroInvalido = camposNumericos.any {
                        it.isNotBlank() && decimalOpcional(it) == null
                    }

                    if (hayNumeroInvalido) {
                        errorNumerico =
                            "Revisa los valores numéricos. Usa números válidos."
                        return@TextButton
                    }

                    if (tag.isBlank() || tipoSeleccionado == null) {
                        return@TextButton
                    }

                    alGuardar(
                        tag.trim(),
                        tipoSeleccionado?.idTipoEquipo,
                        marca.trim().ifBlank { null },
                        modelo.trim().ifBlank { null },
                        numeroSerie.trim().ifBlank { null },
                        decimalOpcional(potencia),
                        decimalOpcional(tensiones),
                        decimalOpcional(corriente),
                        decimalOpcional(maxContactos)
                    )
                },
                enabled = tag.isNotBlank() &&
                        tipoSeleccionado != null
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = alCancelar) {
                Text("Cancelar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun CrearProtocoloContenidoPreview() {
    MiRegistroTheme {
        CrearProtocoloContenido(
            estado = CrearProtocoloUiState(),
            onVolver = {},
            onSeleccionarCliente = {},
            onSeleccionarInstalacion = {},
            onSeleccionarEquipo = {},
            onSeleccionarPrueba = {},
            onTemperaturaCambia = {},
            onHumedadCambia = {},
            onAvanzar = {},
            onRetroceder = {},
            onLimpiarMensajes = {},
            onCrearInstalacion = { _, _, _, _ -> },
            onCrearEquipo = { _, _, _, _, _, _, _, _, _ -> }
        )
    }
}


/*
package com.example.induzteckapp.ui.theme.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.induzteckapp.R
import com.example.induzteckapp.ui.theme.components.BotonPrincipal
import com.example.induzteckapp.ui.theme.components.BotonSecundario
import com.example.induzteckapp.ui.theme.components.TarjetaSeccion
import com.example.induzteckapp.ui.theme.theme.MiRegistroTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearProtocoloScreen(
    onVolver: () -> Unit
) {
    var pasoActual by rememberSaveable { mutableIntStateOf(1) }

    val titulosPasos = listOf(
        "Contexto de terreno",
        "Condiciones ambientales",
        "Mediciones"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Nuevo protocolo")
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = "Volver al inicio"
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
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⚡ Registro de pruebas eléctricas",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Completa los datos del terreno y registra las mediciones del equipo.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            TarjetaSeccion(titulo = "Progreso del protocolo") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    titulosPasos.forEachIndexed { indice, titulo ->
                        val numeroPaso = indice + 1
                        val actual = pasoActual == numeroPaso
                        val completado = pasoActual > numeroPaso

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = when {
                                    completado -> "✓"
                                    else -> numeroPaso.toString()
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (actual || completado) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier
                                    .padding(4.dp)
                            )

                            HorizontalDivider(
                                color = if (actual || completado) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                },
                                thickness = if (actual) 3.dp else 1.dp
                            )

                            Text(
                                text = titulo,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (actual) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                fontWeight = if (actual) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            when (pasoActual) {
                1 -> {
                    TarjetaSeccion(titulo = "1. Contexto de terreno") {
                        Text(
                            text = "Identifica el cliente y selecciona la instalación y el equipo que se someterá a prueba.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        HorizontalDivider()

                        Text(
                            text = "Cliente",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Aquí se cargará el selector de clientes registrados.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Instalación",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Podrás seleccionar una instalación existente o registrar una nueva.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Equipo",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Podrás seleccionar un equipo existente o crear uno para la instalación elegida.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                2 -> {
                    TarjetaSeccion(titulo = "2. Condiciones ambientales") {
                        Text(
                            text = "Se registrarán automáticamente la fecha y la hora del protocolo.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Temperatura ambiente (°C)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Rango permitido: -10,0 a 60,0 °C.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Humedad relativa (%)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Rango permitido: 0,0 a 100,0 %.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                3 -> {
                    TarjetaSeccion(titulo = "3. Mediciones eléctricas") {
                        Text(
                            text = "Los campos se adaptarán al tipo de equipo y a la prueba seleccionada.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Aquí se incorporarán las mediciones, sus unidades y las validaciones correspondientes antes de registrar el protocolo.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pasoActual == 1) {
                    BotonSecundario(
                        texto = "Cancelar",
                        onClick = onVolver,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    BotonSecundario(
                        texto = "Anterior",
                        onClick = { pasoActual-- },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (pasoActual < 3) {
                    BotonPrincipal(
                        texto = "Continuar",
                        onClick = { pasoActual++ },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    BotonPrincipal(
                        texto = "Confirmar protocolo",
                        onClick = {
                            // Se conectará al guardado real cuando integremos
                            // el ViewModel y la persistencia de SQLite.
                        },
                        modifier = Modifier.weight(1f),
                        habilitado = false
                    )
                }
            }

            Text(
                text = "Los datos del protocolo se validarán antes de guardarse localmente.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CrearProtocoloScreenPreview() {
    MiRegistroTheme {
        CrearProtocoloScreen(onVolver = {})
    }
}

*/

