package com.example.induzteckapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.induzteckapp.data.InduztekDbHelper
import com.example.induzteckapp.data.InduztekPreferencias
import com.example.induzteckapp.model.PruebaDetalle
import com.example.induzteckapp.model.ProtocoloDetalle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetalleRespaldoViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = InduztekDbHelper(application)
    private val preferencias = InduztekPreferencias(application)

    private val _protocoloState = MutableStateFlow<ProtocoloDetalle?>(null)
    val protocoloState: StateFlow<ProtocoloDetalle?> = _protocoloState.asStateFlow()
    private val _cargandoState = MutableStateFlow(true)
    val cargandoState: StateFlow<Boolean> = _cargandoState.asStateFlow()
    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()
    private val _esTecnico = MutableStateFlow(false)
    val esTecnico: StateFlow<Boolean> = _esTecnico.asStateFlow()

    init {
        _esTecnico.value = preferencias.leerRol() == "TECNICO_INGENIERO"
    }

    fun cargarDetalle(idProtocolo: Int) {
        viewModelScope.launch {
            _cargandoState.value = true
            _errorState.value = null
            try {
                val detalle = withContext(Dispatchers.IO) {
                    val base = dbHelper.obtenerDetalleProtocolo(idProtocolo) ?: return@withContext null
                    // Reutiliza la consulta detallada del Historial para mostrar cada parámetro,
                    // su valor y su unidad; no se agrupan lecturas sin identificar su origen.
                    val historial = dbHelper.obtenerHistorialProtocolos()
                        .firstOrNull { it.idProtocolo == idProtocolo.toLong() }
                    if (historial == null) base else {
                        val pruebas = historial.pruebas.map { prueba ->
                            val lecturas = prueba.mediciones.joinToString("\n") {
                                "${it.nombre}: ${it.valor} ${it.unidad}"
                            }.ifBlank { if (prueba.realizada) "Sin mediciones" else "Prueba pendiente" }
                            val resultado = when (prueba.idResultado) {
                                InduztekDbHelper.RESULTADO_APROBADO -> "Aprobado"
                                InduztekDbHelper.RESULTADO_OBSERVADO -> "Observado"
                                InduztekDbHelper.RESULTADO_RECHAZADO -> "Rechazado"
                                else -> "Pendiente"
                            }
                            PruebaDetalle(
                                idPrueba = prueba.idPrueba.toInt(),
                                tipoPruebaNombre = prueba.nombre,
                                elemento = prueba.elemento,
                                momento = "",
                                valorMedido = lecturas,
                                unidadMedida = "",
                                resultado = resultado
                            )
                        }
                        base.copy(pruebas = pruebas)
                    }
                }
                _protocoloState.value = detalle
            } catch (_: Exception) {
                _protocoloState.value = null
                _errorState.value = "No fue posible cargar el detalle del protocolo."
            } finally {
                _cargandoState.value = false
            }
        }
    }

    override fun onCleared() {
        dbHelper.close()
        super.onCleared()
    }
}
