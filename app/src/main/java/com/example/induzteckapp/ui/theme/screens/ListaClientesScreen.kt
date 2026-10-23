package com.example.induzteckapp.ui.theme.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.induzteckapp.model.ClienteConsulta
import com.example.induzteckapp.model.EquipoConsulta
import com.example.induzteckapp.viewmodel.ListaClientesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaClientesScreen(
    viewModel: ListaClientesViewModel,
    onProtocoloClick: (Int) -> Unit = {}
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val clientes by viewModel.clientesState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consulta de Clientes y Equipos") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // CA-2.1: Búsqueda por TAG, Cliente o Instalación
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por Cliente, TAG (ej: TR-01) o Instalación...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(clientes, key = { it.idUsuario }) { cliente ->
                    ClienteCardItem(
                        cliente = cliente,
                        onClienteClick = { viewModel.toggleClienteExpansion(cliente.idUsuario) },
                        onEquipoClick = { idEquipo -> viewModel.toggleEquipoExpansion(cliente.idUsuario, idEquipo) },
                        onProtocoloClick = onProtocoloClick
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
    onProtocoloClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClienteClick() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = cliente.nombreRazonSocial, style = MaterialTheme.typography.titleMedium)
                    Text(text = "RUT: ${cliente.rut}", style = MaterialTheme.typography.bodySmall)
                }
                Icon(
                    imageVector = if (cliente.isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            if (cliente.isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                cliente.instalaciones.forEach { instalacion ->
                    Text(
                        text = "Instalación: ${instalacion.nombre}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    instalacion.equipos.forEach { equipo ->
                        EquipoCardItem(
                            equipo = equipo,
                            onEquipoClick = { onEquipoClick(equipo.idEquipo) },
                            onProtocoloClick = onProtocoloClick
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
    onProtocoloClick: (Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEquipoClick() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "TAG: ${equipo.tag} (${equipo.tipoEquipo})", style = MaterialTheme.typography.titleSmall)
                    // CA-2.2: Etiqueta contextual con la ubicación/instalación
                    AssistChip(
                        onClick = { },
                        label = { Text("Ubicación: ${equipo.instalacionNombre}") }
                    )
                }
                Icon(
                    imageVector = if (equipo.isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            // CA-2.3: Historial desplegable de pruebas
            if (equipo.isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Text(
                    text = "Historial de Pruebas:",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                equipo.historialProtocolos.forEach { protocolo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onProtocoloClick(protocolo.idProtocolo) }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Nº: ${protocolo.numeroProtocolo}", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Fecha: ${protocolo.fechaHora} | Técnico: ${protocolo.tecnicoResponsable}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "Estado: ${protocolo.estado}", style = MaterialTheme.typography.bodySmall)
                        }
                        ResultadoBadge(resultado = protocolo.resultadoGlobal)
                    }
                }
            }
        }
    }
}

@Composable
fun ResultadoBadge(resultado: String) {
    val (color, text) = when (resultado) {
        "Aprobado" -> Color(0xFF2E7D32) to "Aprobado"
        "Observado" -> Color(0xFFED6C02) to "Observado"
        else -> Color(0xFFD32F2F) to "Rechazado"
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.extraSmall,
        border = BorderStroke(1.dp, color)
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}