package com.example.induzteckapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.induzteckapp.data.InduztekDbHelper
import com.example.induzteckapp.data.InduztekPreferencias
import com.example.induzteckapp.model.ProtocoloHistorial
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class HistorialProtocolosUiState(
    val protocolos: List<ProtocoloHistorial> = emptyList(),
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val mensajeError: String? = null,
    val mensajeExito: String? = null
)

class HistorialProtocolosViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = InduztekDbHelper(application)
    private val preferencias = InduztekPreferencias(application)
    private val _uiState = MutableStateFlow(HistorialProtocolosUiState())
    val uiState: StateFlow<HistorialProtocolosUiState> = _uiState.asStateFlow()

    init { cargarHistorial() }

    fun cargarHistorial() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, mensajeError = null) }
            try {
                val lista = withContext(Dispatchers.IO) { dbHelper.obtenerHistorialProtocolos() }
                _uiState.update { it.copy(protocolos = lista, cargando = false) }
            } catch (_: Exception) {
                _uiState.update { it.copy(cargando = false, mensajeError = "No fue posible cargar el historial de protocolos.") }
            }
        }
    }

    fun cambiarEstado(idProtocolo: Long, nuevoEstado: String, motivo: String = "") {
        val idUsuario = preferencias.leerSesion()
        if (idUsuario <= 0L) {
            _uiState.update { it.copy(mensajeError = "No hay una sesión activa.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, mensajeError = null, mensajeExito = null) }
            try {
                withContext(Dispatchers.IO) {
                    dbHelper.cambiarEstadoProtocolo(idProtocolo, nuevoEstado, idUsuario, motivo)
                }
                val lista = withContext(Dispatchers.IO) { dbHelper.obtenerHistorialProtocolos() }
                _uiState.update { it.copy(protocolos = lista, guardando = false, mensajeExito = "Estado del protocolo actualizado correctamente.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, mensajeError = e.message ?: "No fue posible cambiar el estado del protocolo.") }
            }
        }
    }

    fun limpiarMensajes() { _uiState.update { it.copy(mensajeError = null, mensajeExito = null) } }
}
