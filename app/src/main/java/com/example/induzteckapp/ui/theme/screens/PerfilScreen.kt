package com.example.induzteckapp.ui.theme.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induzteckapp.R
import com.example.induzteckapp.ui.theme.components.AvatarCircular
import com.example.induzteckapp.ui.theme.components.BotonPrincipal
import com.example.induzteckapp.ui.theme.components.BotonSecundario
import com.example.induzteckapp.ui.theme.theme.MiRegistroTheme
import com.example.induzteckapp.viewmodel.PerfilViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    onVolver: () -> Unit = {},
    viewModel: PerfilViewModel = viewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val usuario by viewModel.usuarioState.collectAsState()
    val nombre by viewModel.nombre.collectAsState()
    val apellido by viewModel.apellido.collectAsState()
    val giro by viewModel.giro.collectAsState()
    val contactoNombre by viewModel.contactoNombre.collectAsState()
    val telefono by viewModel.telefono.collectAsState()
    val avatarBytes by viewModel.avatarBytes.collectAsState()
    val mensaje by viewModel.mensajeState.collectAsState()

    // Selector nativo de galería de Android (PickVisualMedia / Photo Picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        // CA-8.2: Si selecciona una imagen válida, actualiza avatar BLOB de inmediato.
        // CA-8.3: Si cancela (uri == null), mantiene la imagen previa sin errores.
        if (uri != null) {
            viewModel.onAvatarSeleccionado(context, uri)
        }
    }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Encabezado con Avatar Circular BLOB (CA-8.2) y Botón para cambiar foto
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AvatarCircular(
                        avatarBytes = avatarBytes,
                        descripcion = "Foto de perfil de ${usuario?.nombre ?: "usuario"}"
                    )

                    BotonSecundario(
                        texto = "Cambiar Avatar",
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                }
            }

            // Datos de Solo Lectura (RUT, Correo, Rol) - CA-8.1
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Información de Cuenta (Solo Lectura)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    HorizontalDivider()

                    OutlinedTextField(
                        value = usuario?.rut ?: "",
                        onValueChange = {},
                        label = { Text("RUT") },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = usuario?.correo ?: "",
                        onValueChange = {},
                        label = { Text("Correo Electrónico") },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = usuario?.rol?.etiqueta ?: "",
                        onValueChange = {},
                        label = { Text("Rol Asignado") },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Datos Editables (Nombre, Apellidos, Giro, Contacto, Teléfono)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Información Personal / Empresa",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    HorizontalDivider()

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { viewModel.onNombreChanged(it) },
                        label = { Text("Nombre / Razón Social") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = apellido,
                        onValueChange = { viewModel.onApellidoChanged(it) },
                        label = { Text("Apellidos (Opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = giro,
                        onValueChange = { viewModel.onGiroChanged(it) },
                        label = { Text("Giro / Rubro (Opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = contactoNombre,
                        onValueChange = { viewModel.onContactoNombreChanged(it) },
                        label = { Text("Nombre de Contacto (Opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { viewModel.onTelefonoChanged(it) },
                        label = { Text("Teléfono de Contacto") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    BotonPrincipal(
                        texto = "Guardar Cambios",
                        onClick = { viewModel.guardarCambiosPerfil() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PerfilScreenPreview() {
    MiRegistroTheme {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Vista previa Perfil",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
