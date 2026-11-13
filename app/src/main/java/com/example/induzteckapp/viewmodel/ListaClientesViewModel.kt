package com.example.induzteckapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.induzteckapp.data.EstadoProtocoloRepository
import com.example.induzteckapp.data.InduztekDbHelper
import com.example.induzteckapp.data.InduztekPreferencias
import com.example.induzteckapp.model.ClienteConsulta
import com.example.induzteckapp.model.RolUsuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListaClientesViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = InduztekDbHelper(application)
    private val preferencias = InduztekPreferencias(application)
    private val estados = EstadoProtocoloRepository(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _clientesState = MutableStateFlow<List<ClienteConsulta>>(emptyList())
    val clientesState: StateFlow<List<ClienteConsulta>> = _clientesState.asStateFlow()
    private val _esTecnico = MutableStateFlow(false)
    val esTecnico: StateFlow<Boolean> = _esTecnico.asStateFlow()
    private val _guardandoEstado = MutableStateFlow(false)
    val guardandoEstado: StateFlow<Boolean> = _guardandoEstado.asStateFlow()
    private val _mensajeEstado = MutableStateFlow<String?>(null)
    val mensajeEstado: StateFlow<String?> = _mensajeEstado.asStateFlow()

    init { cargarDatos(); verificarRolTecnico() }

    private fun verificarRolTecnico() {
        val idUsuario = preferencias.leerSesion()
        if (idUsuario > 0L) {
            _esTecnico.value = preferencias.leerRol() == RolUsuario.TECNICO_INGENIERO.name
        }
    }

    fun onSearchQueryChanged(newQuery: String) { _searchQuery.value = newQuery; cargarDatos(newQuery) }

    private fun cargarDatos(filtro: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            _clientesState.value = dbHelper.obtenerClientesConActivos(filtro)
        }
    }

    fun toggleClienteExpansion(idCliente: Int) {
        _clientesState.value = _clientesState.value.map {
            if (it.idUsuario == idCliente) it.copy(isExpanded = !it.isExpanded) else it
        }
    }

    fun toggleEquipoExpansion(idCliente: Int, idEquipo: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val historial = dbHelper.obtenerHistorialEquipo(idEquipo)
            _clientesState.value = _clientesState.value.map { cliente ->
                if (cliente.idUsuario == idCliente) {
                    val instalaciones = cliente.instalaciones.map { instalacion ->
                        val equipos = instalacion.equipos.map { equipo ->
                            if (equipo.idEquipo == idEquipo) equipo.copy(
                                isExpanded = !equipo.isExpanded, historialProtocolos = historial
                            ) else equipo
                        }
                        instalacion.copy(equipos = equipos)
                    }
                    cliente.copy(instalaciones = instalaciones)
                } else cliente
            }
        }
    }

    fun cambiarEstadoProtocolo(idProtocolo: Int, idEquipo: Int, nuevoEstado: String, motivo: String) {
        viewModelScope.launch {
            _guardandoEstado.value = true
            _mensajeEstado.value = null
            try {
                estados.cambiarEstado(idProtocolo.toLong(), nuevoEstado, motivo)
                val historial = withContext(Dispatchers.IO) { dbHelper.obtenerHistorialEquipo(idEquipo) }
                _clientesState.value = _clientesState.value.map { cliente ->
                    cliente.copy(instalaciones = cliente.instalaciones.map { instalacion ->
                        instalacion.copy(equipos = instalacion.equipos.map { equipo ->
                            if (equipo.idEquipo == idEquipo) equipo.copy(historialProtocolos = historial) else equipo
                        })
                    })
                }
                _mensajeEstado.value = "Estado del protocolo actualizado correctamente."
            } catch (e: Exception) {
                _mensajeEstado.value = e.message ?: "No fue posible cambiar el estado."
            } finally { _guardandoEstado.value = false }
        }
    }

    fun limpiarMensajeEstado() { _mensajeEstado.value = null }

    override fun onCleared() {
        dbHelper.close()
        estados.cerrar()
        super.onCleared()
    }
}
