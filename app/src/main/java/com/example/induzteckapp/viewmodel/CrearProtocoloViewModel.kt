

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
                mensajeError = null,
                erroresCampos = emptyMap()
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
        _uiState.update { estado ->
            estado.copy(
                regionSeleccionada = region,
                comunaSeleccionada = null,
                mensajeError = limpiarMensajeValidacion(estado),
                erroresCampos = estado.erroresCampos - "regionSeleccionada" - "comunaSeleccionada"
            )
        }
    }

    fun seleccionarComuna(comuna: Comuna) {
        actualizarCampo("comunaSeleccionada") {
            it.copy(comunaSeleccionada = comuna)
        }
    }

    fun mostrarFormularioInstalacion(mostrar: Boolean) {
        _uiState.update { estado ->
            estado.copy(
                mostrarFormularioInstalacion = mostrar,
                mensajeError = null,
                erroresCampos = estado.erroresCampos - setOf(
                    "nombreInstalacion", "ubicacionInstalacion",
                    "regionSeleccionada", "comunaSeleccionada"
                )
            )
        }
    }

    fun actualizarNombreInstalacion(valor: String) =
        actualizarCampo("nombreInstalacion") { it.copy(nombreInstalacion = valor) }

    fun actualizarUbicacionInstalacion(valor: String) =
        actualizarCampo("ubicacionInstalacion") { it.copy(ubicacionInstalacion = valor) }

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

        val errores = buildMap {
            if (estado.nombreInstalacion.isBlank()) {
                put("nombreInstalacion", "Ingresa el nombre de la instalación.")
            }
            if (estado.ubicacionInstalacion.isBlank()) {
                put("ubicacionInstalacion", "Ingresa la ubicación o dirección de la instalación.")
            }
            if (estado.regionSeleccionada == null) {
                put("regionSeleccionada", "Selecciona una región.")
            }
            if (comuna == null) {
                put("comunaSeleccionada", "Selecciona una comuna.")
            } else if (estado.regionSeleccionada != null &&
                comuna.idRegion != estado.regionSeleccionada.idRegion
            ) {
                put("comunaSeleccionada", "La comuna no corresponde a la región seleccionada.")
            }
        }
        if (errores.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    mensajeError = "Revisa los campos marcados e inténtalo nuevamente.",
                    mensajeExito = null,
                    erroresCampos = errores
                )
            }
            return
        }
        val comunaNoNula = comuna ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, mensajeError = null, erroresCampos = emptyMap()) }

            try {
                val idNuevo = withContext(Dispatchers.IO) {
                    dbHelper.insertarInstalacionProtocolo(
                        cliente.idUsuario,
                        estado.nombreInstalacion.trim(),
                        estado.ubicacionInstalacion.trim(),
                        comunaNoNula.idComuna,
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
                mensajeError = null,
                erroresCampos = emptyMap()
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

    fun seleccionarTipoEquipo(tipo: TipoEquipo) {
        _uiState.update { estado ->
            estado.copy(
                tipoEquipoSeleccionado = tipo,
                maxContactosUohm = if (requiereResistenciaMaxima(tipo)) estado.maxContactosUohm else "",
                mensajeError = limpiarMensajeValidacion(estado),
                erroresCampos = estado.erroresCampos - setOf("tipoEquipoSeleccionado", "maxContactosUohm")
            )
        }
    }

    fun mostrarFormularioEquipo(mostrar: Boolean) {
        _uiState.update { estado ->
            estado.copy(
                mostrarFormularioEquipo = mostrar,
                mensajeError = null,
                erroresCampos = estado.erroresCampos - setOf(
                    "tipoEquipoSeleccionado", "tagEquipo", "potenciaKva",
                    "tensiones", "corriente", "maxContactosUohm"
                )
            )
        }
    }

    fun actualizarTagEquipo(valor: String) =
        actualizarCampo("tagEquipo") { it.copy(tagEquipo = valor) }

    fun actualizarMarcaEquipo(valor: String) =
        actualizar { it.copy(marcaEquipo = valor) }

    fun actualizarModeloEquipo(valor: String) =
        actualizar { it.copy(modeloEquipo = valor) }

    fun actualizarNumeroSerieEquipo(valor: String) =
        actualizar { it.copy(numeroSerieEquipo = valor) }

    fun actualizarPotenciaKva(valor: String) =
        actualizarCampo("potenciaKva") { it.copy(potenciaKva = valor) }

    fun actualizarTensiones(valor: String) =
        actualizarCampo("tensiones") { it.copy(tensiones = valor) }

    fun actualizarCorriente(valor: String) =
        actualizarCampo("corriente") { it.copy(corriente = valor) }

    fun actualizarMaxContactosUohm(valor: String) =
        actualizarCampo("maxContactosUohm") { it.copy(maxContactosUohm = valor) }

    fun crearEquipo() {
        val estado = _uiState.value
        val instalacion = estado.instalacionSeleccionada
        val tipo = estado.tipoEquipoSeleccionado

        if (instalacion == null) {
            mostrarError("Primero debes seleccionar una instalación.")
            return
        }

        val errores = mutableMapOf<String, String>()
        if (estado.tagEquipo.isBlank()) {
            errores["tagEquipo"] = "El TAG del equipo es obligatorio."
        }
        if (tipo == null) {
            errores["tipoEquipoSeleccionado"] = "Selecciona el tipo de equipo."
        }

        fun validarNumeroOpcional(valor: String, clave: String): Double? {
            if (valor.isBlank()) return null
            val numero = decimalValido(valor)
            if (numero == null) {
                errores[clave] = "Ingresa un número válido usando punto decimal."
                return null
            }
            if (numero < 0.0) {
                errores[clave] = "Este valor no puede ser negativo."
                return null
            }
            return numero
        }

        val potencia = validarNumeroOpcional(estado.potenciaKva, "potenciaKva")
        val tensiones = validarNumeroOpcional(estado.tensiones, "tensiones")
        val corriente = validarNumeroOpcional(estado.corriente, "corriente")
        val requiereMaxContactos = tipo != null && requiereResistenciaMaxima(tipo)
        val maxContactos = validarNumeroOpcional(
            estado.maxContactosUohm,
            "maxContactosUohm"
        )

        if (requiereMaxContactos && (maxContactos == null || maxContactos <= 0.0)) {
            errores["maxContactosUohm"] =
                "La resistencia máxima de placa es obligatoria y debe ser mayor que cero."
        }

        if (errores.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    mensajeError = "Revisa los campos marcados e inténtalo nuevamente.",
                    mensajeExito = null,
                    erroresCampos = errores
                )
            }
            return
        }

        val tipoNoNulo = tipo ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, mensajeError = null, erroresCampos = emptyMap()) }

            try {
                val idNuevo = withContext(Dispatchers.IO) {
                    dbHelper.insertarEquipoProtocolo(
                        instalacion.idInstalacion,
                        estado.tagEquipo.trim(),
                        tipoNoNulo.idTipoEquipo,
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
                    _uiState.update {
                        it.copy(
                            mensajeError = "No se pudo registrar el equipo. Comprueba el TAG e inténtalo nuevamente.",
                            mensajeExito = null,
                            erroresCampos = mapOf(
                                "tagEquipo" to "El TAG puede estar duplicado en esta instalación."
                            )
                        )
                    }
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
                        mensajeExito = "Equipo registrado correctamente.",
                        erroresCampos = emptyMap()
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

                if (e.message?.contains("UNIQUE", ignoreCase = true) == true) {
                    _uiState.update {
                        it.copy(
                            mensajeError = mensaje,
                            mensajeExito = null,
                            erroresCampos = mapOf("tagEquipo" to mensaje)
                        )
                    }
                } else {
                    mostrarError(mensaje)
                }
            } finally {
                _uiState.update { it.copy(guardando = false) }
            }
        }
    }

    private fun requiereResistenciaMaxima(tipo: TipoEquipo): Boolean {
        val texto = (tipo.nombre + " " + tipo.clasificacion).lowercase(Locale.ROOT)
        return "interruptor" in texto || "celda" in texto
    }

    fun seleccionarEquipo(equipo: Equipo) {
        _uiState.update {
            it.copy(
                equipoSeleccionado = equipo,
                pruebaSeleccionada = null,
                pruebasSeleccionadas = emptyList(),
                indicePruebaActual = 0,
                pruebasCompletadas = emptySet(),
                idProtocoloBorrador = null,
                pruebasCompatibles = emptyList(),
                parametrosPrueba = emptyList(),
                mediciones = emptyMap(),
                mensajeError = null,
                erroresCampos = emptyMap()
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


    fun alternarPrueba(prueba: TipoPrueba, seleccionada: Boolean) {
        if (_uiState.value.idProtocoloBorrador != null) return
        if (prueba.idTipoPrueba in _uiState.value.pruebasCompletadas) return
        _uiState.update { estado ->
            val nuevaLista = if (seleccionada) {
                if (estado.pruebasSeleccionadas.any { it.idTipoPrueba == prueba.idTipoPrueba }) estado.pruebasSeleccionadas
                else estado.pruebasSeleccionadas + prueba
            } else {
                estado.pruebasSeleccionadas.filterNot { it.idTipoPrueba == prueba.idTipoPrueba }
            }
            estado.copy(
                pruebasSeleccionadas = nuevaLista,
                pruebaSeleccionada = nuevaLista.firstOrNull(),
                indicePruebaActual = 0,
                parametrosPrueba = emptyList(),
                mediciones = emptyMap(),
                mensajeError = null,
                erroresCampos = emptyMap()
            )
        }
    }

    fun seleccionarPrueba(prueba: TipoPrueba) {
        alternarPrueba(
            prueba,
            !_uiState.value.pruebasSeleccionadas.any { it.idTipoPrueba == prueba.idTipoPrueba }
        )
    }

    fun actualizarTemperatura(valor: String) {
        actualizarCampo("temperaturaC") { it.copy(temperaturaC = valor) }
        persistirBorradorSiValido()
    }

    fun actualizarHumedad(valor: String) {
        actualizarCampo("humedadPct") { it.copy(humedadPct = valor) }
        persistirBorradorSiValido()
    }

    fun actualizarMedicion(codigo: String, valor: String) {
        actualizarCampo(codigo) {
            it.copy(mediciones = it.mediciones + (codigo to valor))
        }
    }

    fun actualizarUnidadAislacion(unidad: String) {
        if (unidad != "MΩ" && unidad != "GΩ") return
        _uiState.update { it.copy(unidadAislacion = unidad, mensajeError = null) }
    }

    fun actualizarElementoPrueba(valor: String) =
        actualizarCampo("elementoPrueba") { it.copy(elementoPrueba = valor) }

    fun actualizarReleOpero(valor: Boolean) =
        actualizar { it.copy(releOpero = valor) }

    fun actualizarResultadoManual(valor: Int) {
        if (valor !in 1..3) return
        actualizar { it.copy(resultadoManual = valor) }
    }

    private var creandoBorrador = false

    private fun persistirBorradorSiValido() {
        val estado = _uiState.value
        val temperatura = decimalValido(estado.temperaturaC) ?: return
        val humedad = decimalValido(estado.humedadPct) ?: return
        val equipo = estado.equipoSeleccionado ?: return
        if (temperatura !in -10.0..60.0 || humedad !in 0.0..100.0 || creandoBorrador || estado.idProtocoloBorrador != null) return
        val userId = preferencias.leerSesion()
        if (userId <= 0L) return
        creandoBorrador = true
        viewModelScope.launch {
            try {
                val fechaHora = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                val id = withContext(Dispatchers.IO) {
                    val nuevoId = dbHelper.guardarCabeceraBorrador(
                        null, equipo.idEquipo, userId, fechaHora, temperatura, humedad
                    )
                    dbHelper.guardarPruebasSeleccionadasBorrador(
                        nuevoId, estado.pruebasSeleccionadas.map { it.idTipoPrueba }
                    )
                    nuevoId
                }
                _uiState.update { it.copy(idProtocoloBorrador = id, mensajeExito = "Borrador guardado automáticamente.") }
            } catch (_: Exception) {
                _uiState.update { it.copy(mensajeError = "No fue posible guardar el borrador automáticamente.") }
            } finally {
                creandoBorrador = false
            }
        }
    }

    fun avanzarPaso() {
        val estado = _uiState.value
        when (estado.pasoActual) {
            1 -> {
                val errores = buildMap {
                    if (estado.clienteSeleccionado == null) {
                        put("clienteSeleccionado", "Selecciona un cliente.")
                    } else if (estado.instalacionSeleccionada == null) {
                        put("instalacionSeleccionada", "Selecciona una instalación.")
                    } else if (estado.equipoSeleccionado == null) {
                        put("equipoSeleccionado", "Selecciona un equipo.")
                    } else if (estado.pruebasSeleccionadas.isEmpty()) {
                        put("pruebasSeleccionadas", "Selecciona al menos una prueba.")
                    }
                }
                if (errores.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            mensajeError = "Completa los campos marcados antes de continuar.",
                            mensajeExito = null,
                            erroresCampos = errores
                        )
                    }
                    return
                }
                _uiState.update {
                    it.copy(
                        pasoActual = 2,
                        mensajeError = null,
                        erroresCampos = emptyMap()
                    )
                }
            }
            2 -> {
                if (creandoBorrador && estado.idProtocoloBorrador == null) {
                    mostrarError("El borrador se está guardando. Espera un momento y pulsa Continuar nuevamente.")
                    return
                }
                val temperatura = decimalValido(estado.temperaturaC)
                val humedad = decimalValido(estado.humedadPct)
                val errores = buildMap {
                    if (temperatura == null || temperatura !in -10.0..60.0) {
                        put("temperaturaC", "Ingresa una temperatura entre -10 y 60 °C.")
                    }
                    if (humedad == null || humedad !in 0.0..100.0) {
                        put("humedadPct", "Ingresa una humedad entre 0 y 100 %.")
                    }
                }
                if (errores.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            mensajeError = "Revisa las condiciones ambientales marcadas.",
                            mensajeExito = null,
                            erroresCampos = errores
                        )
                    }
                    return
                }
                val temperaturaValida = temperatura ?: return
                val humedadValida = humedad ?: return
                val userId = preferencias.leerSesion()
                val equipo = estado.equipoSeleccionado
                if (equipo == null || userId <= 0L) {
                    mostrarError("No se encontró el equipo o una sesión válida.")
                    return
                }
                viewModelScope.launch {
                    _uiState.update { it.copy(guardando = true, mensajeError = null) }
                    try {
                        val fechaHora = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                        val idBorrador = withContext(Dispatchers.IO) {
                            val id = dbHelper.guardarCabeceraBorrador(
                                estado.idProtocoloBorrador, equipo.idEquipo, userId,
                                fechaHora, temperaturaValida, humedadValida
                            )
                            dbHelper.guardarPruebasSeleccionadasBorrador(
                                id, estado.pruebasSeleccionadas.map { it.idTipoPrueba }
                            )
                            id
                        }
                        val primera = estado.pruebasSeleccionadas.first()
                        val parametros = withContext(Dispatchers.IO) {
                            parametrosParaEquipo(
                                primera,
                                equipo,
                                dbHelper.obtenerParametrosPorTipoPrueba(primera.idTipoPrueba)
                            )
                        }
                        _uiState.update {
                            it.copy(
                                idProtocoloBorrador = idBorrador,
                                pasoActual = 3,
                                indicePruebaActual = 0,
                                pruebaSeleccionada = primera,
                                parametrosPrueba = parametros,
                                mediciones = emptyMap(),
                                erroresCampos = emptyMap(),
                                guardando = false,
                                mensajeExito = "Borrador guardado. Completa las pruebas seleccionadas.",
                                mensajeError = null
                            )
                        }
                    } catch (_: Exception) {
                        _uiState.update { it.copy(guardando = false, mensajeError = "No fue posible guardar el borrador.") }
                    }
                }
            }
        }
    }

    fun retrocederPaso() {
        _uiState.update {
            it.copy(pasoActual = (it.pasoActual - 1).coerceAtLeast(1), mensajeError = null)
        }
    }

    fun guardarProtocolo() {
        val estado = _uiState.value
        if (estado.pasoActual != 3) return
        val idBorrador = estado.idProtocoloBorrador
        val prueba = estado.pruebaSeleccionada
        val equipo = estado.equipoSeleccionado
        if (idBorrador == null || prueba == null || equipo == null) {
            mostrarError("No se encontró el borrador o la prueba actual.")
            return
        }
        if (estado.pruebasCompletadas.contains(prueba.idTipoPrueba)) {
            mostrarError("Esta prueba ya fue guardada. Continúa con la siguiente o vuelve a la lista de protocolos.")
            return
        }
        if (estado.parametrosPrueba.isEmpty()) {
            mostrarError("La prueba no tiene parámetros configurados.")
            return
        }

        val valores = mutableMapOf<String, Double>()
        val erroresMediciones = mutableMapOf<String, String>()
        for (p in estado.parametrosPrueba) {
            val raw = estado.mediciones[p.codigo].orEmpty()
            val n = decimalValido(raw)
            if (n == null) {
                erroresMediciones[p.codigo] =
                    "Completa '${p.nombre}' con un número válido usando punto decimal."
                continue
            }
            if (p.codigo == "POS_TAP" && n % 1.0 != 0.0) {
                erroresMediciones[p.codigo] =
                    "La posición TAP debe ser un entero; admite valores negativos, cero o positivos."
                continue
            }
            if (p.codigo == "TIEMPO_ESPERADO" || p.codigo == "TIEMPO_MEDIDO") {
                if (n <= 0.0) {
                    erroresMediciones[p.codigo] = "El tiempo debe ser mayor que cero."
                    continue
                }
            } else if (p.codigo != "POS_TAP" && n <= 0.0) {
                erroresMediciones[p.codigo] = "'${p.nombre}' debe ser mayor que cero."
                continue
            }
            valores[p.codigo] = if (
                estado.unidadAislacion == "GΩ" &&
                (p.codigo == "LEC_1MIN" || p.codigo == "LEC_10MIN")
            ) n * 1000.0 else n
        }
        if (erroresMediciones.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    mensajeError = "Revisa las mediciones marcadas antes de guardar la prueba.",
                    mensajeExito = null,
                    erroresCampos = erroresMediciones
                )
            }
            return
        }

        val elementosRequeridos = elementosRequeridos(prueba, equipo)
        val elementoActual = estado.elementoPrueba.trim()
        if (elementosRequeridos.isNotEmpty()) {
            val errorElemento = when {
                elementoActual.isBlank() ->
                    "Selecciona el elemento: ${elementosRequeridos.joinToString(", ")}."
                elementosRequeridos.none { it.equals(elementoActual, ignoreCase = true) } ->
                    "Selecciona uno de estos elementos: ${elementosRequeridos.joinToString(", ")}."
                "${prueba.idTipoPrueba}|${elementoActual.lowercase(Locale.ROOT)}" in estado.elementosCompletados ->
                    "Ese elemento ya fue guardado. Selecciona uno pendiente."
                else -> null
            }
            if (errorElemento != null) {
                _uiState.update {
                    it.copy(
                        mensajeError = errorElemento,
                        mensajeExito = null,
                        erroresCampos = mapOf("elementoPrueba" to errorElemento)
                    )
                }
                return
            }
        }
        if (prueba.nombre.contains("contacto", ignoreCase = true) &&
            (equipo.maxContactosUohm == null || equipo.maxContactosUohm <= 0.0)
        ) {
            mostrarError("Para evaluar resistencia de contactos, registra primero la resistencia máxima indicada en la placa del equipo.")
            return
        }

        val resultado = evaluarResultado(estado, valores, equipo.maxContactosUohm)
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, mensajeError = null, mensajeExito = null, erroresCampos = emptyMap()) }
            try {
                withContext(Dispatchers.IO) {
                    dbHelper.guardarPruebaEvaluada(
                        idBorrador,
                        prueba.idTipoPrueba,
                        estado.elementoPrueba.trim().ifBlank { null },
                        valores,
                        resultado
                    )
                }
                val requeridos = elementosRequeridos(prueba, equipo)
                val elementosActualizados = if (requeridos.isNotEmpty()) {
                    estado.elementosCompletados + "${prueba.idTipoPrueba}|${estado.elementoPrueba.trim().lowercase(Locale.ROOT)}"
                } else estado.elementosCompletados
                val elementosListos = requeridos.isEmpty() || requeridos.all { elemento ->
                    "${prueba.idTipoPrueba}|${elemento.lowercase(Locale.ROOT)}" in elementosActualizados
                }

                if (!elementosListos) {
                    val guardados = requeridos.count { elemento ->
                        "${prueba.idTipoPrueba}|${elemento.lowercase(Locale.ROOT)}" in elementosActualizados
                    }
                    _uiState.update {
                        it.copy(
                            elementosCompletados = elementosActualizados,
                            mediciones = emptyMap(),
                            elementoPrueba = "",
                            guardando = false,
                            erroresCampos = emptyMap(),
                            mensajeExito = "Elemento guardado. Resultado: ${nombreResultado(resultado)}. Faltan ${requeridos.size - guardados} elementos para completar esta prueba."
                        )
                    }
                    return@launch
                }

                val completadas = estado.pruebasCompletadas + prueba.idTipoPrueba
                val siguienteIndice = estado.indicePruebaActual + 1
                if (siguienteIndice < estado.pruebasSeleccionadas.size) {
                    val siguiente = estado.pruebasSeleccionadas[siguienteIndice]
                    val parametros = withContext(Dispatchers.IO) {
                        parametrosParaEquipo(
                            siguiente,
                            equipo,
                            dbHelper.obtenerParametrosPorTipoPrueba(siguiente.idTipoPrueba)
                        )
                    }
                    _uiState.update {
                        it.copy(
                            pruebasCompletadas = completadas,
                            elementosCompletados = elementosActualizados,
                            indicePruebaActual = siguienteIndice,
                            pruebaSeleccionada = siguiente,
                            parametrosPrueba = parametros,
                            mediciones = emptyMap(),
                            elementoPrueba = "",
                            guardando = false,
                            erroresCampos = emptyMap(),
                            mensajeExito = "Prueba guardada. Resultado: ${nombreResultado(resultado)}. Continúa con ${siguiente.nombre}."
                        )
                    }
                } else {
                    val resumen = withContext(Dispatchers.IO) {
                        dbHelper.finalizarBorradorProtocolo(idBorrador)
                        dbHelper.obtenerResultadoGlobalProtocolo(idBorrador) to
                                dbHelper.obtenerResumenPruebasProtocolo(idBorrador)
                    }
                    val resumenGlobal = resumen.first?.let { nombreResultado(it) } ?: "Sin resultado"
                    _uiState.update {
                        it.copy(
                            pruebasCompletadas = completadas,
                            elementosCompletados = elementosActualizados,
                            resumenResultados = resumen.second,
                            guardando = false,
                            erroresCampos = emptyMap(),
                            protocoloFinalizado = true,
                            mensajeExito = "Todas las pruebas fueron guardadas. Protocolo registrado en terreno. Resultado global: $resumenGlobal.",
                            mensajeError = null
                        )
                    }
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        mensajeError = "No fue posible guardar esta prueba. El borrador se conserva para volver a intentarlo."
                    )
                }
            }
        }
    }

    private fun parametrosParaEquipo(
        prueba: TipoPrueba,
        equipo: Equipo,
        parametros: List<com.example.induzteckapp.model.ParametroPrueba>
    ): List<com.example.induzteckapp.model.ParametroPrueba> {
        val nombrePrueba = prueba.nombre.lowercase(Locale.ROOT)
        val tipoEquipo = (equipo.nombreTipoEquipo + " " + equipo.clasificacionTipo)
            .lowercase(Locale.ROOT)
        val esAislacion = "aislación" in nombrePrueba || "aislacion" in nombrePrueba
        val esInterruptorOCelda = "interruptor" in tipoEquipo || "celda" in tipoEquipo

        
        
        return if (esAislacion && esInterruptorOCelda) {
            parametros.filterNot { it.codigo == "LEC_10MIN" }
        } else {
            parametros
        }
    }

    private fun elementosRequeridos(prueba: TipoPrueba, equipo: Equipo): List<String> {
        val nombrePrueba = prueba.nombre.lowercase(Locale.ROOT)
        val tipoEquipo = (equipo.nombreTipoEquipo + " " + equipo.clasificacionTipo).lowercase(Locale.ROOT)
        val aislacion = "aislación" in nombrePrueba || "aislacion" in nombrePrueba
        return when {
            aislacion && "transformador" in tipoEquipo -> listOf("AT-BT", "AT-T", "BT-T")
            aislacion && "cable" in tipoEquipo -> listOf("Fase A", "Fase B", "Fase C")
            "contacto" in nombrePrueba -> listOf("Polo 1", "Polo 2", "Polo 3")
            else -> emptyList()
        }
    }

    private fun evaluarResultado(
        estado: CrearProtocoloUiState,
        m: Map<String, Double>,
        maxContactos: Double?
    ): Int {
        val nombre = estado.pruebaSeleccionada?.nombre.orEmpty().lowercase(Locale.ROOT)
        if ("aislación" in nombre || "aislacion" in nombre) {
            val uno = m["LEC_1MIN"] ?: return InduztekDbHelper.RESULTADO_RECHAZADO
            val diez = m["LEC_10MIN"]
            if (diez == null) return estado.resultadoManual
            if (uno < 100.0) return InduztekDbHelper.RESULTADO_RECHAZADO
            val ip = diez / uno
            return when {
                ip >= 2.0 -> InduztekDbHelper.RESULTADO_APROBADO
                ip >= 1.0 -> InduztekDbHelper.RESULTADO_OBSERVADO
                else -> InduztekDbHelper.RESULTADO_RECHAZADO
            }
        }
        if ("ttr" in nombre || "relación de transformación" in nombre || "relacion de transformacion" in nombre) {
            val teorica = m["REL_TEORICA"] ?: return InduztekDbHelper.RESULTADO_RECHAZADO
            val medida = m["REL_MEDIDA"] ?: return InduztekDbHelper.RESULTADO_RECHAZADO
            val error = kotlin.math.abs(medida - teorica) / teorica * 100.0
            return when {
                error <= 0.5 -> InduztekDbHelper.RESULTADO_APROBADO
                error <= 1.0 -> InduztekDbHelper.RESULTADO_OBSERVADO
                else -> InduztekDbHelper.RESULTADO_RECHAZADO
            }
        }
        if ("contacto" in nombre) {
            val medida = m["RES_CONTACTO"] ?: return InduztekDbHelper.RESULTADO_RECHAZADO
            if (maxContactos == null || maxContactos <= 0.0) {
                return InduztekDbHelper.RESULTADO_RECHAZADO
            }
            val porcentaje = medida / maxContactos * 100.0
            return when {
                porcentaje <= 100.0 -> InduztekDbHelper.RESULTADO_APROBADO
                porcentaje <= 120.0 -> InduztekDbHelper.RESULTADO_OBSERVADO
                else -> InduztekDbHelper.RESULTADO_RECHAZADO
            }
        }
        if ("relé" in nombre || "rele" in nombre) {
            if (!estado.releOpero) return InduztekDbHelper.RESULTADO_RECHAZADO
            val esperado = m["TIEMPO_ESPERADO"] ?: return InduztekDbHelper.RESULTADO_RECHAZADO
            val medido = m["TIEMPO_MEDIDO"] ?: return InduztekDbHelper.RESULTADO_RECHAZADO
            val diferencia = kotlin.math.abs(medido - esperado) / esperado * 100.0
            return when {
                diferencia <= 5.0 -> InduztekDbHelper.RESULTADO_APROBADO
                diferencia <= 10.0 -> InduztekDbHelper.RESULTADO_OBSERVADO
                else -> InduztekDbHelper.RESULTADO_RECHAZADO
            }
        }
        if ("devanado" in nombre) return estado.resultadoManual
        return estado.resultadoManual
    }

    private fun nombreResultado(id: Int): String = when (id) {
        InduztekDbHelper.RESULTADO_APROBADO -> "Aprobado"
        InduztekDbHelper.RESULTADO_OBSERVADO -> "Observado"
        else -> "Rechazado"
    }

    fun cargarBorrador(idProtocolo: Long) {
        val userId = preferencias.leerSesion()
        if (userId <= 0L) {
            mostrarError("Inicia sesión nuevamente para recuperar el borrador.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, mensajeError = null) }
            try {
                val cargado = withContext(Dispatchers.IO) {
                    val detalle = dbHelper.obtenerDetalleBorrador(idProtocolo, userId)
                        ?: throw IllegalArgumentException("No se encontró el borrador.")
                    val clientes = dbHelper.obtenerClientesProtocolo()
                    val cliente = clientes.firstOrNull { it.idUsuario == detalle.idUsuarioCliente }
                        ?: throw IllegalStateException("El cliente del borrador ya no está disponible.")
                    val instalaciones = dbHelper.obtenerInstalacionesPorCliente(cliente.idUsuario)
                    val instalacion = instalaciones.firstOrNull { it.idInstalacion == detalle.idInstalacion }
                        ?: throw IllegalStateException("La instalación del borrador ya no está disponible.")
                    val equipos = dbHelper.obtenerEquiposPorInstalacion(instalacion.idInstalacion)
                    val equipo = equipos.firstOrNull { it.idEquipo == detalle.idEquipo }
                        ?: throw IllegalStateException("El equipo del borrador ya no está disponible.")
                    val compatibles = dbHelper.obtenerPruebasCompatibles(equipo.idTipoEquipo)
                    val completadas = dbHelper.obtenerTiposPruebaCompletados(idProtocolo)
                    val seleccionadas = dbHelper.obtenerTiposPruebaSeleccionadosBorrador(idProtocolo)
                    Triple(
                        detalle,
                        Triple(cliente, instalaciones, instalacion),
                        Triple(equipos, equipo, Triple(compatibles, completadas, seleccionadas))
                    )
                }
                val detalle = cargado.first
                val cliente = cargado.second.first
                val instalaciones = cargado.second.second
                val instalacion = cargado.second.third
                val equipos = cargado.third.first
                val equipo = cargado.third.second
                val compatibles = cargado.third.third.first
                val completadas = cargado.third.third.second
                val tiposSeleccionados = cargado.third.third.third
                val elementosCargados = withContext(Dispatchers.IO) {
                    compatibles.flatMap { prueba ->
                        dbHelper.obtenerElementosPruebaCompletados(idProtocolo, prueba.idTipoPrueba)
                            .map { "${prueba.idTipoPrueba}|$it" }
                    }.toSet()
                }
                val completadasReales = compatibles.filter { prueba ->
                    val requeridos = elementosRequeridos(prueba, equipo)
                    if (requeridos.isEmpty()) {
                        prueba.idTipoPrueba in completadas
                    } else {
                        requeridos.all { "${prueba.idTipoPrueba}|${it.lowercase(Locale.ROOT)}" in elementosCargados }
                    }
                }.map { it.idTipoPrueba }.toSet()
                val finalizarAlRecuperar = tiposSeleccionados.isNotEmpty() &&
                        tiposSeleccionados.all { it in completadasReales }
                val resultadoRecuperado = if (finalizarAlRecuperar) {
                    withContext(Dispatchers.IO) {
                        dbHelper.finalizarBorradorProtocolo(idProtocolo)
                        dbHelper.obtenerResultadoGlobalProtocolo(idProtocolo) to
                                dbHelper.obtenerResumenPruebasProtocolo(idProtocolo)
                    }
                } else null
                val clientesActuales = withContext(Dispatchers.IO) { dbHelper.obtenerClientesProtocolo() }
                val regiones = withContext(Dispatchers.IO) { dbHelper.obtenerRegiones() }
                val comunas = withContext(Dispatchers.IO) { dbHelper.obtenerComunas() }
                val tipos = withContext(Dispatchers.IO) { dbHelper.obtenerTiposEquipo() }
                _uiState.update {
                    it.copy(
                        pasoActual = if (finalizarAlRecuperar) 3 else 1,
                        clientes = clientesActuales,
                        regiones = regiones,
                        comunas = comunas,
                        tiposEquipo = tipos,
                        clienteSeleccionado = cliente,
                        instalaciones = instalaciones,
                        instalacionSeleccionada = instalacion,
                        equipos = equipos,
                        equipoSeleccionado = equipo,
                        pruebasCompatibles = compatibles,
                        pruebasSeleccionadas = compatibles.filter {
                            it.idTipoPrueba in tiposSeleccionados && it.idTipoPrueba !in completadasReales
                        },
                        pruebaSeleccionada = null,
                        indicePruebaActual = 0,
                        pruebasCompletadas = completadasReales,
                        elementosCompletados = elementosCargados,
                        idProtocoloBorrador = detalle.idProtocolo,
                        protocoloFinalizado = finalizarAlRecuperar,
                        resumenResultados = resultadoRecuperado?.second.orEmpty(),
                        temperaturaC = detalle.temperaturaC.toString(),
                        humedadPct = detalle.humedadPct.toString(),
                        parametrosPrueba = emptyList(),
                        mediciones = emptyMap(),
                        cargando = false,
                        mensajeExito = if (finalizarAlRecuperar) {
                            "El borrador ya tenía todas las pruebas completas. Se confirmó su estado REGISTRADO_TERRENO. Resultado global: ${resultadoRecuperado?.first?.let { nombreResultado(it) } ?: "Sin resultado"}."
                        } else {
                            "Borrador recuperado. Las pruebas ya guardadas se conservan; continúa con las pendientes."
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, mensajeError = e.message ?: "No fue posible recuperar el borrador.") }
            }
        }
    }

    fun cargarEdicion(idProtocolo: Int) {
        val userId = preferencias.leerSesion()
        if (userId <= 0L) {
            mostrarError("Inicia sesión nuevamente para editar el protocolo.")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(modoEdicion = true, cargando = true, mensajeError = null) }
            try {
                withContext(Dispatchers.IO) {
                    if (!dbHelper.obtenerDetalleProtocoloParaEdicion(idProtocolo.toLong(), userId)) {
                        throw IllegalArgumentException("No se encontró el protocolo o no tienes permisos para editarlo.")
                    }
                    dbHelper.prepararProtocoloParaEdicion(idProtocolo.toLong(), userId)
                }
                cargarBorrador(idProtocolo.toLong())
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, mensajeError = e.message ?: "No fue posible preparar la edición.") }
            }
        }
    }

    fun cargarBorradores() {
        val userId = preferencias.leerSesion()
        if (userId <= 0L) return
        viewModelScope.launch {
            try {
                val lista = withContext(Dispatchers.IO) { dbHelper.obtenerBorradoresProtocolo(userId) }
                _uiState.update { it.copy(borradores = lista) }
            } catch (_: Exception) {
                mostrarError("No fue posible consultar los borradores.")
            }
        }
    }

    fun limpiarMensajes() {
        _uiState.update {
            it.copy(mensajeError = null, mensajeExito = null, erroresCampos = emptyMap())
        }
    }

    private fun actualizarCampo(
        clave: String,
        cambio: (CrearProtocoloUiState) -> CrearProtocoloUiState
    ) {
        _uiState.update { estado ->
            val erroresRestantes = estado.erroresCampos - clave
            cambio(estado).copy(
                erroresCampos = erroresRestantes,
                mensajeError = if (erroresRestantes.isEmpty()) null else estado.mensajeError,
                mensajeExito = null
            )
        }
    }

    private fun limpiarMensajeValidacion(estado: CrearProtocoloUiState): String? =
        if (estado.mensajeError == "Revisa los campos marcados e inténtalo nuevamente.") {
            null
        } else {
            estado.mensajeError
        }

    private fun actualizar(cambio: (CrearProtocoloUiState) -> CrearProtocoloUiState) {
        _uiState.update { cambio(it).copy(mensajeError = null, mensajeExito = null) }
    }

    private fun mostrarError(mensaje: String) {
        _uiState.update {
            it.copy(mensajeError = mensaje, mensajeExito = null, erroresCampos = emptyMap())
        }
    }

    private fun decimalValido(texto: String): Double? {
        val limpio = texto.trim()
        if (!limpio.matches(Regex("-?\\d+(\\.\\d+)?"))) return null
        return limpio.toDoubleOrNull()?.takeIf { it.isFinite() }
    }

    fun guardarYMarcarObservadoOnExit(onCompletado: () -> Unit) {
        val estado = _uiState.value
        if (estado.protocoloFinalizado) {
            onCompletado()
            return
        }
        val idBorrador = estado.idProtocoloBorrador
        val userId = preferencias.leerSesion()

        if (idBorrador != null && userId > 0L) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    dbHelper.guardarYMarcarObservado(idBorrador, userId)
                } catch (_: Exception) {}
                withContext(Dispatchers.Main) {
                    onCompletado()
                }
            }
        } else {
            val equipo = estado.equipoSeleccionado
            val temp = decimalValido(estado.temperaturaC) ?: 20.0
            val hum = decimalValido(estado.humedadPct) ?: 45.0
            if (equipo != null && userId > 0L) {
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        val fechaHora = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                        val nuevoId = dbHelper.guardarCabeceraBorrador(
                            null, equipo.idEquipo, userId, fechaHora, temp, hum
                        )
                        if (estado.pruebasSeleccionadas.isNotEmpty()) {
                            dbHelper.guardarPruebasSeleccionadasBorrador(
                                nuevoId, estado.pruebasSeleccionadas.map { it.idTipoPrueba }
                            )
                        }
                        dbHelper.guardarYMarcarObservado(nuevoId, userId)
                    } catch (_: Exception) {}
                    withContext(Dispatchers.Main) {
                        onCompletado()
                    }
                }
            } else {
                onCompletado()
            }
        }
    }

    override fun onCleared() {
        dbHelper.close()
        super.onCleared()
    }
}
