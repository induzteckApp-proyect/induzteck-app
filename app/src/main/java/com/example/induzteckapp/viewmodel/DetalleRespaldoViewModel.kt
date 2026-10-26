package com.example.induzteckapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.induzteckapp.data.InduztekDbHelper
import com.example.induzteckapp.model.ProtocoloDetalle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetalleRespaldoViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = InduztekDbHelper(application)

    private val _protocoloState = MutableStateFlow<ProtocoloDetalle?>(null)
    val protocoloState: StateFlow<ProtocoloDetalle?> = _protocoloState.asStateFlow()

    private val _cargandoState = MutableStateFlow(true)
    val cargandoState: StateFlow<Boolean> = _cargandoState.asStateFlow()

    fun cargarDetalle(idProtocolo: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _cargandoState.value = true
            val detalle = dbHelper.obtenerDetalleProtocolo(idProtocolo)
            _protocoloState.value = detalle
            _cargandoState.value = false
        }
    }
}
