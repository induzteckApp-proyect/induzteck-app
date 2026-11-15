package com.example.induzteckapp.ui.theme.screens

import android.content.ActivityNotFoundException
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induzteckapp.R
import com.example.induzteckapp.permisos.PermisosCamara
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
    val contactoCargo by viewModel.contactoCargo.collectAsState()
    val telefono by viewModel.telefono.collectAsState()
    val avatarBytes by viewModel.avatarBytes.collectAsState()
    val mensaje by viewModel.mensajeState.collectAsState()
    val errorNombre by viewModel.errorNombre.collectAsState()
    val errorApellido by viewModel.errorApellido.collectAsState()
    val errorTelefono by viewModel.errorTelefono.collectAsState()
    val errorContacto by viewModel.errorContacto.collectAsState()
    val errorGiro by viewModel.errorGiro.collectAsState()
    val errorContactoCargo by viewModel.errorContactoCargo.collectAsState()

    var claveActual by remember { mutableStateOf("") }
    var claveNueva by remember { mutableStateOf("") }
    var confirmarClave by remember { mutableStateOf("") }
    var mostrarOpcionesAvatar by remember { mutableStateOf(false) }


    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->


        if (uri != null) {
            viewModel.onImagenSeleccionada(context, uri)
        }
    }


    val lanzadorCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { foto: Bitmap? ->


        if (foto != null) {
            viewModel.onFotoCapturada(foto)
        }
    }

    fun ejecutarCamara() {
        try {
            lanzadorCamara.launch(null)
        } catch (_: ActivityNotFoundException) {

        }
    }


    val permisoCamaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            ejecutarCamara()
        }
    }

    fun abrirCamara() {
        if (PermisosCamara.tienePermiso(context)) {
            ejecutarCamara()
        } else {
            permisoCamaraLauncher.launch(PermisosCamara.CAMARA)
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
                        avatarUrl = usuario?.avatarUrl,
                        descripcion = "Foto de perfil de ${usuario?.nombre ?: "usuario"}"
                    )

                    BotonSecundario(
                        texto = "Cambiar Avatar",
                        onClick = { mostrarOpcionesAvatar = true }
                    )
                }
            }


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
                        singleLine = true,
                        isError = errorNombre != null,
                        supportingText = errorNombre?.let { { Text(it) } }
                    )

                    OutlinedTextField(
                        value = apellido,
                        onValueChange = { viewModel.onApellidoChanged(it) },
                        label = { Text("Apellidos (Opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = errorApellido != null,
                        supportingText = errorApellido?.let { { Text(it) } }
                    )

                    OutlinedTextField(
                        value = giro,
                        onValueChange = { viewModel.onGiroChanged(it) },
                        label = { Text("Giro / Rubro") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = errorGiro != null,
                        supportingText = errorGiro?.let { { Text(it) } }
                    )

                    OutlinedTextField(
                        value = contactoNombre,
                        onValueChange = { viewModel.onContactoNombreChanged(it) },
                        label = { Text("Nombre de Contacto") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = errorContacto != null,
                        supportingText = errorContacto?.let { { Text(it) } }
                    )

                    OutlinedTextField(
                        value = contactoCargo,
                        onValueChange = { viewModel.onContactoCargoChanged(it) },
                        label = { Text("Cargo del contacto") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = errorContactoCargo != null,
                        supportingText = errorContactoCargo?.let { { Text(it) } }
                    )

                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { viewModel.onTelefonoChanged(it) },
                        label = { Text("Teléfono de Contacto") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = errorTelefono != null,
                        supportingText = errorTelefono?.let { { Text(it) } }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    BotonPrincipal(
                        texto = "Guardar Cambios",
                        onClick = { viewModel.guardarCambiosPerfil() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Seguridad de la cuenta", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    HorizontalDivider()
                    OutlinedTextField(
                        value = claveActual, onValueChange = { claveActual = it },
                        label = { Text("Contraseña actual") }, modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(), singleLine = true
                    )
                    OutlinedTextField(
                        value = claveNueva, onValueChange = { claveNueva = it },
                        label = { Text("Nueva contraseña") }, modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(), singleLine = true
                    )
                    OutlinedTextField(
                        value = confirmarClave, onValueChange = { confirmarClave = it },
                        label = { Text("Confirmar nueva contraseña") }, modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(), singleLine = true
                    )
                    Text("Mínimo 8 caracteres, mayúscula, minúscula, número y símbolo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    BotonSecundario(
                        texto = "Cambiar contraseña",
                        onClick = {
                            viewModel.cambiarContrasena(claveActual, claveNueva, confirmarClave)
                            claveActual = ""; claveNueva = ""; confirmarClave = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }


    if (mostrarOpcionesAvatar) {
        AlertDialog(
            onDismissRequest = { mostrarOpcionesAvatar = false },
            title = { Text("Seleccionar Imagen de Avatar") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                mostrarOpcionesAvatar = false
                                abrirCamara()
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_camara),
                            contentDescription = "Cámara",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Tomar foto con la cámara",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                mostrarOpcionesAvatar = false
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_galeria),
                            contentDescription = "Galería",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Elegir de la galería",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { mostrarOpcionesAvatar = false }) {
                    Text("Cancelar")
                }
            }
        )
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
