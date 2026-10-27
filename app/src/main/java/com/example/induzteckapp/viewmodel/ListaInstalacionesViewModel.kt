package com.example.induzteckapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.induzteckapp.data.InduztekDbHelper
import com.example.induzteckapp.data.InduztekPreferencias
import com.example.induzteckapp.model.InstalacionConsulta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ListaInstalacionesViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = InduztekDbHelper(application)
    private val preferencias = InduztekPreferencias(application)

    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    
    private val _instalacionesState = MutableStateFlow<List<InstalacionConsulta>>(emptyList())
    val instalacionesState: StateFlow<List<InstalacionConsulta>> = _instalacionesState.asStateFlow()

    private var idClienteLogueado: Long = -1L

    init {
        idClienteLogueado = preferencias.leerSesion()
        cargarDatos()
    }

    
    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        cargarDatos(newQuery)
    }

    private fun cargarDatos(filtro: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            val result = if (idClienteLogueado != -1L) {
                dbHelper.obtenerInstalacionesCliente(idClienteLogueado, filtro)
            } else {
                emptyList()
            }
            _instalacionesState.value = result
        }
    }

    
    fun toggleInstalacionExpansion(idInstalacion: Int) {
        _instalacionesState.value = _instalacionesState.value.map { instalacion ->
            if (instalacion.idInstalacion == idInstalacion) {
                instalacion.copy(isExpanded = !instalacion.isExpanded)
            } else instalacion
        }
    }

    
    fun toggleEquipoExpansion(idInstalacion: Int, idEquipo: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val historial = dbHelper.obtenerHistorialEquipoCliente(idEquipo)
            _instalacionesState.value = _instalacionesState.value.map { instalacion ->
                if (instalacion.idInstalacion == idInstalacion) {
                    val equiposActualizados = instalacion.equipos.map { equipo ->
                        if (equipo.idEquipo == idEquipo) {
                            equipo.copy(
                                isExpanded = !equipo.isExpanded,
                                historialProtocolos = historial
                            )
                        } else equipo
                    }
                    instalacion.copy(equipos = equiposActualizados)
                } else instalacion
            }
        }
    }
}
