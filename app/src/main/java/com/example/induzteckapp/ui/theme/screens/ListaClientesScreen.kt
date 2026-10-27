package com.example.induzteckapp.ui.theme.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.induzteckapp.ui.theme.theme.ResultadoAprobadoColor
import com.example.induzteckapp.ui.theme.theme.ResultadoObservadoColor
import com.example.induzteckapp.ui.theme.theme.ResultadoRechazadoColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induzteckapp.R
import com.example.induzteckapp.model.ClienteConsulta
import com.example.induzteckapp.model.EquipoConsulta
import com.example.induzteckapp.model.InstalacionConsulta
import com.example.induzteckapp.model.ProtocoloResumen
import com.example.induzteckapp.viewmodel.ListaClientesViewModel

import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaClientesScreen(
    onVolver: () -> Unit = {},
    onProtocoloClick: (Int) -> Unit = {},
    onEditarProtocolo: (Int) -> Unit = {},
    viewModel: ListaClientesViewModel = viewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val clientes by viewModel.clientesState.collectAsState()
    val esTecnico by viewModel.esTecnico.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consulta de Clientes y Equipos") },
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // CA-2.1: Barra de búsqueda en tiempo real para TAG, Cliente o Instalación
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por Cliente, TAG (ej: TR-01) o Instalación...") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lista desplegable de clientes
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(clientes, key = { it.idUsuario }) { cliente ->
                    ClienteCardItem(
                        cliente = cliente,
                        onClienteClick = { viewModel.toggleClienteExpansion(cliente.idUsuario) },
                        onEquipoClick = { idEquipo ->
                            viewModel.toggleEquipoExpansion(cliente.idUsuario, idEquipo)
                        },
                        onProtocoloClick = onProtocoloClick,
                        onEditarProtocolo = onEditarProtocolo,
                        esTecnico = esTecnico
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
    onEditarProtocolo: (Int) -> Unit = {},
    esTecnico: Boolean = false
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
                    Text(
                        text = cliente.nombreRazonSocial,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "RUT: ${cliente.rut}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = if (cliente.isExpanded) "▲" else "▼",
                    style = MaterialTheme.typography.bodyMedium
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
                            onProtocoloClick = onProtocoloClick,
                            onEditarProtocolo = onEditarProtocolo,
                            esTecnico = esTecnico
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
    onEditarProtocolo: (Int) -> Unit = {},
    esTecnico: Boolean = false
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
                    Text(
                        text = "TAG: ${equipo.tag} (${equipo.tipoEquipo})",
                        style = MaterialTheme.typography.titleSmall
                    )
                    // CA-2.2: Etiqueta contextual con la ubicación/instalación
                    AssistChip(
                        onClick = { },
                        label = { Text("Ubicación: ${equipo.instalacionNombre}") }
                    )
                }
                Text(
                    text = if (equipo.isExpanded) "▲" else "▼",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // CA-2.3: Historial desplegable de pruebas del equipo
            if (equipo.isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Text(
                    text = "Historial de Protocolos:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                if (equipo.historialProtocolos.isEmpty()) {
                    Text(
                        text = "Sin revisiones registradas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                equipo.historialProtocolos.forEach { protocolo ->
                    val esModificable = esTecnico &&
                        protocolo.estado != "Formalizado" &&
                        protocolo.estado != "FORMALIZADO" &&
                        protocolo.estado != "Anulado" &&
                        protocolo.estado != "ANULADO"

                    ProtocoloResumenItem(
                        protocolo = protocolo,
                        onClick = { onProtocoloClick(protocolo.idProtocolo) },
                        onEditar = if (esModificable) {
                            { onEditarProtocolo(protocolo.idProtocolo) }
                        } else null
                    )
                }
            }
        }
    }
}

@Composable
fun ResultadoBadge(resultado: String) {
    val (color, text) = when (resultado) {
        "Aprobado" -> ResultadoAprobadoColor to "Aprobado"
        "Observado" -> ResultadoObservadoColor to "Observado"
        else -> ResultadoRechazadoColor to "Rechazado"
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





// 1. Preview de la pantalla completa en modo de vista previa
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ListaClientesScreenPreview() {
    MaterialTheme {
        Surface {
            // Se renderiza con una lista de datos simulados
            val clienteEjemplo = ClienteConsulta(
                idUsuario = 1,
                nombreRazonSocial = "Envasadora del Pacífico S.A.",
                rut = "76.482.915-8",
                isExpanded = true,
                instalaciones = listOf(
                    InstalacionConsulta(
                        idInstalacion = 101,
                        nombre = "Planta Lampa",
                        equipos = listOf(
                            EquipoConsulta(
                                idEquipo = 201,
                                tag = "TR-01",
                                tipoEquipo = "Transformador",
                                instalacionNombre = "Planta Lampa",
                                isExpanded = true,
                                historialProtocolos = listOf(
                                    ProtocoloResumen(
                                        idProtocolo = 301,
                                        numeroProtocolo = "PT-2026-0401",
                                        fechaHora = "26-09-2026",
                                        estado = "Registrado en terreno",
                                        resultadoGlobal = "Observado",
                                        tecnicoResponsable = "Matías Godoy"
                                    ),
                                    ProtocoloResumen(
                                        idProtocolo = 302,
                                        numeroProtocolo = "PT-2025-0288",
                                        fechaHora = "13-09-2025",
                                        estado = "Formalizado",
                                        resultadoGlobal = "Aprobado",
                                        tecnicoResponsable = "Javiera Tapia"
                                    )
                                )
                            )
                        )
                    )
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar por Cliente, TAG (ej: TR-01) o Instalación...") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                ClienteCardItem(
                    cliente = clienteEjemplo,
                    onClienteClick = {},
                    onEquipoClick = {},
                    onProtocoloClick = {}
                )
            }
        }
    }
}

// 2. Preview específico para verificar los tres tipos de badges de resultado (CA-2.3, RN-10)
@Preview(showBackground = true)
@Composable
fun ResultadoBadgesPreview() {
    MaterialTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ResultadoBadge(resultado = "Aprobado")
            ResultadoBadge(resultado = "Observado")
            ResultadoBadge(resultado = "Rechazado")
        }
    }
}