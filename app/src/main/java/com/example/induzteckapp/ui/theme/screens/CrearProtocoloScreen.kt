// com.example.induzteckapp/ui/theme/screens/CrearProtocoloScreen.kt

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.induzteckapp.R
import com.example.induzteckapp.ui.theme.components.BotonPrincipal
import com.example.induzteckapp.ui.theme.components.BotonSecundario
import com.example.induzteckapp.ui.theme.components.TarjetaSeccion
import com.example.induzteckapp.ui.theme.theme.MiRegistroTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearProtocoloScreen(
    onVolver: () -> Unit
) {
    var pasoActual by rememberSaveable { mutableIntStateOf(1) }

    val titulosPasos = listOf(
        "Contexto de terreno",
        "Condiciones ambientales",
        "Mediciones"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Nuevo protocolo")
                },
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

                Spacer(modifier = Modifier.height(8.dp))

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
                        val numeroPaso = indice + 1
                        val actual = pasoActual == numeroPaso
                        val completado = pasoActual > numeroPaso

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = when {
                                    completado -> "✓"
                                    else -> numeroPaso.toString()
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (actual || completado) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier
                                    .padding(4.dp)
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
                                fontWeight = if (actual) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            when (pasoActual) {
                1 -> {
                    TarjetaSeccion(titulo = "1. Contexto de terreno") {
                        Text(
                            text = "Identifica el cliente y selecciona la instalación y el equipo que se someterá a prueba.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        HorizontalDivider()

                        Text(
                            text = "Cliente",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Aquí se cargará el selector de clientes registrados.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Instalación",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Podrás seleccionar una instalación existente o registrar una nueva.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Equipo",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Podrás seleccionar un equipo existente o crear uno para la instalación elegida.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                2 -> {
                    TarjetaSeccion(titulo = "2. Condiciones ambientales") {
                        Text(
                            text = "Se registrarán automáticamente la fecha y la hora del protocolo.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Temperatura ambiente (°C)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Rango permitido: -10,0 a 60,0 °C.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Humedad relativa (%)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Rango permitido: 0,0 a 100,0 %.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                3 -> {
                    TarjetaSeccion(titulo = "3. Mediciones eléctricas") {
                        Text(
                            text = "Los campos se adaptarán al tipo de equipo y a la prueba seleccionada.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Aquí se incorporarán las mediciones, sus unidades y las validaciones correspondientes antes de registrar el protocolo.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pasoActual == 1) {
                    BotonSecundario(
                        texto = "Cancelar",
                        onClick = onVolver,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    BotonSecundario(
                        texto = "Anterior",
                        onClick = { pasoActual-- },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (pasoActual < 3) {
                    BotonPrincipal(
                        texto = "Continuar",
                        onClick = { pasoActual++ },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    BotonPrincipal(
                        texto = "Confirmar protocolo",
                        onClick = {
                            // Se conectará al guardado real cuando integremos
                            // el ViewModel y la persistencia de SQLite.
                        },
                        modifier = Modifier.weight(1f),
                        habilitado = false
                    )
                }
            }

            Text(
                text = "Los datos del protocolo se validarán antes de guardarse localmente.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CrearProtocoloScreenPreview() {
    MiRegistroTheme {
        CrearProtocoloScreen(onVolver = {})
    }
}


/*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.induzteckapp.R
import com.example.induzteckapp.ui.theme.theme.MiRegistroTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearProtocoloScreen(
    onVolver: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Protocolo On-The-Fly") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = "Volver al Inicio"
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "⚡ Registro de Protocolos en Terreno (HU-04)",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "¡Acceso confirmado como Técnico / Ingeniero!\n\nDesde esta vista se integrará la geolocalización GPS nativa para la ubicación de instalaciones y el selector de evidencias/fotos.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onVolver,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Volver al Dashboard")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CrearProtocoloScreenPreview() {
    MiRegistroTheme {
        CrearProtocoloScreen(onVolver = {})
    }
}

 */