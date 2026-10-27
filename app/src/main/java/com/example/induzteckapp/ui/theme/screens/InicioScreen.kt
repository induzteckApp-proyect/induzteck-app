// com.example.induzteckapp/ui/theme/screens/InicioScreen.kt

package com.example.induzteckapp.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induzteckapp.ui.theme.components.BotonPrincipal
import com.example.induzteckapp.ui.theme.components.TarjetaSeccion
import com.example.induzteckapp.ui.theme.theme.Dimens
import com.example.induzteckapp.ui.theme.theme.MiRegistroTheme
import com.example.induzteckapp.viewmodel.InicioViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioScreen(
    onCerrarSesion: () -> Unit,
    onIrANuevoProtocolo: () -> Unit = {},
    onIrAListaClientes: () -> Unit = {},
    onIrAListaInstalaciones: () -> Unit = {}, // Callback para la vista del cliente
    onIrAPerfil: () -> Unit = {},
    viewModel: InicioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Induzteck",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = uiState.usuario?.nombre.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = uiState.usuario?.rol?.etiqueta.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text("Inicio") },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Mi Perfil") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onIrAPerfil()
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Cerrar sesión") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            viewModel.cerrarSesion()
                            onCerrarSesion()
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Panel Principal") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Text(
                                text = "☰",
                                style = MaterialTheme.typography.headlineSmall
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
            // Botón flotante exclusivo para que el Técnico registre protocolos rápidamente
            floatingActionButton = {
                if (uiState.mostrarBotonCrearProtocolo) {
                    ExtendedFloatingActionButton(
                        onClick = onIrANuevoProtocolo,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Text(text = "+ Nuevo Protocolo")
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                if (uiState.usuario == null) {
                    Text(
                        text = "No hay una sesión activa.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Text(
                        text = uiState.tituloSaludo,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = uiState.descripcionBienvenida,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Secciones unificadas con la paleta de colores y tarjetas de FormularioScreen
                    if (uiState.esTecnico) {
                        TarjetaSeccion(titulo = "Gestión de Terreno") {
                            Text(
                                text = "Inspecciona la cartera de clientes, sus instalaciones asociadas y el historial de protocolos registrados.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            BotonPrincipal(
                                texto = "Ver Clientes",
                                onClick = onIrAListaClientes,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else if (uiState.esCliente) {
                        TarjetaSeccion(titulo = "Tus Instalaciones y Equipos") {
                            Text(
                                text = "Consulta el detalle de los equipos registrados bajo tu razón social y el historial de revisiones eléctricas.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            BotonPrincipal(
                                texto = "Ver mis Instalaciones",
                                onClick = onIrAListaInstalaciones,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------
// VISTA PREVIA (PREVIEW)
// -----------------------------------------------------
@Preview(showBackground = true)
@Composable
private fun InicioScreenPreview() {
    MiRegistroTheme {
        InicioScreen(
            onCerrarSesion = {},
            onIrANuevoProtocolo = {},
            onIrAListaClientes = {},
            onIrAListaInstalaciones = {}
        )
    }
}