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
            it.copy(
                tipoEquipoSeleccionado = tipo,
                maxContactosUohm = if (requiereResistenciaMaxima(tipo)) it.maxContactosUohm else "",
                mensajeError = null
            )
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

        val requiereMaxContactos = requiereResistenciaMaxima(tipo)
        val maxContactos = convertirNumeroOpcional(
            estado.maxContactosUohm,
            "resistencia máxima de contactos"
        ) ?: run {
            if (estado.maxContactosUohm.isNotBlank()) return
            null
        }

        if (requiereMaxContactos && (maxContactos == null || maxContactos <= 0.0)) {
            mostrarError("Para un interruptor o una celda de MT, la resistencia máxima de contactos indicada en la placa es obligatoria y debe ser mayor que cero.")
            return
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
                mensajeError = null
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
        actualizar { it.copy(temperaturaC = valor) }
        persistirBorradorSiValido()
    }

    fun actualizarHumedad(valor: String) {
        actualizar { it.copy(humedadPct = valor) }
        persistirBorradorSiValido()
    }

    fun actualizarMedicion(codigo: String, valor: String) {
        _uiState.update {
            it.copy(mediciones = it.mediciones + (codigo to valor), mensajeError = null)
        }
    }

    fun actualizarUnidadAislacion(unidad: String) {
        if (unidad != "MΩ" && unidad != "GΩ") return
        _uiState.update { it.copy(unidadAislacion = unidad, mensajeError = null) }
    }

    fun actualizarElementoPrueba(valor: String) =
        actualizar { it.copy(elementoPrueba = valor) }

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
                if (estado.clienteSeleccionado == null ||
                    estado.instalacionSeleccionada == null ||
                    estado.equipoSeleccionado == null ||
                    estado.pruebasSeleccionadas.isEmpty()
                ) {
                    mostrarError("Selecciona cliente, instalación, equipo y al menos una prueba.")
                    return
                }
                _uiState.update { it.copy(pasoActual = 2, mensajeError = null) }
            }
            2 -> {
                if (creandoBorrador && estado.idProtocoloBorrador == null) {
                    mostrarError("El borrador se está guardando. Espera un momento y pulsa Continuar nuevamente.")
                    return
                }
                val temperatura = decimalValido(estado.temperaturaC)
                val humedad = decimalValido(estado.humedadPct)
                if (temperatura == null || temperatura !in -10.0..60.0) {
                    mostrarError("La temperatura debe ser un número entre -10 y 60 °C.")
                    return
                }
                if (humedad == null || humedad !in 0.0..100.0) {
                    mostrarError("La humedad debe estar entre 0 y 100 %.")
                    return
                }
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
                                fechaHora, temperatura, humedad
                            )
                            dbHelper.guardarPruebasSeleccionadasBorrador(
                                id, estado.pruebasSeleccionadas.map { it.idTipoPrueba }
                            )
                            id
                        }
                        val primera = estado.pruebasSeleccionadas.first()
                        val parametros = withContext(Dispatchers.IO) {
                            dbHelper.obtenerParametrosPorTipoPrueba(primera.idTipoPrueba)
                        }
                        _uiState.update {
                            it.copy(
                                idProtocoloBorrador = idBorrador,
                                pasoActual = 3,
                                indicePruebaActual = 0,
                                pruebaSeleccionada = primera,
                                parametrosPrueba = parametros,
                                mediciones = emptyMap(),
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
        for (p in estado.parametrosPrueba) {
            val raw = estado.mediciones[p.codigo].orEmpty()
            val n = decimalValido(raw)
            if (n == null) {
                mostrarError("Completa '${p.nombre}' con un valor numérico válido.")
                return
            }
            if (p.codigo == "POS_TAP" && (n < 1 || n % 1.0 != 0.0)) {
                mostrarError("La posición TAP debe ser un entero mayor o igual a 1.")
                return
            }
            if ((p.codigo == "TIEMPO_ESPERADO" || p.codigo == "TIEMPO_MEDIDO") && n <= 0) {
                mostrarError("Los tiempos deben ser mayores que cero.")
                return
            }
            if (p.codigo != "TIEMPO_ESPERADO" && p.codigo != "TIEMPO_MEDIDO" && n <= 0) {
                mostrarError("'${p.nombre}' debe ser mayor que cero.")
                return
            }
            valores[p.codigo] = if (
                estado.unidadAislacion == "GΩ" &&
                (p.codigo == "LEC_1MIN" || p.codigo == "LEC_10MIN")
            ) n * 1000.0 else n
        }

        val elementosRequeridos = elementosRequeridos(prueba, equipo)
        val elementoActual = estado.elementoPrueba.trim()
        if (elementosRequeridos.isNotEmpty()) {
            if (elementoActual.isBlank()) {
                mostrarError("Indica el elemento: ${elementosRequeridos.joinToString(", ")}.")
                return
            }
            if (elementosRequeridos.none { it.equals(elementoActual, ignoreCase = true) }) {
                mostrarError("Para esta prueba utiliza uno de estos elementos: ${elementosRequeridos.joinToString(", ")}.")
                return
            }
            val claveElemento = "${prueba.idTipoPrueba}|${elementoActual.lowercase(Locale.ROOT)}"
            if (claveElemento in estado.elementosCompletados) {
                mostrarError("Ese elemento ya fue guardado. Ingresa uno que aún esté pendiente.")
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
            _uiState.update { it.copy(guardando = true, mensajeError = null, mensajeExito = null) }
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
                        dbHelper.obtenerParametrosPorTipoPrueba(siguiente.idTipoPrueba)
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
        _uiState.update { it.copy(mensajeError = null, mensajeExito = null) }
    }

    private fun actualizar(cambio: (CrearProtocoloUiState) -> CrearProtocoloUiState) {
        _uiState.update { cambio(it).copy(mensajeError = null, mensajeExito = null) }
    }

    private fun mostrarError(mensaje: String) {
        _uiState.update { it.copy(mensajeError = mensaje, mensajeExito = null) }
    }

    private fun decimalValido(texto: String): Double? {
        val limpio = texto.trim()
        if (!limpio.matches(Regex("-?\\d+(\\.\\d+)?"))) return null
        return limpio.toDoubleOrNull()?.takeIf { it.isFinite() }
    }

    private fun convertirNumeroOpcional(texto: String, nombreCampo: String): Double? {
        if (texto.isBlank()) return null
        val numero = decimalValido(texto)
        if (numero == null) mostrarError("El campo '$nombreCampo' debe ser numérico y usar punto decimal.")
        return numero
    }

    override fun onCleared() {
        dbHelper.close()
        super.onCleared()
    }
}


/*
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

*/