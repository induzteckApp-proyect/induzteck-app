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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.induzteckapp.ui.theme.theme.ResultadoAprobadoColor
import com.example.induzteckapp.ui.theme.theme.ResultadoObservadoColor
import com.example.induzteckapp.ui.theme.theme.ResultadoRechazadoColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induzteckapp.R
import com.example.induzteckapp.model.EquipoConsulta
import com.example.induzteckapp.model.InstalacionConsulta
import com.example.induzteckapp.model.ProtocoloResumen
import com.example.induzteckapp.ui.theme.theme.MiRegistroTheme
import com.example.induzteckapp.viewmodel.ListaInstalacionesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaInstalacionesScreen(
    onVolver: () -> Unit = {},
    onProtocoloClick: (Int) -> Unit = {},
    viewModel: ListaInstalacionesViewModel = viewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val instalaciones by viewModel.instalacionesState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Instalaciones y Equipos") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = "Volver al Panel"
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
                .padding(16.dp)
        ) {
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por Instalación o TAG (ej: TR-01)...") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (instalaciones.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🏢 No se encontraron instalaciones ni equipos registrados.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(instalaciones, key = { it.idInstalacion }) { instalacion ->
                        InstalacionCardItem(
                            instalacion = instalacion,
                            onInstalacionClick = { viewModel.toggleInstalacionExpansion(instalacion.idInstalacion) },
                            onEquipoClick = { idEquipo ->
                                viewModel.toggleEquipoExpansion(instalacion.idInstalacion, idEquipo)
                            },
                            onProtocoloClick = onProtocoloClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InstalacionCardItem(
    instalacion: InstalacionConsulta,
    onInstalacionClick: () -> Unit,
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
                    .clickable { onInstalacionClick() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = instalacion.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${instalacion.equipos.size} equipo(s) asociado(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = if (instalacion.isExpanded) "▲" else "▼",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (instalacion.isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                if (instalacion.equipos.isEmpty()) {
                    Text(
                        text = "No hay equipos registrados en esta instalación.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    instalacion.equipos.forEach { equipo ->
                        EquipoClienteCardItem(
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
fun EquipoClienteCardItem(
    equipo: EquipoConsulta,
    onEquipoClick: () -> Unit,
    onProtocoloClick: (Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 4.dp, bottom = 4.dp)
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
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
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
                } else {
                    equipo.historialProtocolos.forEach { protocolo ->
                        ProtocoloResumenItem(
                            protocolo = protocolo,
                            onClick = { onProtocoloClick(protocolo.idProtocolo) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProtocoloResumenItem(
    protocolo: ProtocoloResumen,
    onClick: () -> Unit,
    onEditar: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Nº Protocolo: ${protocolo.numeroProtocolo}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Fecha: ${protocolo.fechaHora} | Técnico: ${protocolo.tecnicoResponsable}",
                style = MaterialTheme.typography.bodySmall
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            EstadoFormalizacionBadge(estado = protocolo.estado)
            ResultadoClienteBadge(resultado = protocolo.resultadoGlobal)
            onEditar?.let { TextButton(onClick = it) { Text("Editar") } }
        }
    }
}

@Composable
fun EstadoFormalizacionBadge(estado: String) {
    val (color, text) = if (estado == "Formalizado") {
        ResultadoAprobadoColor to "Formalizado"
    } else {
        ResultadoObservadoColor to "Pendiente de formalización"
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

@Composable
fun ResultadoClienteBadge(resultado: String) {
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




@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ListaInstalacionesScreenPreview() {
    MiRegistroTheme {
        Surface {
            val instalacionEjemplo = InstalacionConsulta(
                idInstalacion = 101,
                nombre = "Planta Central Lampa",
                isExpanded = true,
                equipos = listOf(
                    EquipoConsulta(
                        idEquipo = 201,
                        tag = "TR-01",
                        tipoEquipo = "Transformador Triásico",
                        instalacionNombre = "Planta Central Lampa",
                        isExpanded = true,
                        historialProtocolos = listOf(
                            ProtocoloResumen(
                                idProtocolo = 301,
                                numeroProtocolo = "PT-2026-0401",
                                fechaHora = "26-09-2026",
                                estado = "Formalizado",
                                resultadoGlobal = "Aprobado",
                                tecnicoResponsable = "Matías Godoy"
                            ),
                            ProtocoloResumen(
                                idProtocolo = 302,
                                numeroProtocolo = "PT-2025-0288",
                                fechaHora = "13-09-2025",
                                estado = "Formalizado",
                                resultadoGlobal = "Observado",
                                tecnicoResponsable = "Javiera Tapia"
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
                    placeholder = { Text("Buscar por Instalación o TAG (ej: TR-01)...") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                InstalacionCardItem(
                    instalacion = instalacionEjemplo,
                    onInstalacionClick = {},
                    onEquipoClick = {},
                    onProtocoloClick = {}
                )
            }
        }
    }
}
