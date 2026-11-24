package com.example.induzteckapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.induzteckapp.data.ActivosRepository
import com.example.induzteckapp.data.InduztekPreferencias
import com.example.induzteckapp.data.ProtocolosRepository
import com.example.induzteckapp.model.EquipoConsulta
import com.example.induzteckapp.model.InstalacionConsulta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListaInstalacionesViewModel(application: Application) : AndroidViewModel(application) {
    private val activosRepository = ActivosRepository(application)
    private val protocolosRepository = ProtocolosRepository(application)
    private val preferencias = InduztekPreferencias(application)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _instalacionesState = MutableStateFlow<List<InstalacionConsulta>>(emptyList())
    val instalacionesState: StateFlow<List<InstalacionConsulta>> = _instalacionesState.asStateFlow()
    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()
    private var idClienteLogueado: Long = -1L

    init { idClienteLogueado = preferencias.leerSesion(); cargarDatos() }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        cargarDatos(newQuery)
    }

    private fun cargarDatos(filtro: String = "") {
        viewModelScope.launch {
            try {
                if (idClienteLogueado <= 0L) {
                    _instalacionesState.value = emptyList()
                    return@launch
                }
                val instalaciones = withContext(Dispatchers.IO) {
                    activosRepository.obtenerInstalacionesPorCliente(idClienteLogueado)
                }
                val lista = mutableListOf<InstalacionConsulta>()
                for (instalacion in filtrarInstalaciones(instalaciones, filtro)) {
                    val equipos = withContext(Dispatchers.IO) {
                        activosRepository.obtenerEquiposPorInstalacion(instalacion.idInstalacion)
                    }
                    lista += InstalacionConsulta(
                        idInstalacion = instalacion.idInstalacion.toInt(),
                        nombre = instalacion.nombre,
                        equipos = equipos.map { equipo ->
                            EquipoConsulta(
                                idEquipo = equipo.idEquipo.toInt(), tag = equipo.tag,
                                tipoEquipo = equipo.nombreTipoEquipo, instalacionNombre = instalacion.nombre
                            )
                        }
                    )
                }
                _instalacionesState.value = lista
            } catch (e: Exception) {
                _instalacionesState.value = emptyList()
                _mensajeError.value = e.message ?: "No fue posible cargar las instalaciones."
            }
        }
    }

    fun limpiarMensajeError() {
        _mensajeError.value = null
    }

    fun toggleInstalacionExpansion(idInstalacion: Int) {
        _instalacionesState.value = _instalacionesState.value.map { instalacion ->
            if (instalacion.idInstalacion == idInstalacion) instalacion.copy(isExpanded = !instalacion.isExpanded) else instalacion
        }
    }

    fun toggleEquipoExpansion(idInstalacion: Int, idEquipo: Int) {
        viewModelScope.launch {
            try {
                val historial = withContext(Dispatchers.IO) { protocolosRepository.obtenerHistorialEquipoCliente(idEquipo) }
                _instalacionesState.value = _instalacionesState.value.map { instalacion ->
                    if (instalacion.idInstalacion == idInstalacion) {
                        instalacion.copy(equipos = instalacion.equipos.map { equipo ->
                            if (equipo.idEquipo == idEquipo) equipo.copy(
                                isExpanded = !equipo.isExpanded, historialProtocolos = historial
                            ) else equipo
                        })
                    } else instalacion
                }
            } catch (_: Exception) { }
        }
    }

    companion object {
        fun filtrarInstalaciones(instalaciones: List<com.example.induzteckapp.model.Instalacion>, filtro: String): List<com.example.induzteckapp.model.Instalacion> {
            val consulta = filtro.trim()
            if (consulta.isBlank()) return instalaciones
            return instalaciones.filter { instalacion ->
                instalacion.nombre.contains(consulta, ignoreCase = true) ||
                        instalacion.nombreComuna.contains(consulta, ignoreCase = true) ||
                        (instalacion.ubicacion ?: "").contains(consulta, ignoreCase = true)
            }
        }
    }
}

