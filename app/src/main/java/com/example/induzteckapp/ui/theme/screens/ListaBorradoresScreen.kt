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
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induzteckapp.R
import com.example.induzteckapp.ui.theme.components.TarjetaSeccion
import com.example.induzteckapp.viewmodel.CrearProtocoloViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ListaBorradoresScreen(
    onVolver: () -> Unit,
    onRecuperar: (Long) -> Unit,
    viewModel: CrearProtocoloViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.cargarBorradores() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Borradores de protocolos") },
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Los borradores conservan las condiciones ambientales y las pruebas que ya se hayan guardado. Al recuperarlos, puedes continuar con las pruebas pendientes.",
                style = MaterialTheme.typography.bodyMedium
            )

            estado.mensajeError?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            if (estado.borradores.isEmpty()) {
                Text("No hay borradores pendientes para esta sesión.")
            } else {
                estado.borradores.forEach { borrador ->
                    TarjetaSeccion(titulo = "${borrador.numero} — ${borrador.tagEquipo}") {
                        Text(borrador.tipoEquipo, fontWeight = FontWeight.SemiBold)
                        Text("Fecha: ${borrador.fechaHora}")
                        Text("Temperatura: ${borrador.temperaturaC} °C")
                        Text("Humedad: ${borrador.humedadPct} %")
                        Spacer(Modifier.height(6.dp))
                        Button(
                            onClick = { onRecuperar(borrador.idProtocolo) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Recuperar borrador")
                        }
                    }
                }
            }
        }
    }
}
