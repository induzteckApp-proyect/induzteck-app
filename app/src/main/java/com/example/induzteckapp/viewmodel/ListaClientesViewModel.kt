package com.example.induzteckapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.induzteckapp.data.InduztekDbHelper
import com.example.induzteckapp.data.InduztekPreferencias
import com.example.induzteckapp.model.ClienteConsulta
import com.example.induzteckapp.model.RolUsuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ListaClientesViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = InduztekDbHelper(application)
    private val preferencias = InduztekPreferencias(application)

    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    
    private val _clientesState = MutableStateFlow<List<ClienteConsulta>>(emptyList())
    val clientesState: StateFlow<List<ClienteConsulta>> = _clientesState.asStateFlow()

    private val _esTecnico = MutableStateFlow(false)
    val esTecnico: StateFlow<Boolean> = _esTecnico.asStateFlow()

    init {
        cargarDatos()
        verificarRolTecnico()
    }

    private fun verificarRolTecnico() {
        val idUsuario = preferencias.leerSesion()
        if (idUsuario > 0L) {
            viewModelScope.launch(Dispatchers.IO) {
                val user = dbHelper.obtenerUsuarioPorId(idUsuario)
                _esTecnico.value = user?.rol == RolUsuario.TECNICO_INGENIERO
            }
        }
    }

    
    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        cargarDatos(newQuery)
    }

    private fun cargarDatos(filtro: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            val result = dbHelper.obtenerClientesConActivos(filtro)
            _clientesState.value = result
        }
    }

    
    fun toggleClienteExpansion(idCliente: Int) {
        _clientesState.value = _clientesState.value.map { cliente ->
            if (cliente.idUsuario == idCliente) {
                cliente.copy(isExpanded = !cliente.isExpanded)
            } else cliente
        }
    }

    
    fun toggleEquipoExpansion(idCliente: Int, idEquipo: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val historial = dbHelper.obtenerHistorialEquipo(idEquipo)
            _clientesState.value = _clientesState.value.map { cliente ->
                if (cliente.idUsuario == idCliente) {
                    val instalacionesActualizadas = cliente.instalaciones.map { instalacion ->
                        val equiposActualizados = instalacion.equipos.map { equipo ->
                            if (equipo.idEquipo == idEquipo) {
                                equipo.copy(
                                    isExpanded = !equipo.isExpanded,
                                    historialProtocolos = historial
                                )
                            } else equipo
                        }
                        instalacion.copy(equipos = equiposActualizados)
                    }
                    cliente.copy(instalaciones = instalacionesActualizadas)
                } else cliente
            }
        }
    }
}
