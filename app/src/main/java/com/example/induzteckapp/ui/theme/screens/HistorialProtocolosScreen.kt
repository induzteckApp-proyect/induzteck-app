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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induzteckapp.model.ProtocoloHistorial
import com.example.induzteckapp.model.PruebaHistorial
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

    if (protocoloAccion != null) {
        val necesitaMotivo = estadoDestino == "OBSERVADO" || estadoDestino == "ANULADO"
        AlertDialog(
            onDismissRequest = { protocoloAccion = null; motivo = "" },
            title = { Text(if (estadoDestino == "ANULADO") "Anular protocolo" else "Cambiar estado") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Protocolo ${protocoloAccion?.numero}: ${nombreEstado(estadoDestino)}")
                    if (necesitaMotivo) {
                        OutlinedTextField(
                            value = motivo,
                            onValueChange = { motivo = it },
                            label = { Text("Motivo obligatorio") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            isError = motivo.isBlank()
                        )
                    } else {
                        Text("¿Confirmas el cambio de estado?")
                    }
                }
            },
            confirmButton = {
                TextButton(enabled = !estado.guardando && (!necesitaMotivo || motivo.isNotBlank()), onClick = {
                    protocoloAccion?.let { viewModel.cambiarEstado(it.idProtocolo, estadoDestino, motivo) }
                    protocoloAccion = null
                    motivo = ""
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { protocoloAccion = null; motivo = "" }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Protocolos") },
                navigationIcon = { TextButton(onClick = onVolver) { Text("‹ Volver") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = { TextButton(onClick = viewModel::cargarHistorial) { Text("Actualizar") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Todos los protocolos registrados en el dispositivo, con su cliente, equipo, pruebas y mediciones.", style = MaterialTheme.typography.bodyMedium)
            estado.mensajeError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            estado.mensajeExito?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            if (estado.cargando) CircularProgressIndicator()
            if (!estado.cargando && estado.protocolos.isEmpty()) Text("Todavía no hay protocolos guardados.")
            estado.protocolos.forEach { protocolo ->
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text("${protocolo.numero} · ${protocolo.estadoNombre}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Cliente: ${protocolo.cliente}")
                        Text("Equipo: ${protocolo.tagEquipo} — ${protocolo.tipoEquipo}")
                        Text("Fecha: ${protocolo.fechaHora}")
                        Text("Resultado global: ${protocolo.resultadoGlobal?.let(::nombreResultado) ?: "Sin resultado"}")
                        TextButton(onClick = {
                            expandido = if (protocolo.idProtocolo in expandido) expandido - protocolo.idProtocolo else expandido + protocolo.idProtocolo
                        }) { Text(if (protocolo.idProtocolo in expandido) "Ocultar detalles ▲" else "Ver detalles ▼") }
                        if (protocolo.idProtocolo in expandido) {
                            Spacer(Modifier.height(3.dp))
                            Text("Cliente e instalación", fontWeight = FontWeight.Bold)
                            Text("Instalación: ${protocolo.instalacion}")
                            if (protocolo.ubicacion.isNotBlank()) Text("Ubicación: ${protocolo.ubicacion}")
                            Text("Marca/modelo: ${(protocolo.marca.ifBlank { "No indicada" })} / ${(protocolo.modelo.ifBlank { "No indicado" })}")
                            Text("Técnico/ingeniero: ${protocolo.tecnico}")
                            Text("Condiciones: ${protocolo.temperaturaC} °C · ${protocolo.humedadPct} % HR")
                            if (protocolo.observaciones.isNotBlank()) Text("Motivo/observaciones: ${protocolo.observaciones}")
                            Text("Pruebas", fontWeight = FontWeight.Bold)
                            if (protocolo.pruebas.isEmpty()) Text("No hay pruebas asociadas.")
                            protocolo.pruebas.forEach { prueba -> DetallePruebaHistorial(prueba) }
                            Text("Cambiar estado", fontWeight = FontWeight.Bold)
                            accionesDisponibles(protocolo.estadoCodigo).forEach { (codigo, etiqueta) ->
                                Button(
                                    onClick = {
                                        estadoDestino = codigo
                                        protocoloAccion = protocolo
                                        motivo = ""
                                    },
                                    enabled = !estado.guardando,
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text(etiqueta) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetallePruebaHistorial(prueba: PruebaHistorial) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(prueba.nombre, fontWeight = FontWeight.SemiBold)
            if (prueba.elemento.isNotBlank()) Text("Elemento: ${prueba.elemento}")
            Text("Estado de la prueba: ${if (prueba.realizada) "Realizada" else "Pendiente"}")
            if (prueba.realizada) Text("Resultado: ${prueba.idResultado?.let(::nombreResultado) ?: "Sin resultado"}")
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
