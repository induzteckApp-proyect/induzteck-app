package com.example.induzteckapp.ui.theme.screens

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induzteckapp.R
import com.example.induzteckapp.model.Comuna
import com.example.induzteckapp.model.CrearProtocoloUiState
import com.example.induzteckapp.model.Region
import com.example.induzteckapp.model.TipoEquipo
import com.example.induzteckapp.permisos.PermisosUbicacion
import com.example.induzteckapp.ui.theme.components.BotonPrincipal
import com.example.induzteckapp.ui.theme.components.BotonSecundario
import com.example.induzteckapp.ui.theme.components.TarjetaSeccion
import com.example.induzteckapp.ui.theme.theme.MiRegistroTheme
import com.example.induzteckapp.viewmodel.CrearProtocoloViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearProtocoloScreen(
    onVolver: () -> Unit,
    onIrABorradores: () -> Unit = {},
    onIrAHistorial: () -> Unit = {},
    idBorrador: Long = -1L,
    viewModel: CrearProtocoloViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val locationManager = remember(context) {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    var cargandoUbicacion by remember { mutableStateOf(false) }
    var mensajeUbicacion by remember { mutableStateOf<String?>(null) }
    var intentoUbicacion by remember { mutableStateOf(0) }
    var mostrarAjustesPermisoUbicacion by remember { mutableStateOf(false) }
    var mostrarAjustesDispositivoUbicacion by remember { mutableStateOf(false) }

    fun procesarUbicacion(ubicacion: Location) {
        mensajeUbicacion = "Ubicación obtenida. Buscando una dirección legible…"
        Thread {
            var direccion: String? = null
            try {
                if (Geocoder.isPresent()) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    @Suppress("DEPRECATION")
                    val direcciones = geocoder.getFromLocation(
                        ubicacion.latitude,
                        ubicacion.longitude,
                        1
                    )
                    val primera = direcciones?.firstOrNull()
                    direccion = primera?.getAddressLine(0)
                        ?.takeIf { it.isNotBlank() }
                        ?: listOfNotNull(
                            primera?.subLocality,
                            primera?.locality,
                            primera?.adminArea,
                            primera?.countryName
                        ).joinToString(", ").takeIf { it.isNotBlank() }
                }
            } catch (_: Exception) {
                // La geocodificación puede no estar disponible en todos los dispositivos.
            }

            val direccionFinal = direccion
            val textoUbicacion = direccionFinal ?: String.format(
                Locale.US,
                "Coordenadas: %.6f, %.6f",
                ubicacion.latitude,
                ubicacion.longitude
            )
            val direccionLegibleObtenida = !direccionFinal.isNullOrBlank()

            Handler(Looper.getMainLooper()).post {
                if (!viewModel.uiState.value.mostrarFormularioInstalacion) return@post
                viewModel.actualizarUbicacionInstalacion(textoUbicacion)
                cargandoUbicacion = false
                mostrarAjustesPermisoUbicacion = false
                mostrarAjustesDispositivoUbicacion = false
                mensajeUbicacion = if (direccionLegibleObtenida) {
                    "Ubicación completada. Puedes editar la dirección antes de guardar."
                } else {
                    "No se obtuvo una dirección legible; se cargaron las coordenadas y puedes editarlas."
                }
            }
        }.start()
    }

    fun consultarProveedor(proveedores: List<String>, indice: Int = 0) {
        if (indice >= proveedores.size) {
            cargandoUbicacion = false
            mensajeUbicacion = "No se pudo obtener una ubicación actual. Puedes escribir la dirección manualmente."
            return
        }

        val proveedor = proveedores[indice]
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                locationManager.getCurrentLocation(
                    proveedor,
                    null,
                    context.mainExecutor
                ) { ubicacion ->
                    if (ubicacion != null) {
                        procesarUbicacion(ubicacion)
                    } else {
                        consultarProveedor(proveedores, indice + 1)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val ultimaUbicacion = locationManager.getLastKnownLocation(proveedor)
                if (ultimaUbicacion != null) {
                    procesarUbicacion(ultimaUbicacion)
                } else {
                    val listener = object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            locationManager.removeUpdates(this)
                            procesarUbicacion(location)
                        }

                        @Deprecated("Deprecated in Android")
                        override fun onStatusChanged(
                            provider: String?,
                            status: Int,
                            extras: Bundle?
                        ) = Unit

                        override fun onProviderEnabled(provider: String) = Unit
                        override fun onProviderDisabled(provider: String) = Unit
                    }
                    @Suppress("DEPRECATION")
                    locationManager.requestSingleUpdate(
                        proveedor,
                        listener,
                        Looper.getMainLooper()
                    )
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (cargandoUbicacion) {
                            runCatching { locationManager.removeUpdates(listener) }
                            cargandoUbicacion = false
                            mensajeUbicacion = "El dispositivo no entregó una ubicación a tiempo. Puedes ingresar la dirección manualmente."
                        }
                    }, 15000L)
                }
            }
        } catch (_: SecurityException) {
            cargandoUbicacion = false
            mensajeUbicacion = "Android no permitió acceder a la ubicación. Puedes continuar escribiendo la dirección."
            mostrarAjustesPermisoUbicacion = true
        } catch (_: Exception) {
            cargandoUbicacion = false
            mensajeUbicacion = "Ocurrió un problema al consultar la ubicación. Puedes ingresarla manualmente."
        }
    }

    fun obtenerUbicacionActual() {
        if (!PermisosUbicacion.tienePermiso(context)) {
            mensajeUbicacion = null
            return
        }

        val proveedores = try {
            listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
                .filter { proveedor ->
                    runCatching { locationManager.isProviderEnabled(proveedor) }.getOrDefault(false)
                }
        } catch (_: Exception) {
            emptyList()
        }

        if (proveedores.isEmpty()) {
            cargandoUbicacion = false
            mensajeUbicacion = "Activa los servicios de ubicación del dispositivo o escribe la dirección manualmente."
            mostrarAjustesDispositivoUbicacion = true
            return
        }

        intentoUbicacion += 1
        val intentoActual = intentoUbicacion
        cargandoUbicacion = true
        mensajeUbicacion = "Obteniendo ubicación…"
        mostrarAjustesDispositivoUbicacion = false
        consultarProveedor(proveedores)

        Handler(Looper.getMainLooper()).postDelayed({
            if (
                intentoUbicacion == intentoActual &&
                cargandoUbicacion &&
                mensajeUbicacion == "Obteniendo ubicación…"
            ) {
                cargandoUbicacion = false
                mensajeUbicacion = "No se recibió una ubicación a tiempo. Puedes escribir la dirección manualmente."
            }
        }, 20000L)
    }

    val lanzadorPermisoUbicacion = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val concedido = permisos.values.any { it } || PermisosUbicacion.tienePermiso(context)
        if (concedido) {
            obtenerUbicacionActual()
        } else {
            cargandoUbicacion = false
            mensajeUbicacion = "Permiso denegado. Puedes escribir la dirección manualmente o revisar los permisos en Ajustes."
            mostrarAjustesPermisoUbicacion = true
        }
    }

    fun solicitarUbicacion() {
        mensajeUbicacion = null
        mostrarAjustesPermisoUbicacion = false
        mostrarAjustesDispositivoUbicacion = false
        if (PermisosUbicacion.tienePermiso(context)) {
            obtenerUbicacionActual()
        } else {
            lanzadorPermisoUbicacion.launch(PermisosUbicacion.PERMISOS_UBICACION)
        }
    }

    LaunchedEffect(idBorrador) {
        if (idBorrador > 0L) viewModel.cargarBorrador(idBorrador)
    }

    LaunchedEffect(estado.mostrarFormularioInstalacion) {
        if (!estado.mostrarFormularioInstalacion) {
            mensajeUbicacion = null
            mostrarAjustesPermisoUbicacion = false
            mostrarAjustesDispositivoUbicacion = false
            cargandoUbicacion = false
        }
    }

    val titulosPasos = listOf(
        "Contexto de terreno",
        "Condiciones ambientales",
        "Mediciones"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo protocolo") },
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
                .imePadding()
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

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Completa los datos del terreno y registra las mediciones del equipo.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                TextButton(onClick = onIrABorradores) {
                    Text("Ver y recuperar borradores")
                }
            }

            TextButton(onClick = onIrAHistorial, modifier = Modifier.fillMaxWidth()) {
                Text("Ver Historial")
            }

            TarjetaSeccion(titulo = "Progreso del protocolo") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    titulosPasos.forEachIndexed { indice, titulo ->
                        val numero = indice + 1
                        val actual = estado.pasoActual == numero
                        val completado = estado.pasoActual > numero

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (completado) "✓" else numero.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (actual || completado) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
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
                                fontWeight = if (actual) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                },
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            if (estado.cargando) {
                Text(
                    text = "Cargando datos...",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            estado.mensajeError?.let { mensaje ->
                MensajeEstado(mensaje, esError = true)
            }

            estado.mensajeExito?.let { mensaje ->
                MensajeEstado(mensaje, esError = false)
            }

            if (!estado.protocoloFinalizado) {
                when (estado.pasoActual) {

                    1 -> PasoContexto(
                        estado = estado,
                        viewModel = viewModel,
                        context = context,
                        cargandoUbicacion = cargandoUbicacion,
                        mensajeUbicacion = mensajeUbicacion,
                        mostrarAjustesPermisoUbicacion = mostrarAjustesPermisoUbicacion,
                        mostrarAjustesDispositivoUbicacion = mostrarAjustesDispositivoUbicacion,
                        onSolicitarUbicacion = { solicitarUbicacion() }
                    )

                    2 -> PasoAmbiental(estado = estado, viewModel = viewModel)
                    3 -> PasoMediciones(estado = estado, viewModel = viewModel)
                }
            } else {
                TarjetaSeccion(titulo = "Protocolo registrado en terreno") {
                    Text("Cliente: ${estado.clienteSeleccionado?.nombreDesplegable.orEmpty()}")
                    Text("Instalación: ${estado.instalacionSeleccionada?.nombre.orEmpty()}")
                    Text("Equipo: ${estado.equipoSeleccionado?.tag.orEmpty()} — ${estado.equipoSeleccionado?.nombreTipoEquipo.orEmpty()}")
                    Text("Condiciones ambientales: ${estado.temperaturaC} °C · ${estado.humedadPct} % HR")
                    Text("Resultados individuales", fontWeight = FontWeight.Bold)
                    estado.resumenResultados.forEach { Text("• $it") }
                    Text("El detalle completo, incluidas las mediciones guardadas, está disponible en HistorialProtocolos.", style = MaterialTheme.typography.bodySmall)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (estado.protocoloFinalizado) {
                    BotonSecundario(texto = "Volver al inicio", onClick = onVolver, modifier = Modifier.weight(1f))
                } else if (estado.pasoActual == 1) {
                    BotonSecundario(texto = "Cancelar", onClick = onVolver, modifier = Modifier.weight(1f))
                } else {
                    BotonSecundario(texto = "Anterior", onClick = viewModel::retrocederPaso, modifier = Modifier.weight(1f))
                }

                if (estado.protocoloFinalizado) {
                    BotonPrincipal(texto = "Ver historial", onClick = onIrAHistorial, modifier = Modifier.weight(1f))
                } else if (estado.pasoActual < 3) {
                    BotonPrincipal(
                        texto = "Continuar",
                        onClick = viewModel::avanzarPaso,
                        modifier = Modifier.weight(1f),
                        habilitado = !estado.cargando && !estado.guardando
                    )
                } else {
                    BotonPrincipal(
                        texto = when {
                            estado.protocoloFinalizado -> "Registrado"
                            estado.guardando -> "Guardando..."
                            else -> "Guardar prueba y continuar"
                        },
                        onClick = viewModel::guardarProtocolo,
                        modifier = Modifier.weight(1f),
                        habilitado = !estado.guardando && !estado.cargando && !estado.protocoloFinalizado
                    )
                }
            }

            Text(
                text = "La fecha y hora se registran al confirmar el protocolo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PasoContexto(

    estado: CrearProtocoloUiState,
    viewModel: CrearProtocoloViewModel,
    context: Context,
    cargandoUbicacion: Boolean,
    mensajeUbicacion: String?,
    mostrarAjustesPermisoUbicacion: Boolean,
    mostrarAjustesDispositivoUbicacion: Boolean,
    onSolicitarUbicacion: () -> Unit


) {
    TarjetaSeccion(titulo = "1. Contexto de terreno") {
        Text(
            text = "Selecciona el cliente, la instalación, el equipo y la prueba que realizarás.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider()

        Selector(
            etiqueta = "Cliente",
            seleccionado = estado.clienteSeleccionado?.nombreDesplegable.orEmpty(),
            opciones = estado.clientes.map { it.nombreDesplegable },
            habilitado = !estado.cargando && !estado.guardando && estado.idProtocoloBorrador == null,
            error = estado.erroresCampos["clienteSeleccionado"] != null,
            mensajeError = estado.erroresCampos["clienteSeleccionado"],
            alSeleccionar = { indice ->
                viewModel.seleccionarCliente(estado.clientes[indice])
            }
        )

        if (estado.clienteSeleccionado != null) {
            Selector(
                etiqueta = "Instalación",
                seleccionado = estado.instalacionSeleccionada?.let {
                    "${it.nombre} — ${it.nombreComuna}"
                }.orEmpty(),
                opciones = estado.instalaciones.map {
                    "${it.nombre} — ${it.nombreComuna}"
                },
                habilitado = !estado.guardando && estado.idProtocoloBorrador == null,
                error = estado.erroresCampos["instalacionSeleccionada"] != null,
                mensajeError = estado.erroresCampos["instalacionSeleccionada"],
                alSeleccionar = { indice ->
                    viewModel.seleccionarInstalacion(
                        estado.instalaciones[indice]
                    )
                }
            )

            TextButton(
                onClick = {
                    viewModel.mostrarFormularioInstalacion(
                        !estado.mostrarFormularioInstalacion
                    )
                }
            ) {
                Text(
                    if (estado.mostrarFormularioInstalacion) {
                        "Cancelar nueva instalación"
                    } else {
                        "+ Registrar instalación"
                    }
                )
            }

            if (estado.mostrarFormularioInstalacion) {
                FormularioInstalacion(
                    estado = estado,
                    viewModel = viewModel,
                    cargandoUbicacion = cargandoUbicacion,
                    mensajeUbicacion = mensajeUbicacion,
                    mostrarAjustesPermisoUbicacion = mostrarAjustesPermisoUbicacion,
                    mostrarAjustesDispositivoUbicacion = mostrarAjustesDispositivoUbicacion,
                    onSolicitarUbicacion = onSolicitarUbicacion,
                    onAbrirAjustesPermiso = {
                        runCatching {
                            PermisosUbicacion.abrirAjustesAplicacion(context)
                        }
                    },
                    onAbrirAjustesDispositivo = {
                        runCatching {
                            PermisosUbicacion.abrirAjustesUbicacion(context)
                        }
                    }
                )
            }

        }

        if (estado.instalacionSeleccionada != null) {
            Selector(
                etiqueta = "Equipo",
                seleccionado = estado.equipoSeleccionado?.let {
                    "${it.tag} — ${it.nombreTipoEquipo}"
                }.orEmpty(),
                opciones = estado.equipos.map {
                    "${it.tag} — ${it.nombreTipoEquipo}"
                },
                habilitado = !estado.guardando && estado.idProtocoloBorrador == null,
                error = estado.erroresCampos["equipoSeleccionado"] != null,
                mensajeError = estado.erroresCampos["equipoSeleccionado"],
                alSeleccionar = { indice ->
                    viewModel.seleccionarEquipo(estado.equipos[indice])
                }
            )

            TextButton(
                onClick = {
                    viewModel.mostrarFormularioEquipo(
                        !estado.mostrarFormularioEquipo
                    )
                }
            ) {
                Text(
                    if (estado.mostrarFormularioEquipo) {
                        "Cancelar nuevo equipo"
                    } else {
                        "+ Registrar equipo"
                    }
                )
            }

            if (estado.mostrarFormularioEquipo) {
                FormularioEquipo(estado, viewModel)
            }
        }

        if (estado.equipoSeleccionado != null) {
            Text(
                text = "Pruebas eléctricas (puedes seleccionar varias)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            estado.pruebasCompatibles.forEach { prueba ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = estado.pruebasSeleccionadas.any {
                            it.idTipoPrueba == prueba.idTipoPrueba
                        } || prueba.idTipoPrueba in estado.pruebasCompletadas,
                        enabled = !estado.guardando && estado.idProtocoloBorrador == null && prueba.idTipoPrueba !in estado.pruebasCompletadas,
                        onCheckedChange = { marcado ->
                            viewModel.alternarPrueba(prueba, marcado)
                        }
                    )
                    Text(
                        if (prueba.idTipoPrueba in estado.pruebasCompletadas) {
                            "${prueba.nombre} (completada)"
                        } else {
                            prueba.nombre
                        }
                    )
                }
            }
            estado.erroresCampos["pruebasSeleccionadas"]?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "Las pruebas se solicitarán una por una en el orden seleccionado.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FormularioInstalacion(
    estado: CrearProtocoloUiState,
    viewModel: CrearProtocoloViewModel,
    cargandoUbicacion: Boolean,
    mensajeUbicacion: String?,
    mostrarAjustesPermisoUbicacion: Boolean,
    mostrarAjustesDispositivoUbicacion: Boolean,
    onSolicitarUbicacion: () -> Unit,
    onAbrirAjustesPermiso: () -> Unit,
    onAbrirAjustesDispositivo: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Nueva instalación",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        CampoTexto(
            etiqueta = "Nombre de la instalación *",
            valor = estado.nombreInstalacion,
            alCambiar = viewModel::actualizarNombreInstalacion,
            error = estado.erroresCampos["nombreInstalacion"] != null,
            mensajeError = estado.erroresCampos["nombreInstalacion"]
        )

        CampoTexto(
            etiqueta = "Ubicación o dirección *",
            valor = estado.ubicacionInstalacion,
            alCambiar = viewModel::actualizarUbicacionInstalacion,
            error = estado.erroresCampos["ubicacionInstalacion"] != null ||
                    (estado.mensajeError != null && estado.ubicacionInstalacion.isBlank()),
            mensajeError = estado.erroresCampos["ubicacionInstalacion"]
        )

        BotonSecundario(
            texto = if (cargandoUbicacion) "Obteniendo ubicación…" else "Usar ubicación actual",
            onClick = onSolicitarUbicacion,
            modifier = Modifier.fillMaxWidth(),
            habilitado = !cargandoUbicacion && !estado.guardando
        )
        Text(
            text = "Opcional: puedes completar este campo con tu ubicación y editar la dirección manualmente.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        mensajeUbicacion?.let { mensaje ->
            Text(
                text = mensaje,
                color = if (mensaje.startsWith("No se pudo") ||
                    mensaje.startsWith("Permiso denegado") ||
                    mensaje.startsWith("Activa") ||
                    mensaje.startsWith("Ocurrió") ||
                    mensaje.startsWith("Android no")
                ) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
        if (mostrarAjustesPermisoUbicacion) {
            TextButton(onClick = onAbrirAjustesPermiso) {
                Text("Abrir ajustes de permisos")
            }
        }
        if (mostrarAjustesDispositivoUbicacion) {
            TextButton(onClick = onAbrirAjustesDispositivo) {
                Text("Activar ubicación del dispositivo")
            }
        }

        Selector(
            etiqueta = "Región *",
            seleccionado = estado.regionSeleccionada?.nombre.orEmpty(),
            opciones = estado.regiones.map { it.nombre },
            error = estado.erroresCampos["regionSeleccionada"] != null,
            mensajeError = estado.erroresCampos["regionSeleccionada"],
            alSeleccionar = { indice ->
                viewModel.seleccionarRegion(estado.regiones[indice])
            }
        )

        val comunasFiltradas = estado.regionSeleccionada?.let { region ->
            estado.comunas.filter { it.idRegion == region.idRegion }
        }.orEmpty()

        Selector(
            etiqueta = "Comuna *",
            seleccionado = estado.comunaSeleccionada?.nombre.orEmpty(),
            opciones = comunasFiltradas.map { it.nombre },
            habilitado = estado.regionSeleccionada != null,
            error = estado.erroresCampos["comunaSeleccionada"] != null,
            mensajeError = estado.erroresCampos["comunaSeleccionada"],
            alSeleccionar = { indice ->
                viewModel.seleccionarComuna(comunasFiltradas[indice])
            }
        )

        CampoTexto(
            etiqueta = "Observaciones de acceso (opcional)",
            valor = estado.observacionesAcceso,
            alCambiar = viewModel::actualizarObservacionesAcceso,
            unaLinea = false
        )

        Text(
            text = "La instalación se guardará en SQLite al pulsar el botón.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        BotonPrincipal(
            texto = if (estado.guardando) "Guardando..." else "Guardar instalación",
            onClick = viewModel::crearInstalacion,
            modifier = Modifier.fillMaxWidth(),
            habilitado = !estado.guardando
        )
    }
}

@Composable
private fun FormularioEquipo(
    estado: CrearProtocoloUiState,
    viewModel: CrearProtocoloViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Nuevo equipo",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Selector(
            etiqueta = "Tipo de equipo *",
            seleccionado = estado.tipoEquipoSeleccionado?.nombre.orEmpty(),
            opciones = estado.tiposEquipo.map { it.nombre },
            error = estado.erroresCampos["tipoEquipoSeleccionado"] != null,
            mensajeError = estado.erroresCampos["tipoEquipoSeleccionado"],
            alSeleccionar = { indice ->
                viewModel.seleccionarTipoEquipo(estado.tiposEquipo[indice])
            }
        )

        CampoTexto(
            etiqueta = "TAG del equipo *",
            valor = estado.tagEquipo,
            alCambiar = viewModel::actualizarTagEquipo,
            error = estado.erroresCampos["tagEquipo"] != null,
            mensajeError = estado.erroresCampos["tagEquipo"]
        )

        CampoTexto(
            etiqueta = "Marca (opcional)",
            valor = estado.marcaEquipo,
            alCambiar = viewModel::actualizarMarcaEquipo
        )

        CampoTexto(
            etiqueta = "Modelo (opcional)",
            valor = estado.modeloEquipo,
            alCambiar = viewModel::actualizarModeloEquipo
        )

        CampoTexto(
            etiqueta = "Número de serie (opcional)",
            valor = estado.numeroSerieEquipo,
            alCambiar = viewModel::actualizarNumeroSerieEquipo
        )

        CampoDecimal(
            etiqueta = "Potencia (kVA)",
            valor = estado.potenciaKva,
            alCambiar = viewModel::actualizarPotenciaKva,
            error = estado.erroresCampos["potenciaKva"] != null,
            mensajeError = estado.erroresCampos["potenciaKva"]
        )

        CampoDecimal(
            etiqueta = "Tensiones (V)",
            valor = estado.tensiones,
            alCambiar = viewModel::actualizarTensiones,
            error = estado.erroresCampos["tensiones"] != null,
            mensajeError = estado.erroresCampos["tensiones"]
        )

        CampoDecimal(
            etiqueta = "Corriente (A)",
            valor = estado.corriente,
            alCambiar = viewModel::actualizarCorriente,
            error = estado.erroresCampos["corriente"] != null,
            mensajeError = estado.erroresCampos["corriente"]
        )

        val tipoEquipoTexto = (estado.tipoEquipoSeleccionado?.nombre.orEmpty() + " " +
                estado.tipoEquipoSeleccionado?.clasificacion.orEmpty()).lowercase()
        if ("interruptor" in tipoEquipoTexto || "celda" in tipoEquipoTexto) {
            CampoDecimal(
                etiqueta = "Resistencia máxima de contactos de placa (µΩ) *",
                valor = estado.maxContactosUohm,
                alCambiar = viewModel::actualizarMaxContactosUohm,
                nota = "Obligatoria para interruptores y celdas MT. Ejemplo: 50.0 µΩ.",
                error = estado.erroresCampos["maxContactosUohm"] != null,
                mensajeError = estado.erroresCampos["maxContactosUohm"]
            )
        }

        Text(
            text = "Los campos técnicos son opcionales. Si los completas, utiliza punto decimal.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        BotonPrincipal(
            texto = if (estado.guardando) "Guardando..." else "Guardar equipo",
            onClick = viewModel::crearEquipo,
            modifier = Modifier.fillMaxWidth(),
            habilitado = !estado.guardando
        )
    }
}

@Composable
private fun PasoAmbiental(
    estado: CrearProtocoloUiState,
    viewModel: CrearProtocoloViewModel
) {
    TarjetaSeccion(titulo = "2. Condiciones ambientales") {
        Text(
            text = "La fecha y hora se registrarán automáticamente al guardar.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        CampoTemperatura(
            valor = estado.temperaturaC,
            alCambiar = viewModel::actualizarTemperatura,
            mensajeError = estado.erroresCampos["temperaturaC"]
        )

        CampoDecimal(
            etiqueta = "Humedad relativa (%)",
            valor = estado.humedadPct,
            alCambiar = viewModel::actualizarHumedad,
            nota = "Rango permitido: 0 a 100 %.",
            error = estado.erroresCampos["humedadPct"] != null,
            mensajeError = estado.erroresCampos["humedadPct"]
        )

        Text(
            text = "Para decimales utiliza un punto. Ejemplo: 23.5",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PasoMediciones(
    estado: CrearProtocoloUiState,
    viewModel: CrearProtocoloViewModel
) {
    TarjetaSeccion(titulo = "3. Mediciones eléctricas") {
        Text(
            text = estado.pruebaSeleccionada?.nombre
                ?: "Selecciona una prueba en el paso anterior.",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Prueba ${estado.indicePruebaActual + 1} de ${estado.pruebasSeleccionadas.size}. Completa las mediciones para guardar su resultado.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val nombrePruebaActual = estado.pruebaSeleccionada?.nombre.orEmpty()
        val tipoEquipoActual = (estado.equipoSeleccionado?.nombreTipoEquipo.orEmpty() + " " +
                estado.equipoSeleccionado?.clasificacionTipo.orEmpty()).lowercase()
        val nombrePruebaMinuscula = nombrePruebaActual.lowercase()
        val elementosNecesarios = when {
            ("aislación" in nombrePruebaMinuscula || "aislacion" in nombrePruebaMinuscula) &&
                    "transformador" in tipoEquipoActual -> listOf("AT-BT", "AT-T", "BT-T")
            ("aislación" in nombrePruebaMinuscula || "aislacion" in nombrePruebaMinuscula) &&
                    "cable" in tipoEquipoActual -> listOf("Fase A", "Fase B", "Fase C")
            "contacto" in nombrePruebaMinuscula -> listOf("Polo 1", "Polo 2", "Polo 3")
            else -> emptyList()
        }
        if (elementosNecesarios.isNotEmpty()) {
            val disponibles = elementosNecesarios.filter { elemento ->
                "${estado.pruebaSeleccionada?.idTipoPrueba}|${elemento.lowercase()}" !in estado.elementosCompletados
            }
            Selector(
                etiqueta = "Elemento / fase / polo *",
                seleccionado = estado.elementoPrueba,
                opciones = disponibles,
                habilitado = disponibles.isNotEmpty() && !estado.guardando,
                error = estado.erroresCampos["elementoPrueba"] != null,
                mensajeError = estado.erroresCampos["elementoPrueba"],
                alSeleccionar = { indice -> viewModel.actualizarElementoPrueba(disponibles[indice]) }
            )
            val guardados = elementosNecesarios.filter { elemento ->
                "${estado.pruebaSeleccionada?.idTipoPrueba}|${elemento.lowercase()}" in estado.elementosCompletados
            }
            Text(
                text = "Elementos requeridos: ${elementosNecesarios.joinToString(", ")}. Ya guardados: ${guardados.ifEmpty { listOf("ninguno") }.joinToString(", ")}.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        val requiereUnidadAislacion = estado.parametrosPrueba.any {
            it.codigo == "LEC_1MIN" || it.codigo == "LEC_10MIN"
        }

        if (requiereUnidadAislacion) {
            Text(
                text = "Unidad para las lecturas de aislación",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("MΩ", "GΩ").forEach { unidad ->
                    if (estado.unidadAislacion == unidad) {
                        Button(
                            onClick = {
                                viewModel.actualizarUnidadAislacion(unidad)
                            }
                        ) {
                            Text(unidad)
                        }
                    } else {
                        TextButton(
                            onClick = {
                                viewModel.actualizarUnidadAislacion(unidad)
                            }
                        ) {
                            Text(unidad)
                        }
                    }
                }
            }

            Text(
                text = "Las lecturas ingresadas en GΩ se convertirán a MΩ al guardarse.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (estado.parametrosPrueba.isEmpty()) {
            Text(
                text = "No hay parámetros disponibles para esta prueba.",
                color = MaterialTheme.colorScheme.error
            )
        } else {
            Text(
                "Ingresa valores numéricos válidos. Las magnitudes positivas deben ser mayores que cero; la posición TAP admite enteros negativos, cero y positivos. Para la tensión de prueba, sigue el procedimiento aplicable al equipo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            estado.parametrosPrueba.forEach { parametro ->
                val unidad = if (
                    requiereUnidadAislacion &&
                    (parametro.codigo == "LEC_1MIN" ||
                            parametro.codigo == "LEC_10MIN")
                ) {
                    estado.unidadAislacion
                } else {
                    parametro.unidad
                }

                val notaParametro = when (parametro.codigo) {
                    "T_PRUEBA" -> "Tensión de ensayo en kV. Ejemplos de referencia: 1.0, 2.5, 5.0 o 10.0; confirma siempre el procedimiento aplicable al equipo."
                    "LEC_1MIN" -> "Lectura tomada al minuto. Ejemplo: 500 MΩ."
                    "LEC_10MIN" -> "Lectura tomada a los diez minutos. Ejemplo: 1200 MΩ."
                    "POS_TAP" -> "Posición del selector TAP como número entero; por ahora solo se admite valores positivos."
                    "REL_TEORICA" -> "Relación teórica indicada para el transformador. Ejemplo: 10.0."
                    "REL_MEDIDA" -> "Relación medida durante la prueba. Ejemplo: 10.03."
                    "AT_BT", "AT_T", "BT_T" -> "Registra el valor medido en mΩ; no hay un umbral automático definido para esta prueba."
                    "RES_CONTACTO" -> "Valor por polo en µΩ; se comparará con la resistencia máxima de placa del equipo."
                    "CORRIENTE_AJUSTE" -> "Corriente de ajuste configurada en el relé, en A."
                    "CORRIENTE_MEDIDA" -> "Corriente medida durante el ensayo, en A."
                    "TIEMPO_ESPERADO" -> "Tiempo esperado de operación, en ms. Debe ser mayor que cero."
                    "TIEMPO_MEDIDO" -> "Tiempo real medido durante el ensayo, en ms. Debe ser mayor que cero."
                    else -> null
                }
                CampoDecimal(
                    etiqueta = "${parametro.nombre} ($unidad)",
                    valor = estado.mediciones[parametro.codigo].orEmpty(),
                    alCambiar = { valor -> viewModel.actualizarMedicion(parametro.codigo, valor) },
                    nota = notaParametro,
                    error = estado.erroresCampos[parametro.codigo] != null,
                    mensajeError = estado.erroresCampos[parametro.codigo]
                )
            }
        }

        if (estado.pruebaSeleccionada?.nombre.orEmpty().contains("relé", ignoreCase = true) ||
            estado.pruebaSeleccionada?.nombre.orEmpty().contains("rele", ignoreCase = true)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = estado.releOpero,
                    onCheckedChange = viewModel::actualizarReleOpero
                )
                Text("El relé operó correctamente")
            }
        }

        val requiereEvaluacionManual = estado.pruebaSeleccionada?.nombre.orEmpty().contains("devanado", ignoreCase = true) ||
                (("aislación" in nombrePruebaMinuscula || "aislacion" in nombrePruebaMinuscula) &&
                        estado.parametrosPrueba.none { it.codigo == "LEC_10MIN" })
        if (requiereEvaluacionManual) {
            Text("Evaluación manual del técnico")
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(
                    1 to "Aprobado",
                    2 to "Observado",
                    3 to "Rechazado"
                ).forEach { (valor, texto) ->
                    TextButton(onClick = { viewModel.actualizarResultadoManual(valor) }) {
                        Text(if (estado.resultadoManual == valor) "✓ $texto" else texto)
                    }
                }
            }
        }

        Text(
            text = "El resultado se calcula según las reglas de evaluación acordadas y se guarda junto a las mediciones.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Selector(
    etiqueta: String,
    seleccionado: String,
    opciones: List<String>,
    habilitado: Boolean = true,
    error: Boolean = false,
    mensajeError: String? = null,
    alSeleccionar: (Int) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = {
            if (habilitado && opciones.isNotEmpty()) {
                expandido = !expandido
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = seleccionado,
            onValueChange = {},
            readOnly = true,
            enabled = habilitado,
            label = { Text(etiqueta) },
            isError = error || mensajeError != null,
            supportingText = if (error || mensajeError != null) ({
                Text(mensajeError ?: "Revisa este campo")
            }) else null,
            placeholder = {
                Text(
                    if (opciones.isEmpty()) "Sin opciones disponibles"
                    else "Seleccionar"
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            opciones.forEachIndexed { indice, opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        alSeleccionar(indice)
                        expandido = false
                    }
                )
            }
        }
    }
}

@Composable
private fun CampoTexto(
    etiqueta: String,
    valor: String,
    alCambiar: (String) -> Unit,
    unaLinea: Boolean = true,
    error: Boolean = false,
    mensajeError: String? = null
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        isError = error || mensajeError != null,
        supportingText = if (error || mensajeError != null) ({
            Text(mensajeError ?: "Revisa este campo")
        }) else null,
        modifier = Modifier.fillMaxWidth(),
        singleLine = unaLinea,
        minLines = if (unaLinea) 1 else 3
    )
}

@Composable
private fun CampoTemperatura(
    valor: String,
    alCambiar: (String) -> Unit,
    mensajeError: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = valor,
                onValueChange = alCambiar,
                label = { Text("Temperatura ambiente (°C)") },
                isError = mensajeError != null,
                supportingText = if (mensajeError != null) ({ Text(mensajeError) }) else null,
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            TextButton(
                onClick = {
                    when {
                        valor.startsWith("-") -> alCambiar(valor.removePrefix("-"))
                        valor.isNotBlank() -> alCambiar("-$valor")
                        else -> alCambiar("-")
                    }
                }
            ) {
                Text("±")
            }
        }
        Text(
            text = "Rango permitido: -10 a 60 °C. Ejemplo: 23.5. El botón ± permite ingresar valores negativos.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CampoDecimal(
    etiqueta: String,
    valor: String,
    alCambiar: (String) -> Unit,
    nota: String? = null,
    error: Boolean = false,
    mensajeError: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = valor,
            onValueChange = alCambiar,
            label = { Text(etiqueta) },
            isError = error || mensajeError != null,
            supportingText = if (error || mensajeError != null) ({
                Text(mensajeError ?: "Revisa este campo")
            }) else null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            )
        )

        nota?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MensajeEstado(
    mensaje: String,
    esError: Boolean
) {
    Text(
        text = mensaje,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = if (esError) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.primary
        },
        style = MaterialTheme.typography.bodyMedium
    )
}


