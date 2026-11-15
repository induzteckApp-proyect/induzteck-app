package com.example.induzteckapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.induzteckapp.data.ProtocolosRepository
import com.example.induzteckapp.model.ProtocoloHistorial
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistorialProtocolosUiState(
    val protocolos: List<ProtocoloHistorial> = emptyList(),
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val mensajeError: String? = null,
    val mensajeExito: String? = null,
    val busqueda: String = ""
)

class HistorialProtocolosViewModel(application: Application) : AndroidViewModel(application) {
    private val protocolosRepository = ProtocolosRepository(application)
    private var todosLosProtocolos: List<ProtocoloHistorial> = emptyList()
    private val _uiState = MutableStateFlow(HistorialProtocolosUiState())
    val uiState: StateFlow<HistorialProtocolosUiState> = _uiState.asStateFlow()

    init { cargarHistorial() }

    fun cargarHistorial() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, mensajeError = null) }
            try {
                todosLosProtocolos = protocolosRepository.obtenerHistorialProtocolos()
                aplicarFiltro()
                _uiState.update { it.copy(cargando = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, mensajeError = e.message ?: "No fue posible cargar el historial de protocolos.") }
            }
        }
    }

    fun onBusquedaChanged(valor: String) {
        _uiState.update { it.copy(busqueda = valor) }
        aplicarFiltro()
    }

    private fun aplicarFiltro() {
        val filtro = _uiState.value.busqueda.trim().lowercase()
        val lista = if (filtro.isBlank()) todosLosProtocolos else todosLosProtocolos.filter { p ->
            listOf<String>(p.numero, p.cliente, p.instalacion, p.ubicacion, p.tagEquipo, p.tipoEquipo,
                p.estadoNombre, p.estadoCodigo, p.fechaHora, p.tecnico)
                .any { it.lowercase().contains(filtro) }
        }
        _uiState.update { it.copy(protocolos = lista) }
    }

    fun cambiarEstado(idProtocolo: Long, nuevoEstado: String, motivo: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, mensajeError = null, mensajeExito = null) }
            try {
                protocolosRepository.cambiarEstado(idProtocolo, nuevoEstado, motivo)
                todosLosProtocolos = protocolosRepository.obtenerHistorialProtocolos()
                aplicarFiltro()
                _uiState.update { it.copy(guardando = false, mensajeExito = "Estado del protocolo actualizado correctamente.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, mensajeError = e.message ?: "No fue posible cambiar el estado del protocolo.") }
            }
        }
    }

    fun limpiarMensajes() { _uiState.update { it.copy(mensajeError = null, mensajeExito = null) } }
}
