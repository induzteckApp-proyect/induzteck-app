// CrearProtocoloViewModel
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