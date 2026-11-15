package com.example.induzteckapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.induzteckapp.data.InduztekPreferencias
import com.example.induzteckapp.data.ProtocolosRepository
import com.example.induzteckapp.model.ProtocoloDetalle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetalleRespaldoViewModel(application: Application) : AndroidViewModel(application) {
    private val protocolosRepository = ProtocolosRepository(application)
    private val preferencias = InduztekPreferencias(application)
    private val _protocoloState = MutableStateFlow<ProtocoloDetalle?>(null)
    val protocoloState: StateFlow<ProtocoloDetalle?> = _protocoloState.asStateFlow()
    private val _cargandoState = MutableStateFlow(true)
    val cargandoState: StateFlow<Boolean> = _cargandoState.asStateFlow()
    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()
    private val _esTecnico = MutableStateFlow(false)
    val esTecnico: StateFlow<Boolean> = _esTecnico.asStateFlow()

    init { _esTecnico.value = preferencias.leerRol() == "TECNICO_INGENIERO" }

    fun cargarDetalle(idProtocolo: Int) {
        viewModelScope.launch {
            _cargandoState.value = true
            _errorState.value = null
            try {
                _protocoloState.value = protocolosRepository.obtenerDetalleProtocolo(idProtocolo)
            } catch (e: Exception) {
                _protocoloState.value = null
                _errorState.value = e.message ?: "No fue posible cargar el detalle del protocolo."
            } finally { _cargandoState.value = false }
        }
    }
}
