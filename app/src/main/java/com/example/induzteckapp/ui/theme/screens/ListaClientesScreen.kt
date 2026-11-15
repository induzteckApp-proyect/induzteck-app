package com.example.induzteckapp.ui.theme.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induzteckapp.R
import com.example.induzteckapp.model.*
import com.example.induzteckapp.ui.theme.theme.ResultadoAprobadoColor
import com.example.induzteckapp.ui.theme.theme.ResultadoObservadoColor
import com.example.induzteckapp.ui.theme.theme.ResultadoRechazadoColor
import com.example.induzteckapp.viewmodel.ListaClientesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaClientesScreen(
    onVolver: () -> Unit = {},
    onProtocoloClick: (Int) -> Unit = {},
    viewModel: ListaClientesViewModel = viewModel()
) {
    val busqueda by viewModel.searchQuery.collectAsStateWithLifecycle()
    val clientes by viewModel.clientesState.collectAsStateWithLifecycle()
    val esTecnico by viewModel.esTecnico.collectAsStateWithLifecycle()
    val guardando by viewModel.guardandoEstado.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensajeEstado.collectAsStateWithLifecycle()

    var protocoloAccion by remember { mutableStateOf<ProtocoloResumen?>(null) }
    var equipoAccion by remember { mutableIntStateOf(-1) }
    var estadoDestino by remember { mutableStateOf("") }
    var motivo by remember { mutableStateOf("") }
    var mostrarMotivoError by remember { mutableStateOf(false) }

    if (protocoloAccion != null) {
        val opciones = accionesDisponiblesCliente(protocoloAccion!!.estado)
        AlertDialog(
            onDismissRequest = { protocoloAccion = null; estadoDestino = ""; motivo = "" },
            title = { Text("Actualizar estado del protocolo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("Protocolo ${protocoloAccion!!.numeroProtocolo}")
                    if (estadoDestino.isBlank()) {
                        Text("Selecciona el nuevo estado:")
                        opciones.forEach { (codigo, etiqueta) ->
                            TextButton(onClick = { estadoDestino = codigo }) { Text(etiqueta) }
                        }
                        if (opciones.isEmpty()) Text("No hay cambios de estado disponibles.")
                    } else {
                        Text("Nuevo estado: ${nombreEstadoCliente(estadoDestino)}")
                        if (estadoDestino == "OBSERVADO" || estadoDestino == "ANULADO") {
                            OutlinedTextField(
                                value = motivo, onValueChange = { motivo = it },
                                label = { Text("Motivo obligatorio") }, minLines = 2,
                                isError = mostrarMotivoError && motivo.isBlank(),
                                supportingText = if (mostrarMotivoError && motivo.isBlank()) {
                                    { Text("Indica el motivo para continuar.") }
                                } else null
                            )
                        } else Text("¿Confirmas el cambio de estado?")
                    }
                }
            },
            confirmButton = {
                if (estadoDestino.isNotBlank()) TextButton(
                    enabled = !guardando,
                    onClick = {
                        mostrarMotivoError = true
                        if ((estadoDestino !in listOf("OBSERVADO", "ANULADO")) || motivo.isNotBlank()) {
                            protocoloAccion?.let {
                                viewModel.cambiarEstadoProtocolo(
                                    it.idProtocolo, equipoAccion, estadoDestino, motivo.trim()
                                )
                            }
                            protocoloAccion = null; estadoDestino = ""; motivo = ""; mostrarMotivoError = false
                        }
                    }
                ) { Text(if (guardando) "Guardando…" else "Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    if (estadoDestino.isNotBlank()) estadoDestino = ""
                    else protocoloAccion = null
                    motivo = ""; mostrarMotivoError = false
                }) { Text(if (estadoDestino.isNotBlank()) "Atrás" else "Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consulta de Clientes y Equipos") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(painterResource(R.drawable.ic_volver), contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = busqueda, onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por Cliente, RUT, TAG o Instalación…") },
                singleLine = true
            )
            mensaje?.let {
                Text(it, color = if (it.contains("correctamente")) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp))
            }
            Spacer(Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(clientes, key = { it.idUsuario }) { cliente ->
                    ClienteCardItem(
                        cliente = cliente,
                        onClienteClick = { viewModel.toggleClienteExpansion(cliente.idUsuario) },
                        onEquipoClick = { viewModel.toggleEquipoExpansion(cliente.idUsuario, it) },
                        onProtocoloClick = onProtocoloClick,
                        esTecnico = esTecnico,
                        onCambiarEstado = { protocolo, idEquipo ->
                            protocoloAccion = protocolo
                            equipoAccion = idEquipo
                            estadoDestino = ""
                            motivo = ""
                            mostrarMotivoError = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ClienteCardItem(
    cliente: ClienteConsulta,
    onClienteClick: () -> Unit,
    onEquipoClick: (Int) -> Unit,
    onProtocoloClick: (Int) -> Unit,
    esTecnico: Boolean = false,
    onCambiarEstado: (ProtocoloResumen, Int) -> Unit = { _, _ -> }
) {
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth().clickable { onClienteClick() },
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(cliente.nombreRazonSocial, style = MaterialTheme.typography.titleMedium)
                    Text("RUT: ${cliente.rut}", style = MaterialTheme.typography.bodySmall)
                }
                Text(if (cliente.isExpanded) "▲" else "▼")
            }
            if (cliente.isExpanded) {
                cliente.instalaciones.forEach { instalacion ->
                    Text("Instalación: ${instalacion.nombre}", style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 6.dp))
                    instalacion.equipos.forEach { equipo ->
                        EquipoCardItem(
                            equipo, { onEquipoClick(equipo.idEquipo) }, onProtocoloClick,
                            esTecnico, { protocolo -> onCambiarEstado(protocolo, equipo.idEquipo) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EquipoCardItem(
    equipo: EquipoConsulta,
    onEquipoClick: () -> Unit,
    onProtocoloClick: (Int) -> Unit,
    esTecnico: Boolean = false,
    onCambiarEstado: (ProtocoloResumen) -> Unit = {}
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth().clickable { onEquipoClick() },
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("TAG: ${equipo.tag} (${equipo.tipoEquipo})", style = MaterialTheme.typography.titleSmall)
                    Text("Ubicación: ${equipo.instalacionNombre}", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(if (equipo.isExpanded) "▲" else "▼")
            }
            if (equipo.isExpanded) {
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Text("Historial de protocolos", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                if (equipo.historialProtocolos.isEmpty()) Text("Sin revisiones registradas.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                equipo.historialProtocolos.forEach { protocolo ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 5.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text("Nº ${protocolo.numeroProtocolo}", fontWeight = FontWeight.SemiBold)
                            Text("Fecha: ${protocolo.fechaHora}")
                            Text("Estado: ${protocolo.estado}", style = MaterialTheme.typography.bodyMedium)
                            Text("Técnico: ${protocolo.tecnicoResponsable}", style = MaterialTheme.typography.bodySmall)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                ResultadoBadge(protocolo.resultadoGlobal)
                                TextButton(onClick = { onProtocoloClick(protocolo.idProtocolo) }) { Text("Ver respaldo") }
                            }
                            if (esTecnico && accionesDisponiblesCliente(protocolo.estado).isNotEmpty()) {
                                OutlinedButton(onClick = { onCambiarEstado(protocolo) }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Cambiar estado")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultadoBadge(resultado: String) {
    val (color, texto) = when (resultado) {
        "Aprobado" -> ResultadoAprobadoColor to "Aprobado"
        "Observado" -> ResultadoObservadoColor to "Observado"
        else -> ResultadoRechazadoColor to "Rechazado"
    }
    Surface(color=color.copy(alpha=0.15f), shape=MaterialTheme.shapes.extraSmall, border=BorderStroke(1.dp,color)) {
        Text(texto,color=color,style=MaterialTheme.typography.labelSmall,modifier=Modifier.padding(horizontal=7.dp,vertical=3.dp))
    }
}

private fun accionesDisponiblesCliente(estadoVisible: String): List<Pair<String,String>> = when (estadoVisible.uppercase()) {
    "BORRADOR" -> listOf("REGISTRADO_TERRENO" to "Marcar como Registrado en terreno","ANULADO" to "Anular protocolo")
    "REGISTRADO EN TERRENO", "REGISTRADO_TERRENO" -> listOf("FORMALIZADO" to "Formalizar","OBSERVADO" to "Marcar como Observado","ANULADO" to "Anular protocolo")
    "OBSERVADO" -> listOf("REGISTRADO_TERRENO" to "Volver a Registrado en terreno","ANULADO" to "Anular protocolo")
    else -> emptyList()
}
private fun nombreEstadoCliente(codigo:String)=when(codigo){
    "REGISTRADO_TERRENO"->"Registrado en terreno";"FORMALIZADO"->"Formalizado";"OBSERVADO"->"Observado";"ANULADO"->"Anulado";else->codigo
}
