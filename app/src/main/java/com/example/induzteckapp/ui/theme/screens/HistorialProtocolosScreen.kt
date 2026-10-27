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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induzteckapp.R
import com.example.induzteckapp.model.ProtocoloHistorial
import com.example.induzteckapp.model.PruebaHistorial
import com.example.induzteckapp.ui.theme.components.BotonPrincipal
import com.example.induzteckapp.ui.theme.components.BotonSecundario
import com.example.induzteckapp.ui.theme.components.TarjetaSeccion
import com.example.induzteckapp.viewmodel.HistorialProtocolosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialProtocolosScreen(
    onVolver: () -> Unit,
    viewModel: HistorialProtocolosViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()
    var expandido by remember { mutableStateOf(setOf<Long>()) }
    var protocoloAccion by remember { mutableStateOf<ProtocoloHistorial?>(null) }
    var estadoDestino by remember { mutableStateOf("") }
    var motivo by remember { mutableStateOf("") }
    var intentoConfirmarMotivo by remember { mutableStateOf(false) }

    if (protocoloAccion != null) {
        val necesitaMotivo = estadoDestino == "OBSERVADO" || estadoDestino == "ANULADO"
        AlertDialog(
            onDismissRequest = {
                protocoloAccion = null
                motivo = ""
                intentoConfirmarMotivo = false
            },
            title = {
                Text(if (estadoDestino == "ANULADO") "Anular protocolo" else "Cambiar estado")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Protocolo ${protocoloAccion?.numero}: ${nombreEstado(estadoDestino)}")
                    if (necesitaMotivo) {
                        OutlinedTextField(
                            value = motivo,
                            onValueChange = { motivo = it },
                            label = { Text("Motivo obligatorio") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            isError = intentoConfirmarMotivo && motivo.isBlank(),
                            supportingText = if (intentoConfirmarMotivo && motivo.isBlank()) {
                                { Text("Indica el motivo para continuar.") }
                            } else null
                        )
                    } else {
                        Text("¿Confirmas el cambio de estado?")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        intentoConfirmarMotivo = true
                        if (!necesitaMotivo || motivo.isNotBlank()) {
                            protocoloAccion?.let {
                                viewModel.cambiarEstado(it.idProtocolo, estadoDestino, motivo)
                            }
                            protocoloAccion = null
                            motivo = ""
                            intentoConfirmarMotivo = false
                        }
                    },
                    enabled = !estado.guardando
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        protocoloAccion = null
                        motivo = ""
                        intentoConfirmarMotivo = false
                    }
                ) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = "Volver"
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TarjetaSeccion(titulo = "Protocolos registrados") {
                Text(
                    "Consulta las mediciones guardadas, revisa los resultados de cada prueba y administra los estados del protocolo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BotonSecundario(
                    texto = if (estado.cargando) "Actualizando…" else "Actualizar historial",
                    onClick = viewModel::cargarHistorial,
                    modifier = Modifier.fillMaxWidth(),
                    habilitado = !estado.cargando
                )
            }

            estado.mensajeError?.let {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            estado.mensajeExito?.let {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (estado.cargando) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator()
                }
            }

            if (!estado.cargando && estado.protocolos.isEmpty()) {
                TarjetaSeccion(titulo = "Aún no hay protocolos") {
                    Text(
                        "Cuando registres un protocolo en terreno, aparecerá aquí con sus pruebas y mediciones.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            estado.protocolos.forEach { protocolo ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        Text(
                            protocolo.numero,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        EtiquetaEstado(protocolo.estadoCodigo, protocolo.estadoNombre)
                        Text("Cliente: ${protocolo.cliente}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Equipo: ${protocolo.tagEquipo} — ${protocolo.tipoEquipo}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Fecha: ${protocolo.fechaHora}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Resultado global",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            EtiquetaResultado(protocolo.resultadoGlobal?.let(::nombreResultado) ?: "Sin resultado")
                        }

                        BotonSecundario(
                            texto = if (protocolo.idProtocolo in expandido) {
                                "Ocultar detalles"
                            } else {
                                "Ver detalles y mediciones"
                            },
                            onClick = {
                                expandido = if (protocolo.idProtocolo in expandido) {
                                    expandido - protocolo.idProtocolo
                                } else {
                                    expandido + protocolo.idProtocolo
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (protocolo.idProtocolo in expandido) {
                            TarjetaSeccion(titulo = "Datos del protocolo") {
                                Text("Instalación: ${protocolo.instalacion}")
                                if (protocolo.ubicacion.isNotBlank()) {
                                    Text("Ubicación: ${protocolo.ubicacion}")
                                }
                                Text(
                                    "Marca/modelo: ${protocolo.marca.ifBlank { "No indicada" }} / ${protocolo.modelo.ifBlank { "No indicado" }}"
                                )
                                Text("Técnico/ingeniero: ${protocolo.tecnico}")
                                Text("Condiciones: ${protocolo.temperaturaC} °C · ${protocolo.humedadPct} % HR")
                                if (protocolo.observaciones.isNotBlank()) {
                                    Text("Observaciones: ${protocolo.observaciones}")
                                }
                            }

                            TarjetaSeccion(titulo = "Pruebas y mediciones") {
                                if (protocolo.pruebas.isEmpty()) {
                                    Text("No hay pruebas asociadas.")
                                } else {
                                    protocolo.pruebas.forEach { prueba ->
                                        DetallePruebaHistorial(prueba)
                                    }
                                }
                            }

                            TarjetaSeccion(titulo = "Acciones disponibles") {
                                val acciones = accionesDisponibles(protocolo.estadoCodigo)
                                if (acciones.isEmpty()) {
                                    Text(
                                        "Este protocolo no tiene más cambios de estado disponibles.",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    acciones.forEachIndexed { indice, (codigo, etiqueta) ->
                                        if (indice == 0) {
                                            BotonPrincipal(
                                                texto = etiqueta,
                                                onClick = {
                                                    estadoDestino = codigo
                                                    protocoloAccion = protocolo
                                                    motivo = ""
                                                    intentoConfirmarMotivo = false
                                                },
                                                habilitado = !estado.guardando,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        } else {
                                            BotonSecundario(
                                                texto = etiqueta,
                                                onClick = {
                                                    estadoDestino = codigo
                                                    protocoloAccion = protocolo
                                                    motivo = ""
                                                    intentoConfirmarMotivo = false
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                habilitado = !estado.guardando
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun EtiquetaEstado(codigo: String, texto: String) {
    val (fondo, textoColor) = when (codigo) {
        "REGISTRADO_TERRENO" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        "FORMALIZADO" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        "OBSERVADO" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        "ANULADO" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(color = fondo, contentColor = textoColor, shape = RoundedCornerShape(50)) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EtiquetaResultado(texto: String) {
    val (fondo, textoColor) = when (texto) {
        "Aprobado" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        "Observado" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        "Rechazado" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(color = fondo, contentColor = textoColor, shape = RoundedCornerShape(50)) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DetallePruebaHistorial(prueba: PruebaHistorial) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(prueba.nombre, fontWeight = FontWeight.SemiBold)
            if (prueba.elemento.isNotBlank()) Text("Elemento: ${prueba.elemento}")
            Text("Estado de la prueba: ${if (prueba.realizada) "Realizada" else "Pendiente"}")
            if (prueba.realizada) {
                Text("Resultado: ${prueba.idResultado?.let(::nombreResultado) ?: "Sin resultado"}")
            }
            if (prueba.mediciones.isEmpty()) Text("Sin mediciones guardadas.")
            prueba.mediciones.forEach { Text("• ${it.nombre}: ${it.valor} ${it.unidad}") }
        }
    }
}

private fun accionesDisponibles(estado: String): List<Pair<String, String>> = when (estado) {
    "BORRADOR" -> listOf("REGISTRADO_TERRENO" to "Marcar como Registrado en terreno", "ANULADO" to "Anular protocolo")
    "REGISTRADO_TERRENO" -> listOf("FORMALIZADO" to "Formalizar", "OBSERVADO" to "Marcar como Observado", "ANULADO" to "Anular protocolo")
    "OBSERVADO" -> listOf("REGISTRADO_TERRENO" to "Volver a Registrado en terreno", "ANULADO" to "Anular protocolo")
    "ANULADO" -> emptyList()
    else -> emptyList()
}

private fun nombreEstado(codigo: String): String = when (codigo) {
    "BORRADOR" -> "Borrador"
    "REGISTRADO_TERRENO" -> "Registrado en terreno"
    "FORMALIZADO" -> "Formalizado"
    "OBSERVADO" -> "Observado"
    "ANULADO" -> "Anulado"
    else -> codigo
}

private fun nombreResultado(id: Int): String = when (id) {
    1 -> "Aprobado"
    2 -> "Observado"
    3 -> "Rechazado"
    else -> "Sin resultado"
}
