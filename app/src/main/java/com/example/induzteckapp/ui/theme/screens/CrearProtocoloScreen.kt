// com.example.induzteckapp/ui/theme/screens/CrearProtocoloScreen.kt

package com.example.induzteckapp.ui.theme.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induzteckapp.R
import com.example.induzteckapp.model.Comuna
import com.example.induzteckapp.model.CrearProtocoloUiState
import com.example.induzteckapp.model.Region
import com.example.induzteckapp.model.TipoEquipo
import com.example.induzteckapp.ui.theme.components.BotonPrincipal
import com.example.induzteckapp.ui.theme.components.BotonSecundario
import com.example.induzteckapp.ui.theme.components.TarjetaSeccion
import com.example.induzteckapp.ui.theme.theme.MiRegistroTheme
import com.example.induzteckapp.viewmodel.CrearProtocoloViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearProtocoloScreen(
    onVolver: () -> Unit,
    viewModel: CrearProtocoloViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()

    val titulosPasos = listOf(
        "Contexto de terreno",
        "Condiciones ambientales",
        "Mediciones"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo protocolo") },
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
                .imePadding()
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

                Spacer(Modifier.height(8.dp))

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
                        val numero = indice + 1
                        val actual = estado.pasoActual == numero
                        val completado = estado.pasoActual > numero

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (completado) "✓" else numero.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (actual || completado) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
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

            if (estado.cargando) {
                Text(
                    text = "Cargando datos...",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            estado.mensajeError?.let { mensaje ->
                MensajeEstado(mensaje, esError = true)
            }

            estado.mensajeExito?.let { mensaje ->
                MensajeEstado(mensaje, esError = false)
            }

            when (estado.pasoActual) {
                1 -> PasoContexto(
                    estado = estado,
                    viewModel = viewModel
                )

                2 -> PasoAmbiental(
                    estado = estado,
                    viewModel = viewModel
                )

                3 -> PasoMediciones(
                    estado = estado,
                    viewModel = viewModel
                )
            }

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
                        onClick = viewModel::retrocederPaso,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (estado.pasoActual < 3) {
                    BotonPrincipal(
                        texto = "Continuar",
                        onClick = viewModel::avanzarPaso,
                        modifier = Modifier.weight(1f),
                        habilitado = !estado.cargando && !estado.guardando
                    )
                } else {
                    BotonPrincipal(
                        texto = if (estado.guardando) {
                            "Registrando..."
                        } else {
                            "Confirmar protocolo"
                        },
                        onClick = viewModel::guardarProtocolo,
                        modifier = Modifier.weight(1f),
                        habilitado = !estado.guardando && !estado.cargando
                    )
                }
            }

            Text(
                text = "La fecha y hora se registran al confirmar el protocolo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PasoContexto(
    estado: CrearProtocoloUiState,
    viewModel: CrearProtocoloViewModel
) {
    TarjetaSeccion(titulo = "1. Contexto de terreno") {
        Text(
            text = "Selecciona el cliente, la instalación, el equipo y la prueba que realizarás.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider()

        Selector(
            etiqueta = "Cliente",
            seleccionado = estado.clienteSeleccionado?.nombreDesplegable.orEmpty(),
            opciones = estado.clientes.map { it.nombreDesplegable },
            habilitado = !estado.cargando && !estado.guardando,
            alSeleccionar = { indice ->
                viewModel.seleccionarCliente(estado.clientes[indice])
            }
        )

        if (estado.clienteSeleccionado != null) {
            Selector(
                etiqueta = "Instalación",
                seleccionado = estado.instalacionSeleccionada?.let {
                    "${it.nombre} — ${it.nombreComuna}"
                }.orEmpty(),
                opciones = estado.instalaciones.map {
                    "${it.nombre} — ${it.nombreComuna}"
                },
                habilitado = !estado.guardando,
                alSeleccionar = { indice ->
                    viewModel.seleccionarInstalacion(
                        estado.instalaciones[indice]
                    )
                }
            )

            TextButton(
                onClick = {
                    viewModel.mostrarFormularioInstalacion(
                        !estado.mostrarFormularioInstalacion
                    )
                }
            ) {
                Text(
                    if (estado.mostrarFormularioInstalacion) {
                        "Cancelar nueva instalación"
                    } else {
                        "+ Registrar instalación"
                    }
                )
            }

            if (estado.mostrarFormularioInstalacion) {
                FormularioInstalacion(estado, viewModel)
            }
        }

        if (estado.instalacionSeleccionada != null) {
            Selector(
                etiqueta = "Equipo",
                seleccionado = estado.equipoSeleccionado?.let {
                    "${it.tag} — ${it.nombreTipoEquipo}"
                }.orEmpty(),
                opciones = estado.equipos.map {
                    "${it.tag} — ${it.nombreTipoEquipo}"
                },
                habilitado = !estado.guardando,
                alSeleccionar = { indice ->
                    viewModel.seleccionarEquipo(estado.equipos[indice])
                }
            )

            TextButton(
                onClick = {
                    viewModel.mostrarFormularioEquipo(
                        !estado.mostrarFormularioEquipo
                    )
                }
            ) {
                Text(
                    if (estado.mostrarFormularioEquipo) {
                        "Cancelar nuevo equipo"
                    } else {
                        "+ Registrar equipo"
                    }
                )
            }

            if (estado.mostrarFormularioEquipo) {
                FormularioEquipo(estado, viewModel)
            }
        }

        if (estado.equipoSeleccionado != null) {
            Selector(
                etiqueta = "Prueba eléctrica",
                seleccionado = estado.pruebaSeleccionada?.nombre.orEmpty(),
                opciones = estado.pruebasCompatibles.map { it.nombre },
                habilitado = !estado.guardando,
                alSeleccionar = { indice ->
                    viewModel.seleccionarPrueba(
                        estado.pruebasCompatibles[indice]
                    )
                }
            )
        }
    }
}

@Composable
private fun FormularioInstalacion(
    estado: CrearProtocoloUiState,
    viewModel: CrearProtocoloViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Nueva instalación",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        CampoTexto(
            etiqueta = "Nombre de la instalación *",
            valor = estado.nombreInstalacion,
            alCambiar = viewModel::actualizarNombreInstalacion
        )

        CampoTexto(
            etiqueta = "Ubicación o dirección *",
            valor = estado.ubicacionInstalacion,
            alCambiar = viewModel::actualizarUbicacionInstalacion
        )

        Selector(
            etiqueta = "Región *",
            seleccionado = estado.regionSeleccionada?.nombre.orEmpty(),
            opciones = estado.regiones.map { it.nombre },
            alSeleccionar = { indice ->
                viewModel.seleccionarRegion(estado.regiones[indice])
            }
        )

        val comunasFiltradas = estado.regionSeleccionada?.let { region ->
            estado.comunas.filter { it.idRegion == region.idRegion }
        }.orEmpty()

        Selector(
            etiqueta = "Comuna *",
            seleccionado = estado.comunaSeleccionada?.nombre.orEmpty(),
            opciones = comunasFiltradas.map { it.nombre },
            habilitado = estado.regionSeleccionada != null,
            alSeleccionar = { indice ->
                viewModel.seleccionarComuna(comunasFiltradas[indice])
            }
        )

        CampoTexto(
            etiqueta = "Observaciones de acceso (opcional)",
            valor = estado.observacionesAcceso,
            alCambiar = viewModel::actualizarObservacionesAcceso,
            unaLinea = false
        )

        Text(
            text = "La instalación se guardará en SQLite al pulsar el botón.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        BotonPrincipal(
            texto = if (estado.guardando) "Guardando..." else "Guardar instalación",
            onClick = viewModel::crearInstalacion,
            modifier = Modifier.fillMaxWidth(),
            habilitado = !estado.guardando
        )
    }
}

@Composable
private fun FormularioEquipo(
    estado: CrearProtocoloUiState,
    viewModel: CrearProtocoloViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Nuevo equipo",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        CampoTexto(
            etiqueta = "TAG del equipo *",
            valor = estado.tagEquipo,
            alCambiar = viewModel::actualizarTagEquipo
        )

        Selector(
            etiqueta = "Tipo de equipo *",
            seleccionado = estado.tipoEquipoSeleccionado?.nombre.orEmpty(),
            opciones = estado.tiposEquipo.map { it.nombre },
            alSeleccionar = { indice ->
                viewModel.seleccionarTipoEquipo(estado.tiposEquipo[indice])
            }
        )

        CampoTexto(
            etiqueta = "Marca (opcional)",
            valor = estado.marcaEquipo,
            alCambiar = viewModel::actualizarMarcaEquipo
        )

        CampoTexto(
            etiqueta = "Modelo (opcional)",
            valor = estado.modeloEquipo,
            alCambiar = viewModel::actualizarModeloEquipo
        )

        CampoTexto(
            etiqueta = "Número de serie (opcional)",
            valor = estado.numeroSerieEquipo,
            alCambiar = viewModel::actualizarNumeroSerieEquipo
        )

        CampoDecimal(
            etiqueta = "Potencia (kVA)",
            valor = estado.potenciaKva,
            alCambiar = viewModel::actualizarPotenciaKva
        )

        CampoDecimal(
            etiqueta = "Tensiones (V)",
            valor = estado.tensiones,
            alCambiar = viewModel::actualizarTensiones
        )

        CampoDecimal(
            etiqueta = "Corriente (A)",
            valor = estado.corriente,
            alCambiar = viewModel::actualizarCorriente
        )

        CampoDecimal(
            etiqueta = "Resistencia máxima de contactos (µΩ)",
            valor = estado.maxContactosUohm,
            alCambiar = viewModel::actualizarMaxContactosUohm
        )

        Text(
            text = "Los campos técnicos son opcionales. Si los completas, utiliza punto decimal.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        BotonPrincipal(
            texto = if (estado.guardando) "Guardando..." else "Guardar equipo",
            onClick = viewModel::crearEquipo,
            modifier = Modifier.fillMaxWidth(),
            habilitado = !estado.guardando
        )
    }
}

