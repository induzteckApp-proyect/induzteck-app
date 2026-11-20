package com.example.induzteckapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.induzteckapp.data.ActivosRepository
import com.example.induzteckapp.data.InduztekPreferencias
import com.example.induzteckapp.data.ProtocolosRepository
import com.example.induzteckapp.model.ClienteConsulta
import com.example.induzteckapp.model.RolUsuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListaClientesViewModel(application: Application) : AndroidViewModel(application) {
    private val activosRepository = ActivosRepository(application)
    private val protocolosRepository = ProtocolosRepository(application)
    private val preferencias = InduztekPreferencias(application)
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

    init {
        _esTecnico.value = preferencias.leerRol() == RolUsuario.TECNICO_INGENIERO.codigo
        cargarDatos()
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        cargarDatos(newQuery)
    }

    private fun cargarDatos(filtro: String = "") {
        viewModelScope.launch {
            try {
                val clientes = withContext(Dispatchers.IO) {
                    activosRepository.listarClientesConsulta(filtro)
                }
                _clientesState.value = filtrarClientes(clientes, filtro)
            } catch (e: Exception) {
                _mensajeEstado.value = e.message ?: "No fue posible cargar los clientes y sus activos."
                _clientesState.value = emptyList()
            }
        }
    }

    fun toggleClienteExpansion(idCliente: Int) {
        _clientesState.value = alternarExpansionCliente(_clientesState.value, idCliente)
    }

    fun toggleEquipoExpansion(idCliente: Int, idEquipo: Int) {
        viewModelScope.launch {
            try {
                val historial = withContext(Dispatchers.IO) { protocolosRepository.obtenerHistorialEquipo(idEquipo) }
                _clientesState.value = _clientesState.value.map { cliente ->
                    if (cliente.idUsuario == idCliente) {
                        cliente.copy(instalaciones = cliente.instalaciones.map { instalacion ->
                            instalacion.copy(equipos = instalacion.equipos.map { equipo ->
                                if (equipo.idEquipo == idEquipo) equipo.copy(
                                    isExpanded = !equipo.isExpanded, historialProtocolos = historial
                                ) else equipo
                            })
                        })
                    } else cliente
                }
            } catch (e: Exception) {
                _mensajeEstado.value = e.message ?: "No fue posible cargar el historial del equipo."
            }
        }
    }

    fun cambiarEstadoProtocolo(idProtocolo: Int, idEquipo: Int, nuevoEstado: String, motivo: String) {
        viewModelScope.launch {
            _guardandoEstado.value = true
            _mensajeEstado.value = null
            try {
                withContext(Dispatchers.IO) { protocolosRepository.cambiarEstado(idProtocolo.toLong(), nuevoEstado, motivo) }
                val historial = withContext(Dispatchers.IO) { protocolosRepository.obtenerHistorialEquipo(idEquipo) }
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

    companion object {
        fun filtrarClientes(clientes: List<ClienteConsulta>, filtro: String): List<ClienteConsulta> {
            val consulta = filtro.trim()
            if (consulta.isBlank()) return clientes
            return clientes.filter { cliente ->
                cliente.nombreRazonSocial.contains(consulta, ignoreCase = true) ||
                        cliente.rut.contains(consulta, ignoreCase = true) ||
                        cliente.correo.contains(consulta, ignoreCase = true)
            }
        }

        fun alternarExpansionCliente(clientes: List<ClienteConsulta>, idCliente: Int): List<ClienteConsulta> =
            clientes.map { cliente ->
                if (cliente.idUsuario == idCliente) cliente.copy(isExpanded = !cliente.isExpanded) else cliente
            }
    }
}


/*
package com.example.induzteckapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.induzteckapp.data.ActivosRepository
import com.example.induzteckapp.data.InduztekPreferencias
import com.example.induzteckapp.data.ProtocolosRepository
import com.example.induzteckapp.model.ClienteConsulta
import com.example.induzteckapp.model.RolUsuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListaClientesViewModel(application: Application) : AndroidViewModel(application) {
    private val activosRepository = ActivosRepository(application)
    private val protocolosRepository = ProtocolosRepository(application)
    private val preferencias = InduztekPreferencias(application)
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

    init {
        _esTecnico.value = preferencias.leerRol() == RolUsuario.TECNICO_INGENIERO.codigo
        cargarDatos()
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        cargarDatos(newQuery)
    }

    private fun cargarDatos(filtro: String = "") {
        viewModelScope.launch {
            try {
                _clientesState.value = withContext(Dispatchers.IO) {
                    activosRepository.listarClientesConsulta(filtro)
                }
            } catch (e: Exception) {
                _mensajeEstado.value = e.message ?: "No fue posible cargar los clientes y sus activos."
                _clientesState.value = emptyList()
            }
        }
    }

    fun toggleClienteExpansion(idCliente: Int) {
        _clientesState.value = _clientesState.value.map {
            if (it.idUsuario == idCliente) it.copy(isExpanded = !it.isExpanded) else it
        }
    }

    fun toggleEquipoExpansion(idCliente: Int, idEquipo: Int) {
        viewModelScope.launch {
            try {
                val historial = withContext(Dispatchers.IO) { protocolosRepository.obtenerHistorialEquipo(idEquipo) }
                _clientesState.value = _clientesState.value.map { cliente ->
                    if (cliente.idUsuario == idCliente) {
                        cliente.copy(instalaciones = cliente.instalaciones.map { instalacion ->
                            instalacion.copy(equipos = instalacion.equipos.map { equipo ->
                                if (equipo.idEquipo == idEquipo) equipo.copy(
                                    isExpanded = !equipo.isExpanded, historialProtocolos = historial
                                ) else equipo
                            })
                        })
                    } else cliente
                }
            } catch (e: Exception) {
                _mensajeEstado.value = e.message ?: "No fue posible cargar el historial del equipo."
            }
        }
    }

    fun cambiarEstadoProtocolo(idProtocolo: Int, idEquipo: Int, nuevoEstado: String, motivo: String) {
        viewModelScope.launch {
            _guardandoEstado.value = true
            _mensajeEstado.value = null
            try {
                withContext(Dispatchers.IO) { protocolosRepository.cambiarEstado(idProtocolo.toLong(), nuevoEstado, motivo) }
                val historial = withContext(Dispatchers.IO) { protocolosRepository.obtenerHistorialEquipo(idEquipo) }
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
}

*/