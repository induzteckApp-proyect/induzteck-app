package com.example.induzteckapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.induzteckapp.data.InduztekDbHelper
import com.example.induzteckapp.model.ClienteConsulta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ListaClientesViewModel(private val dbHelper: InduztekDbHelper) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _clientesState = MutableStateFlow<List<ClienteConsulta>>(emptyList())
    val clientesState = _clientesState.asStateFlow()

    init {
        cargarDatos()
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
            if (cliente.idUsuario == idCliente) cliente.copy(isExpanded = !cliente.isExpanded)
            else cliente
        }
    }

    fun toggleEquipoExpansion(idCliente: Int, idEquipo: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val historial = dbHelper.obtenerHistorialEquipo(idEquipo)
            _clientesState.value = _clientesState.value.map { cliente ->
                if (cliente.idUsuario == idCliente) {
                    val instsActualizadas = cliente.instalaciones.map { inst ->
                        val eqActualizados = inst.equipos.map { eq ->
                            if (eq.idEquipo == idEquipo) {
                                eq.copy(
                                    isExpanded = !eq.isExpanded,
                                    historialProtocolos = historial
                                )
                            } else eq
                        }
                        inst.copy(equipos = eqActualizados)
                    }
                    cliente.copy(instalaciones = instsActualizadas)
                } else cliente
            }
        }
    }
}