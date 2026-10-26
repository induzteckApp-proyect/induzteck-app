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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induzteckapp.R
import com.example.induzteckapp.model.PruebaDetalle
import com.example.induzteckapp.model.ProtocoloDetalle
import com.example.induzteckapp.viewmodel.DetalleRespaldoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleRespaldoDigitalScreen(
    idProtocolo: Int,
    onVolver: () -> Unit,
    viewModel: DetalleRespaldoViewModel = viewModel()
) {
    val protocolo by viewModel.protocoloState.collectAsState()
    val cargando by viewModel.cargandoState.collectAsState()

    LaunchedEffect(idProtocolo) {
        viewModel.cargarDetalle(idProtocolo)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Respaldo Digital") },
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
    ) { innerPadding ->
        when {
            cargando -> LoadingContent(Modifier.padding(innerPadding))
            protocolo == null -> Text(
                text = "No se encontró el protocolo solicitado.",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            )
            else -> ProtocoloContent(
                protocolo = protocolo!!,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(12.dp))
        Text("Cargando respaldo...")
    }
}

@Composable
private fun ProtocoloContent(protocolo: ProtocoloDetalle, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Protocolo Nº ${protocolo.numeroProtocolo}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(protocolo.estadoEtiqueta, color = MaterialTheme.colorScheme.primary)

        DetailCard(title = "Identificación") {
            DetailRow("Fecha y hora", protocolo.fechaHora)
            DetailRow("Cliente", protocolo.clienteNombre)
            DetailRow("Instalación", protocolo.instalacionNombre)
            DetailRow("Equipo", "${protocolo.equipoTag} (${protocolo.tipoEquipo})")
            DetailRow("Técnico responsable", protocolo.tecnicoResponsable)
        }

        DetailCard(title = "Condiciones y resultado") {
            DetailRow("Temperatura", protocolo.temperatura)
            DetailRow("Humedad", protocolo.humedad)
            DetailRow("Resultado global", protocolo.resultadoGlobal)
        }

        DetailCard(title = "Pruebas realizadas") {
            if (protocolo.pruebas.isEmpty()) {
                Text("No hay pruebas asociadas.")
            } else {
                protocolo.pruebas.forEachIndexed { index, prueba ->
                    if (index > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    PruebaRow(prueba)
                }
            }
        }

        if (protocolo.observaciones.isNotBlank()) {
            DetailCard(title = "Observaciones") {
                Text(protocolo.observaciones)
            }
        }
    }
}

@Composable
private fun DetailCard(title: String, content: @Composable () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PruebaRow(prueba: PruebaDetalle) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(prueba.tipoPruebaNombre, fontWeight = FontWeight.SemiBold)
        DetailRow("Elemento", prueba.elemento)
        DetailRow("Momento", prueba.momento)
        DetailRow("Valor medido", "${prueba.valorMedido} ${prueba.unidadMedida}".trim())
        DetailRow("Resultado", prueba.resultado)
    }
}
