package com.example.induzteckapp.ui.theme.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induzteckapp.R
import com.example.induzteckapp.model.ProtocoloDetalle
import com.example.induzteckapp.model.PruebaDetalle
import com.example.induzteckapp.ui.theme.theme.MiRegistroTheme
import com.example.induzteckapp.viewmodel.DetalleRespaldoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleRespaldoDigitalScreen(
    idProtocolo: Int,
    onVolver: () -> Unit = {},
    viewModel: DetalleRespaldoViewModel = viewModel()
) {
    val protocolo by viewModel.protocoloState.collectAsState()
    val cargando by viewModel.cargandoState.collectAsState()
    val error by viewModel.errorState.collectAsState()

    LaunchedEffect(idProtocolo) {
        viewModel.cargarDetalle(idProtocolo)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Respaldo Digital Oficial") },
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
    ) { innerPadding ->
        if (cargando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = error!!,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        } else if (protocolo == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No se encontró la información del protocolo de respaldo.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            val p = protocolo!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tarjeta 1: Encabezado y Estado de Formalización (CA-3.2 / RN-17)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Protocolo N° ${p.numeroProtocolo}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Fecha de prueba: ${p.fechaHora}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                EstadoFormalizacionBadge(estado = p.estadoEtiqueta)
                                ResultadoClienteBadge(resultado = p.resultadoGlobal)
                            }
                        }
                    }
                }

                // Tarjeta 2: Datos Generales de la Inspección (Solo Lectura - CA-3.3)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Información General",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider()
                        ItemDetalleRow(label = "Empresa / Cliente", value = p.clienteNombre)
                        ItemDetalleRow(label = "Instalación", value = p.instalacionNombre)
                        ItemDetalleRow(label = "Equipo / TAG", value = "${p.equipoTag} (${p.tipoEquipo})")
                        ItemDetalleRow(label = "Técnico Responsable", value = p.tecnicoResponsable)
                        ItemDetalleRow(label = "Temperatura Ambiente", value = p.temperatura)
                        ItemDetalleRow(label = "Humedad Relativa", value = p.humedad)
                    }
                }

                // Tarjeta 3: Pruebas Eléctricas Registradas
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Pruebas Eléctricas y Aislamiento",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider()

                        if (p.pruebas.isEmpty()) {
                            Text(
                                text = "Sin pruebas registradas.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            p.pruebas.forEach { prueba ->
                                ItemPruebaRow(prueba = prueba)
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            }
                        }
                    }
                }

                // Tarjeta 4: Observaciones Generales
                if (p.observaciones.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Observaciones de Terreno",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = p.observaciones,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Sello de Respaldo Digital Oficial
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🔒 Respaldo Digital Oficial Induztek",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Documento de solo lectura generado conforme al estándar auditable.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemDetalleRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ItemPruebaRow(prueba: PruebaDetalle) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = prueba.tipoPruebaNombre,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Elemento: ${prueba.elemento} (${prueba.momento})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Lectura: ${prueba.valorMedido} ${prueba.unidadMedida}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
        ResultadoClienteBadge(resultado = prueba.resultado)
    }
}

@Preview(showBackground = true)
@Composable
private fun DetalleRespaldoDigitalScreenPreview() {
    MiRegistroTheme {
        Surface {
            val protocoloMock = ProtocoloDetalle(
                idProtocolo = 301,
                numeroProtocolo = "PT-2026-0401",
                fechaHora = "26-09-2026 14:30",
                estadoCodigo = "FORMALIZADO",
                estadoEtiqueta = "Formalizado",
                resultadoGlobal = "Aprobado",
                tecnicoResponsable = "Matías Godoy",
                clienteNombre = "Envasadora del Pacífico S.A.",
                instalacionNombre = "Planta Lampa",
                equipoTag = "TR-01",
                tipoEquipo = "Transformador Triásico",
                temperatura = "22 °C",
                humedad = "40 %",
                observaciones = "Sin hallazgos críticos. Aislamiento dentro de norma.",
                pruebas = listOf(
                    PruebaDetalle(
                        idPrueba = 1,
                        tipoPruebaNombre = "Resistencia de Aislamiento",
                        elemento = "Devanado Primario - Secundario",
                        momento = "Post-limpieza",
                        valorMedido = "550",
                        unidadMedida = "MΩ",
                        resultado = "Aprobado"
                    )
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Protocolo N° ${protocoloMock.numeroProtocolo}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = "Cliente: ${protocoloMock.clienteNombre}")
                        Text(text = "Equipo: ${protocoloMock.equipoTag}")
                    }
                }
            }
        }
    }
}
