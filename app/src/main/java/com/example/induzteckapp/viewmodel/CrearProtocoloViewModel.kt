// CrearProtocoloViewModel

package com.example.induzteckapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.induzteckapp.data.InduztekDbHelper
import com.example.induzteckapp.data.InduztekPreferencias
import com.example.induzteckapp.model.ClienteProtocolo
import com.example.induzteckapp.model.Comuna
import com.example.induzteckapp.model.CrearProtocoloUiState
import com.example.induzteckapp.model.Equipo
import com.example.induzteckapp.model.Instalacion
import com.example.induzteckapp.model.Region
import com.example.induzteckapp.model.TipoEquipo
import com.example.induzteckapp.model.TipoPrueba
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrearProtocoloViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val dbHelper = InduztekDbHelper(application)

    // Solo se consulta la identidad de la sesión iniciada.
    // Las preferencias no almacenan el protocolo ni sus formularios.
    private val preferencias = InduztekPreferencias(application)

    private val _uiState = MutableStateFlow(CrearProtocoloUiState())
    val uiState: StateFlow<CrearProtocoloUiState> = _uiState.asStateFlow()

    init {
        cargarDatosIniciales()
    }

    private fun cargarDatosIniciales() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(cargando = true, mensajeError = null)
            }

            try {
                val datos = withContext(Dispatchers.IO) {
                    Triple(
                        dbHelper.obtenerClientesProtocolo(),
                        dbHelper.obtenerComunas(),
                        dbHelper.obtenerTiposEquipo()
                    ) to dbHelper.obtenerRegiones()
                }

                _uiState.update {
                    it.copy(
                        clientes = datos.first.first,
                        comunas = datos.first.second,
                        tiposEquipo = datos.first.third,
                        regiones = datos.second,
                        cargando = false
                    )
                }
            } catch (e: Exception) {
                mostrarError("No fue posible cargar los catálogos desde SQLite.")
                _uiState.update { it.copy(cargando = false) }
            }
        }
    }

    // ---------------------------------------------------------
    // PASO 1: CLIENTE, INSTALACIÓN, EQUIPO Y PRUEBA
    // ---------------------------------------------------------

    fun seleccionarCliente(cliente: ClienteProtocolo) {
        _uiState.update {
            it.copy(
                clienteSeleccionado = cliente,
                instalaciones = emptyList(),
                equipos = emptyList(),
                instalacionSeleccionada = null,
                equipoSeleccionado = null,
                pruebaSeleccionada = null,
                pruebasCompatibles = emptyList(),
                parametrosPrueba = emptyList(),
                mostrarFormularioInstalacion = false,
                mostrarFormularioEquipo = false,
                mensajeError = null
            )
        }

        viewModelScope.launch {
            try {
                val instalaciones = withContext(Dispatchers.IO) {
                    dbHelper.obtenerInstalacionesPorCliente(cliente.idUsuario)
                }

                _uiState.update { estado ->
                    if (estado.clienteSeleccionado?.idUsuario == cliente.idUsuario) {
                        estado.copy(instalaciones = instalaciones)
                    } else {
                        estado
                    }
                }
            } catch (e: Exception) {
                mostrarError("No fue posible cargar las instalaciones del cliente.")
            }
        }
    }

    fun seleccionarRegion(region: Region) {
        _uiState.update {
            it.copy(
                regionSeleccionada = region,
                comunaSeleccionada = null,
                mensajeError = null
            )
        }
    }

    fun seleccionarComuna(comuna: Comuna) {
        _uiState.update {
            it.copy(comunaSeleccionada = comuna, mensajeError = null)
        }
    }

    fun mostrarFormularioInstalacion(mostrar: Boolean) {
        _uiState.update {
            it.copy(
                mostrarFormularioInstalacion = mostrar,
                mensajeError = null
            )
        }
    }

    fun actualizarNombreInstalacion(valor: String) =
        actualizar { it.copy(nombreInstalacion = valor) }

    fun actualizarUbicacionInstalacion(valor: String) =
        actualizar { it.copy(ubicacionInstalacion = valor) }

    fun actualizarObservacionesAcceso(valor: String) =
        actualizar { it.copy(observacionesAcceso = valor) }

    fun crearInstalacion() {
        val estado = _uiState.value
        val cliente = estado.clienteSeleccionado
        val comuna = estado.comunaSeleccionada

        if (cliente == null) {
            mostrarError("Primero debes seleccionar un cliente.")
            return
        }

        if (estado.nombreInstalacion.isBlank()) {
            mostrarError("Ingresa el nombre de la instalación.")
            return
        }

        if (estado.ubicacionInstalacion.isBlank()) {
            mostrarError("Ingresa la ubicación de la instalación.")
            return
        }

        if (estado.regionSeleccionada == null || comuna == null) {
            mostrarError("Selecciona una región y una comuna.")
            return
        }

        if (comuna.idRegion != estado.regionSeleccionada.idRegion) {
            mostrarError("La comuna seleccionada no corresponde a la región.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, mensajeError = null) }

            try {
                val idNuevo = withContext(Dispatchers.IO) {
                    dbHelper.insertarInstalacionProtocolo(
                        cliente.idUsuario,
                        estado.nombreInstalacion.trim(),
                        estado.ubicacionInstalacion.trim(),
                        comuna.idComuna,
                        estado.observacionesAcceso.trim().ifBlank { null }
                    )
                }

                if (idNuevo <= 0L) {
                    mostrarError("No fue posible registrar la instalación.")
                    return@launch
                }

                val instalacionesActualizadas = withContext(Dispatchers.IO) {
                    dbHelper.obtenerInstalacionesPorCliente(cliente.idUsuario)
                }

                val nuevaInstalacion =
                    instalacionesActualizadas.firstOrNull {
                        it.idInstalacion == idNuevo
                    }

                _uiState.update {
                    it.copy(
                        instalaciones = instalacionesActualizadas,
                        instalacionSeleccionada = nuevaInstalacion,
                        equipos = emptyList(),
                        equipoSeleccionado = null,
                        pruebaSeleccionada = null,
                        pruebasCompatibles = emptyList(),
                        parametrosPrueba = emptyList(),
                        mostrarFormularioInstalacion = false,
                        mostrarFormularioEquipo = false,
                        nombreInstalacion = "",
                        ubicacionInstalacion = "",
                        observacionesAcceso = "",
                        regionSeleccionada = null,
                        comunaSeleccionada = null,
                        guardando = false,
                        mensajeExito = "Instalación registrada correctamente."
                    )
                }
            } catch (e: Exception) {
                mostrarError(
                    "No fue posible guardar la instalación. " +
                            "Verifica los datos e inténtalo nuevamente."
                )
            } finally {
                _uiState.update { it.copy(guardando = false) }
            }
        }
    }

    fun seleccionarInstalacion(instalacion: Instalacion) {
        _uiState.update {
            it.copy(
                instalacionSeleccionada = instalacion,
                equipos = emptyList(),
                equipoSeleccionado = null,
                pruebaSeleccionada = null,
                pruebasCompatibles = emptyList(),
                parametrosPrueba = emptyList(),
                mostrarFormularioEquipo = false,
                mensajeError = null
            )
        }

        viewModelScope.launch {
            try {
                val equipos = withContext(Dispatchers.IO) {
                    dbHelper.obtenerEquiposPorInstalacion(
                        instalacion.idInstalacion
                    )
                }

                _uiState.update { estado ->
                    if (
                        estado.instalacionSeleccionada?.idInstalacion ==
                        instalacion.idInstalacion
                    ) {
                        estado.copy(equipos = equipos)
                    } else {
                        estado
                    }
                }
            } catch (e: Exception) {
                mostrarError("No fue posible cargar los equipos de la instalación.")
            }
        }
    }

    fun mostrarFormularioEquipo(mostrar: Boolean) {
        _uiState.update {
            it.copy(
                mostrarFormularioEquipo = mostrar,
                mensajeError = null
            )
        }
    }

    fun seleccionarTipoEquipo(tipo: TipoEquipo) {
        _uiState.update {
            it.copy(tipoEquipoSeleccionado = tipo, mensajeError = null)
        }
    }

    fun actualizarTagEquipo(valor: String) =
        actualizar { it.copy(tagEquipo = valor) }

    fun actualizarMarcaEquipo(valor: String) =
        actualizar { it.copy(marcaEquipo = valor) }

    fun actualizarModeloEquipo(valor: String) =
        actualizar { it.copy(modeloEquipo = valor) }

    fun actualizarNumeroSerieEquipo(valor: String) =
        actualizar { it.copy(numeroSerieEquipo = valor) }

    fun actualizarPotenciaKva(valor: String) =
        actualizar { it.copy(potenciaKva = valor) }

    fun actualizarTensiones(valor: String) =
        actualizar { it.copy(tensiones = valor) }

    fun actualizarCorriente(valor: String) =
        actualizar { it.copy(corriente = valor) }

    fun actualizarMaxContactosUohm(valor: String) =
        actualizar { it.copy(maxContactosUohm = valor) }

    fun crearEquipo() {
        val estado = _uiState.value
        val instalacion = estado.instalacionSeleccionada
        val tipo = estado.tipoEquipoSeleccionado

        if (instalacion == null) {
            mostrarError("Primero debes seleccionar una instalación.")
            return
        }

        if (estado.tagEquipo.isBlank()) {
            mostrarError("El TAG del equipo es obligatorio.")
            return
        }

        if (tipo == null) {
            mostrarError("Selecciona el tipo de equipo.")
            return
        }

        val potencia = convertirNumeroOpcional(estado.potenciaKva, "potencia") ?: run {
            if (estado.potenciaKva.isNotBlank()) return
            null
        }

        val tensiones = convertirNumeroOpcional(estado.tensiones, "tensiones") ?: run {
            if (estado.tensiones.isNotBlank()) return
            null
        }

        val corriente = convertirNumeroOpcional(estado.corriente, "corriente") ?: run {
            if (estado.corriente.isNotBlank()) return
            null
        }

        val maxContactos = convertirNumeroOpcional(
            estado.maxContactosUohm,
            "resistencia máxima de contactos"
        ) ?: run {
            if (estado.maxContactosUohm.isNotBlank()) return
            null
        }

        if (listOfNotNull(potencia, tensiones, corriente, maxContactos).any { it < 0 }) {
            mostrarError("Los datos numéricos del equipo no pueden ser negativos.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, mensajeError = null) }

            try {
                val idNuevo = withContext(Dispatchers.IO) {
                    dbHelper.insertarEquipoProtocolo(
                        instalacion.idInstalacion,
                        estado.tagEquipo.trim(),
                        tipo.idTipoEquipo,
                        estado.marcaEquipo.trim().ifBlank { null },
                        estado.modeloEquipo.trim().ifBlank { null },
                        estado.numeroSerieEquipo.trim().ifBlank { null },
                        potencia,
                        tensiones,
                        corriente,
                        maxContactos
                    )
                }

                if (idNuevo <= 0L) {
                    mostrarError(
                        "No se pudo registrar el equipo. " +
                                "Comprueba si ya existe ese TAG en la instalación."
                    )
                    return@launch
                }

                val equiposActualizados = withContext(Dispatchers.IO) {
                    dbHelper.obtenerEquiposPorInstalacion(
                        instalacion.idInstalacion
                    )
                }

                val nuevoEquipo = equiposActualizados.firstOrNull {
                    it.idEquipo == idNuevo
                }

                _uiState.update {
                    it.copy(
                        equipos = equiposActualizados,
                        equipoSeleccionado = nuevoEquipo,
                        tipoEquipoSeleccionado = null,
                        pruebaSeleccionada = null,
                        pruebasCompatibles = emptyList(),
                        parametrosPrueba = emptyList(),
                        mostrarFormularioEquipo = false,
                        tagEquipo = "",
                        marcaEquipo = "",
                        modeloEquipo = "",
                        numeroSerieEquipo = "",
                        potenciaKva = "",
                        tensiones = "",
                        corriente = "",
                        maxContactosUohm = "",
                        guardando = false,
                        mensajeExito = "Equipo registrado correctamente."
                    )
                }

                nuevoEquipo?.let { seleccionarEquipo(it) }
            } catch (e: Exception) {
                val mensaje = if (
                    e.message?.contains("UNIQUE", ignoreCase = true) == true
                ) {
                    "Ese TAG ya existe en esta instalación."
                } else {
                    "No fue posible guardar el equipo. Verifica los datos."
                }

                mostrarError(mensaje)
            } finally {
                _uiState.update { it.copy(guardando = false) }
            }
        }
    }

    fun seleccionarEquipo(equipo: Equipo) {
        _uiState.update {
            it.copy(
                equipoSeleccionado = equipo,
                pruebaSeleccionada = null,
                pruebasCompatibles = emptyList(),
                parametrosPrueba = emptyList(),
                mediciones = emptyMap(),
                mensajeError = null
            )
        }

        viewModelScope.launch {
            try {
                val pruebas = withContext(Dispatchers.IO) {
                    dbHelper.obtenerPruebasCompatibles(equipo.idTipoEquipo)
                }

                _uiState.update { estado ->
                    if (estado.equipoSeleccionado?.idEquipo == equipo.idEquipo) {
                        estado.copy(pruebasCompatibles = pruebas)
                    } else {
                        estado
                    }
                }
            } catch (e: Exception) {
                mostrarError("No fue posible cargar las pruebas compatibles.")
            }
        }
    }

    fun seleccionarPrueba(prueba: TipoPrueba) {
        _uiState.update {
            it.copy(
                pruebaSeleccionada = prueba,
                parametrosPrueba = emptyList(),
                mediciones = emptyMap(),
                mensajeError = null
            )
        }

        viewModelScope.launch {
            try {
                val parametros = withContext(Dispatchers.IO) {
                    dbHelper.obtenerParametrosPorTipoPrueba(prueba.idTipoPrueba)
                }

                _uiState.update { estado ->
                    if (
                        estado.pruebaSeleccionada?.idTipoPrueba ==
                        prueba.idTipoPrueba
                    ) {
                        estado.copy(parametrosPrueba = parametros)
                    } else {
                        estado
                    }
                }
            } catch (e: Exception) {
                mostrarError("No fue posible cargar los parámetros de la prueba.")
            }
        }
    }

    // ---------------------------------------------------------
    // PASO 2: CONDICIONES AMBIENTALES
    // ---------------------------------------------------------

    fun actualizarTemperatura(valor: String) =
        actualizar { it.copy(temperaturaC = valor) }

    fun actualizarHumedad(valor: String) =
        actualizar { it.copy(humedadPct = valor) }

    // ---------------------------------------------------------
    // PASO 3: MEDICIONES
    // ---------------------------------------------------------

    fun actualizarMedicion(codigo: String, valor: String) {
        _uiState.update {
            it.copy(
                mediciones = it.mediciones + (codigo to valor),
                mensajeError = null
            )
        }
    }

    fun actualizarUnidadAislacion(unidad: String) {
        if (unidad != "MΩ" && unidad != "GΩ") return

        _uiState.update {
            it.copy(unidadAislacion = unidad, mensajeError = null)
        }
    }

    // ---------------------------------------------------------
    // NAVEGACIÓN Y VALIDACIÓN
    // ---------------------------------------------------------

    fun avanzarPaso() {
        val estado = _uiState.value

        when (estado.pasoActual) {
            1 -> {
                if (
                    estado.clienteSeleccionado == null ||
                    estado.instalacionSeleccionada == null ||
                    estado.equipoSeleccionado == null ||
                    estado.pruebaSeleccionada == null
                ) {
                    mostrarError(
                        "Selecciona un cliente, instalación, equipo y prueba."
                    )
                    return
                }
            }

            2 -> {
                val temperatura = decimalValido(estado.temperaturaC)
                val humedad = decimalValido(estado.humedadPct)

                if (temperatura == null || temperatura !in -10.0..60.0) {
                    mostrarError(
                        "Ingresa una temperatura entre -10 y 60 °C, usando punto decimal."
                    )
                    return
                }

                if (humedad == null || humedad !in 0.0..100.0) {
                    mostrarError(
                        "Ingresa una humedad entre 0 y 100 %, usando punto decimal."
                    )
                    return
                }
            }

            else -> return
        }

        _uiState.update {
            it.copy(
                pasoActual = (it.pasoActual + 1).coerceAtMost(3),
                mensajeError = null,
                mensajeExito = null
            )
        }
    }

    fun retrocederPaso() {
        _uiState.update {
            it.copy(
                pasoActual = (it.pasoActual - 1).coerceAtLeast(1),
                mensajeError = null
            )
        }
    }

    // ---------------------------------------------------------
    // REGISTRO DEL PROTOCOLO
    // ---------------------------------------------------------

    fun guardarProtocolo() {
        val estado = _uiState.value

        if (estado.pasoActual != 3) return

        val equipo = estado.equipoSeleccionado
        val prueba = estado.pruebaSeleccionada

        if (equipo == null || prueba == null) {
            mostrarError("Falta seleccionar el equipo o la prueba.")
            return
        }

        val temperatura = decimalValido(estado.temperaturaC)
        val humedad = decimalValido(estado.humedadPct)

        if (temperatura == null || temperatura !in -10.0..60.0) {
            mostrarError("La temperatura no es válida.")
            return
        }

        if (humedad == null || humedad !in 0.0..100.0) {
            mostrarError("La humedad no es válida.")
            return
        }

        val parametros = estado.parametrosPrueba

        if (parametros.isEmpty()) {
            mostrarError("La prueba no tiene parámetros configurados.")
            return
        }

        val medicionesConvertidas = mutableMapOf<String, Double>()

        for (parametro in parametros) {
            val texto = estado.mediciones[parametro.codigo].orEmpty()

            if (texto.isBlank()) {
                mostrarError("Completa la medición: ${parametro.nombre}.")
                return
            }

            val numero = decimalValido(texto)

            if (numero == null) {
                mostrarError(
                    "La medición '${parametro.nombre}' debe ser numérica y usar punto decimal."
                )
                return
            }

            val esTiempo = parametro.codigo == "TIEMPO_ESPERADO" ||
                    parametro.codigo == "TIEMPO_MEDIDO"

            val esPosicionTap = parametro.codigo == "POS_TAP"

            if (esPosicionTap && (numero < 1 || numero % 1.0 != 0.0)) {
                mostrarError("La posición TAP debe ser un entero mayor o igual a 1.")
                return
            }

            if (esTiempo && numero < 0.0) {
                mostrarError("Los tiempos no pueden ser negativos.")
                return
            }

            if (!esTiempo && numero <= 0.0) {
                mostrarError(
                    "La medición '${parametro.nombre}' debe ser mayor que cero."
                )
                return
            }

            // SQLite guarda las lecturas de aislación en MΩ.
            // Si se ingresan en GΩ, se convierten antes del registro.
            val valorFinal = if (
                estado.unidadAislacion == "GΩ" &&
                (parametro.codigo == "LEC_1MIN" ||
                        parametro.codigo == "LEC_10MIN")
            ) {
                numero * 1000.0
            } else {
                numero
            }

            medicionesConvertidas[parametro.codigo] = valorFinal
        }

        val idUsuarioRegistra = preferencias.leerSesion()

        if (idUsuarioRegistra <= 0L) {
            mostrarError(
                "No se encontró una sesión válida. Inicia sesión nuevamente."
            )
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(guardando = true, mensajeError = null, mensajeExito = null)
            }

            try {

                val fechaHora = SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault()
                ).format(Date())

                val numeroProtocolo = withContext(Dispatchers.IO) {
                    dbHelper.registrarProtocolo(
                        equipo.idEquipo,
                        idUsuarioRegistra,
                        prueba.idTipoPrueba,
                        fechaHora,
                        temperatura,
                        humedad,
                        medicionesConvertidas
                    )
                }

                _uiState.update {
                    it.copy(
                        guardando = false,
                        mensajeExito =
                            "Protocolo $numeroProtocolo registrado en terreno.",
                        mensajeError = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        mensajeError =
                            "No fue posible registrar el protocolo. " +
                                    "No se confirmó el guardado; revisa los datos e inténtalo nuevamente."
                    )
                }
            }
        }
    }

    fun limpiarMensajes() {
        _uiState.update {
            it.copy(mensajeError = null, mensajeExito = null)
        }
    }

    private fun actualizar(
        cambio: (CrearProtocoloUiState) -> CrearProtocoloUiState
    ) {
        _uiState.update {
            cambio(it).copy(mensajeError = null, mensajeExito = null)
        }
    }

    private fun mostrarError(mensaje: String) {
        _uiState.update {
            it.copy(mensajeError = mensaje, mensajeExito = null)
        }
    }

    private fun decimalValido(texto: String): Double? {
        val limpio = texto.trim()

        // No acepta coma, separadores de miles, NaN ni Infinity.
        if (!limpio.matches(Regex("-?\\d+(\\.\\d+)?"))) return null

        return limpio.toDoubleOrNull()?.takeIf { it.isFinite() }
    }

    private fun convertirNumeroOpcional(
        texto: String,
        nombreCampo: String
    ): Double? {
        if (texto.isBlank()) return null

        val numero = decimalValido(texto)

        if (numero == null) {
            mostrarError(
                "El campo '$nombreCampo' debe ser numérico y usar punto decimal."
            )
        }

        return numero
    }

    override fun onCleared() {
        dbHelper.close()
        super.onCleared()
    }
}


