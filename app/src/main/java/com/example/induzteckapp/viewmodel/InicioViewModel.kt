


package com.example.induzteckapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.induzteckapp.data.InduztekDbHelper
import com.example.induzteckapp.data.InduztekPreferencias
import com.example.induzteckapp.model.InicioUiState
import com.example.induzteckapp.model.RolUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InicioViewModel(application: Application) :
    AndroidViewModel(application) {

    private val dbHelper = InduztekDbHelper(application)
    private val preferencias = InduztekPreferencias(application)

    private val _uiState = MutableStateFlow(InicioUiState())
    val uiState: StateFlow<InicioUiState> = _uiState.asStateFlow()

    init {
        cargarUsuarioSesionActiva()
    }

    
    fun cargarUsuarioSesionActiva() {
        val idUsuario = preferencias.leerSesion()

        
        if (idUsuario == -1L) {
            _uiState.value = InicioUiState()
            return
        }

        
        val usuario = dbHelper.obtenerUsuarioPorId(idUsuario)

        if (usuario == null) {
            _uiState.value = InicioUiState()
            return
        }

        val esTecnico =
            usuario.rol == RolUsuario.TECNICO_INGENIERO

        _uiState.update { state ->
            state.copy(
                usuario = usuario,
                esTecnico = esTecnico,
                esCliente = !esTecnico,
                tituloSaludo = if (esTecnico) {
                    "Hola, Técnico ${usuario.nombre}"
                } else {
                    "Hola, ${usuario.nombre}"
                },
                descripcionBienvenida = if (esTecnico) {
                    "Espacio de trabajo operativo. Registra protocolos en terreno e inspecciona las instalaciones."
                } else {
                    "Espacio de consulta de cliente. Visualiza el estado de tus instalaciones y revisiones eléctricas."
                },
                mostrarBotonCrearProtocolo = esTecnico
            )
        }
    }

    fun cerrarSesion() {
        preferencias.borrarSesion()

        _uiState.update {
            InicioUiState()
        }
    }
}