@Composable
private fun PasoAmbiental(
    estado: CrearProtocoloUiState,
    viewModel: CrearProtocoloViewModel
) {
    TarjetaSeccion(titulo = "2. Condiciones ambientales") {
        Text(
            text = "La fecha y hora se registrarán automáticamente al guardar.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        CampoDecimal(
            etiqueta = "Temperatura ambiente (°C)",
            valor = estado.temperaturaC,
            alCambiar = viewModel::actualizarTemperatura,
            nota = "Rango permitido: -10 a 60 °C."
        )

        CampoDecimal(
            etiqueta = "Humedad relativa (%)",
            valor = estado.humedadPct,
            alCambiar = viewModel::actualizarHumedad,
            nota = "Rango permitido: 0 a 100 %."
        )

        Text(
            text = "Para decimales utiliza un punto. Ejemplo: 23.5",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PasoMediciones(
    estado: CrearProtocoloUiState,
    viewModel: CrearProtocoloViewModel
) {
    TarjetaSeccion(titulo = "3. Mediciones eléctricas") {
        Text(
            text = estado.pruebaSeleccionada?.nombre
                ?: "Selecciona una prueba en el paso anterior.",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Completa todos los parámetros requeridos para registrar la prueba.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val requiereUnidadAislacion = estado.parametrosPrueba.any {
            it.codigo == "LEC_1MIN" || it.codigo == "LEC_10MIN"
        }

        if (requiereUnidadAislacion) {
            Text(
                text = "Unidad para las lecturas de aislación",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("MΩ", "GΩ").forEach { unidad ->
                    if (estado.unidadAislacion == unidad) {
                        Button(
                            onClick = {
                                viewModel.actualizarUnidadAislacion(unidad)
                            }
                        ) {
                            Text(unidad)
                        }
                    } else {
                        TextButton(
                            onClick = {
                                viewModel.actualizarUnidadAislacion(unidad)
                            }
                        ) {
                            Text(unidad)
                        }
                    }
                }
            }

            Text(
                text = "Las lecturas ingresadas en GΩ se convertirán a MΩ al guardarse.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (estado.parametrosPrueba.isEmpty()) {
            Text(
                text = "No hay parámetros disponibles para esta prueba.",
                color = MaterialTheme.colorScheme.error
            )
        } else {
            estado.parametrosPrueba.forEach { parametro ->
                val unidad = if (
                    requiereUnidadAislacion &&
                    (parametro.codigo == "LEC_1MIN" ||
                            parametro.codigo == "LEC_10MIN")
                ) {
                    estado.unidadAislacion
                } else {
                    parametro.unidad
                }

                CampoDecimal(
                    etiqueta = "${parametro.nombre} ($unidad)",
                    valor = estado.mediciones[parametro.codigo].orEmpty(),
                    alCambiar = { valor ->
                        viewModel.actualizarMedicion(parametro.codigo, valor)
                    }
                )
            }
        }

        Text(
            text = "Las mediciones se guardarán sin clasificarlas. Esa evaluación corresponde al HU-05.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Selector(
    etiqueta: String,
    seleccionado: String,
    opciones: List<String>,
    habilitado: Boolean = true,
    alSeleccionar: (Int) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = {
            if (habilitado && opciones.isNotEmpty()) {
                expandido = !expandido
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = seleccionado,
            onValueChange = {},
            readOnly = true,
            enabled = habilitado,
            label = { Text(etiqueta) },
            placeholder = {
                Text(
                    if (opciones.isEmpty()) "Sin opciones disponibles"
                    else "Seleccionar"
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            opciones.forEachIndexed { indice, opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        alSeleccionar(indice)
                        expandido = false
                    }
                )
            }
        }
    }
}

@Composable
private fun CampoTexto(
    etiqueta: String,
    valor: String,
    alCambiar: (String) -> Unit,
    unaLinea: Boolean = true
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = unaLinea,
        minLines = if (unaLinea) 1 else 3
    )
}

@Composable
private fun CampoDecimal(
    etiqueta: String,
    valor: String,
    alCambiar: (String) -> Unit,
    nota: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = valor,
            onValueChange = alCambiar,
            label = { Text(etiqueta) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            )
        )

        nota?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MensajeEstado(
    mensaje: String,
    esError: Boolean
) {
    Text(
        text = mensaje,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = if (esError) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.primary
        },
        style = MaterialTheme.typography.bodyMedium
    )
}