/*
package com.example.induzteckapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.induzteckapp.data.InduztekDbHelper
import com.example.induzteckapp.model.ClienteProtocolo
import com.example.induzteckapp.model.CrearProtocoloUiState
import com.example.induzteckapp.model.Equipo
import com.example.induzteckapp.model.Instalacion
import com.example.induzteckapp.model.TipoPrueba
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CrearProtocoloViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val dbHelper = InduztekDbHelper(application)

    private val _uiState = MutableStateFlow(CrearProtocoloUiState())
    val uiState: StateFlow<CrearProtocoloUiState> = _uiState.asStateFlow()

    init {
        cargarDatosIniciales()
    }

    // Carga los catálogos que necesita el asistente.
    private fun cargarDatosIniciales() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(cargando = true, mensajeError = null)
            }

            try {
                val datos = withContext(Dispatchers.IO) {
                    Triple(
                        dbHelper.obtenerClientesProtocolo(),
                        dbHelper.obtenerComunas(),
                        dbHelper.obtenerTiposEquipo()
                    )
                }

                _uiState.update {
                    it.copy(
                        clientes = datos.first,
                        comunas = datos.second,
                        tiposEquipo = datos.third,
                        cargando = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        mensajeError =
                            "No fue posible cargar los datos desde SQLite."
                    )
                }
            }
        }
    }

    // Paso 1: seleccionar un cliente.
    fun seleccionarCliente(cliente: ClienteProtocolo) {
        _uiState.update {
            it.copy(
                clienteSeleccionado = cliente,
                instalacionSeleccionada = null,
                equipoSeleccionado = null,
                pruebaSeleccionada = null,
                instalaciones = emptyList(),
                equipos = emptyList(),
                pruebasCompatibles = emptyList(),
                mensajeError = null
            )
        }

        viewModelScope.launch {
            try {
                val instalaciones = withContext(Dispatchers.IO) {
                    dbHelper.obtenerInstalacionesPorCliente(
                        cliente.idUsuario
                    )
                }

                // No aplicar el resultado si el usuario ya cambió de cliente.
                _uiState.update { estado ->
                    if (estado.clienteSeleccionado?.idUsuario ==
                        cliente.idUsuario
                    ) {
                        estado.copy(instalaciones = instalaciones)
                    } else {
                        estado
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        mensajeError =
                            "No fue posible cargar las instalaciones."
                    )
                }
            }
        }
    }

    // Paso 1: seleccionar una instalación.
    fun seleccionarInstalacion(instalacion: Instalacion) {
        _uiState.update {
            it.copy(
                instalacionSeleccionada = instalacion,
                equipoSeleccionado = null,
                pruebaSeleccionada = null,
                equipos = emptyList(),
                pruebasCompatibles = emptyList(),
                mensajeError = null
            )
        }

        viewModelScope.launch {
            try {
                val equipos = withContext(Dispatchers.IO) {
                    dbHelper.obtenerEquiposPorInstalacion(
                        instalacion.idInstalacion
                    )
                }

                _uiState.update { estado ->
                    if (estado.instalacionSeleccionada?.idInstalacion ==
                        instalacion.idInstalacion
                    ) {
                        estado.copy(equipos = equipos)
                    } else {
                        estado
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        mensajeError =
                            "No fue posible cargar los equipos."
                    )
                }
            }
        }
    }

    // Paso 1: seleccionar un equipo y cargar sus pruebas permitidas.
    fun seleccionarEquipo(equipo: Equipo) {
        _uiState.update {
            it.copy(
                equipoSeleccionado = equipo,
                pruebaSeleccionada = null,
                pruebasCompatibles = emptyList(),
                mensajeError = null
            )
        }

        viewModelScope.launch {
            try {
                val pruebas = withContext(Dispatchers.IO) {
                    dbHelper.obtenerPruebasCompatibles(
                        equipo.idTipoEquipo
                    )
                }

                _uiState.update { estado ->
                    if (estado.equipoSeleccionado?.idEquipo ==
                        equipo.idEquipo
                    ) {
                        estado.copy(pruebasCompatibles = pruebas)
                    } else {
                        estado
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        mensajeError =
                            "No fue posible cargar las pruebas compatibles."
                    )
                }
            }
        }
    }

    // Paso 3: seleccionar la prueba que se va a realizar.
    fun seleccionarPrueba(prueba: TipoPrueba) {
        _uiState.update {
            it.copy(
                pruebaSeleccionada = prueba,
                mediciones = emptyMap(),
                mensajeError = null
            )
        }
    }

    // Paso 2: mantener los valores como texto mientras se escriben.
    fun actualizarTemperatura(valor: String) {
        _uiState.update {
            it.copy(temperaturaC = valor, mensajeError = null)
        }
    }

    fun actualizarHumedad(valor: String) {
        _uiState.update {
            it.copy(humedadPct = valor, mensajeError = null)
        }
    }

    // Paso 3: actualizar un campo de medición por su código.
    fun actualizarMedicion(codigo: String, valor: String) {
        _uiState.update {
            it.copy(
                mediciones = it.mediciones + (codigo to valor),
                mensajeError = null
            )
        }
    }


    // HU-04 / CA-4.1:
    // Crear y guardar una instalación asociada al cliente seleccionado.
    fun crearInstalacion(
        nombre: String,
        ubicacion: String?,
        idComuna: Long?,
        observacionesAcceso: String?
    ) {
        val estado = _uiState.value
        val cliente = estado.clienteSeleccionado

        if (cliente == null) {
            mostrarError("Primero debes seleccionar un cliente.")
            return
        }

        if (nombre.isBlank()) {
            mostrarError("Ingresa el nombre de la instalación.")
            return
        }

        if (idComuna == null || estado.comunas.none { it.idComuna == idComuna }) {
            mostrarError("Selecciona una comuna válida.")
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(cargando = true, mensajeError = null, mensajeExito = null)
            }

            try {
                val resultado = withContext(Dispatchers.IO) {
                    val idNuevo = dbHelper.insertarInstalacionProtocolo(
                        idUsuarioCliente = cliente.idUsuario,
                        nombre = nombre.trim(),
                        ubicacion = ubicacion,
                        idComuna = idComuna,
                        observacionesAcceso = observacionesAcceso
                    )

                    val instalacionesActualizadas =
                        dbHelper.obtenerInstalacionesPorCliente(cliente.idUsuario)

                    Pair(idNuevo, instalacionesActualizadas)
                }

                val idNuevo = resultado.first
                val instalacionesActualizadas = resultado.second

                if (idNuevo <= 0L) {
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            mensajeError =
                                "No fue posible guardar la instalación. Verifica los datos e inténtalo nuevamente."
                        )
                    }
                    return@launch
                }

                val instalacionNueva =
                    instalacionesActualizadas.find { it.idInstalacion == idNuevo }

                _uiState.update { actual ->
                    if (actual.clienteSeleccionado?.idUsuario != cliente.idUsuario) {
                        actual.copy(cargando = false)
                    } else {
                        actual.copy(
                            instalaciones = instalacionesActualizadas,
                            instalacionSeleccionada = instalacionNueva,
                            equipos = emptyList(),
                            equipoSeleccionado = null,
                            pruebasCompatibles = emptyList(),
                            pruebaSeleccionada = null,
                            cargando = false,
                            mensajeExito = "Instalación guardada correctamente."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        mensajeError = "Ocurrió un error al guardar la instalación."
                    )
                }
            }
        }
    }

    // HU-04 / CA-4.1 y CA-4.2:
    // Crear un equipo en la instalación seleccionada.
    // El TAG se normaliza a mayúsculas antes de guardarlo.
    fun crearEquipo(
        tag: String,
        idTipoEquipo: Long?,
        marca: String?,
        modelo: String?,
        numeroSerie: String?,
        potenciaKva: Double?,
        tensiones: Double?,
        corriente: Double?,
        maxContactosUohm: Double?
    ) {
        val estado = _uiState.value
        val instalacion = estado.instalacionSeleccionada

        if (instalacion == null) {
            mostrarError("Primero debes seleccionar una instalación.")
            return
        }

        val tagNormalizado = tag.trim().uppercase()

        if (tagNormalizado.isBlank()) {
            mostrarError("Ingresa el TAG del equipo.")
            return
        }

        if (
            idTipoEquipo == null ||
            estado.tiposEquipo.none { it.idTipoEquipo == idTipoEquipo }
        ) {
            mostrarError("Selecciona un tipo de equipo válido.")
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(cargando = true, mensajeError = null, mensajeExito = null)
            }

            try {
                val resultado = withContext(Dispatchers.IO) {
                    val idNuevo = dbHelper.insertarEquipoProtocolo(
                        idInstalacion = instalacion.idInstalacion,
                        tag = tagNormalizado,
                        idTipoEquipo = idTipoEquipo,
                        marca = marca,
                        modelo = modelo,
                        numeroSerie = numeroSerie,
                        potenciaKva = potenciaKva,
                        tensiones = tensiones,
                        corriente = corriente,
                        maxContactosUohm = maxContactosUohm
                    )

                    val equiposActualizados =
                        dbHelper.obtenerEquiposPorInstalacion(instalacion.idInstalacion)

                    Pair(idNuevo, equiposActualizados)
                }

                val idNuevo = resultado.first
                val equiposActualizados = resultado.second

                if (idNuevo <= 0L) {
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            mensajeError =
                                "No se pudo guardar el equipo. Comprueba si el TAG ya existe en esta instalación."
                        )
                    }
                    return@launch
                }

                val equipoNuevo =
                    equiposActualizados.find { it.idEquipo == idNuevo }

                _uiState.update { actual ->
                    if (
                        actual.instalacionSeleccionada?.idInstalacion !=
                        instalacion.idInstalacion
                    ) {
                        actual.copy(cargando = false)
                    } else {
                        actual.copy(
                            equipos = equiposActualizados,
                            cargando = false,
                            mensajeExito = "Equipo guardado correctamente."
                        )
                    }
                }

                // Seleccionarlo también carga las pruebas compatibles desde SQLite.
                if (
                    equipoNuevo != null &&
                    _uiState.value.instalacionSeleccionada?.idInstalacion ==
                    instalacion.idInstalacion
                ) {
                    seleccionarEquipo(equipoNuevo)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        mensajeError = "Ocurrió un error al guardar el equipo."
                    )
                }
            }
        }
    }



    // Avanzar únicamente si el paso actual es válido.
    fun avanzarPaso() {
        val estado = _uiState.value

        when (estado.pasoActual) {
            1 -> {
                if (estado.clienteSeleccionado == null ||
                    estado.instalacionSeleccionada == null ||
                    estado.equipoSeleccionado == null
                ) {
                    mostrarError(
                        "Selecciona un cliente, una instalación y un equipo."
                    )
                    return
                }
            }

            2 -> {
                val temperatura = estado.temperaturaC
                    .trim()
                    .replace(',', '.')
                    .toDoubleOrNull()

                val humedad = estado.humedadPct
                    .trim()
                    .replace(',', '.')
                    .toDoubleOrNull()

                if (temperatura == null ||
                    temperatura !in -10.0..60.0
                ) {
                    mostrarError(
                        "Ingresa una temperatura válida entre -10 y 60 °C."
                    )
                    return
                }

                if (humedad == null || humedad !in 0.0..100.0) {
                    mostrarError(
                        "Ingresa una humedad válida entre 0 y 100 %."
                    )
                    return
                }
            }

            else -> return
        }

        _uiState.update {
            it.copy(
                pasoActual = (it.pasoActual + 1).coerceAtMost(3),
                mensajeError = null
            )
        }
    }

    fun retrocederPaso() {
        _uiState.update {
            it.copy(
                pasoActual = (it.pasoActual - 1).coerceAtLeast(1),
                mensajeError = null
            )
        }
    }

    fun limpiarMensajes() {
        _uiState.update {
            it.copy(mensajeError = null, mensajeExito = null)
        }
    }

    private fun mostrarError(mensaje: String) {
        _uiState.update {
            it.copy(mensajeError = mensaje)
        }
    }

    override fun onCleared() {
        dbHelper.close()
        super.onCleared()
    }
}

*/