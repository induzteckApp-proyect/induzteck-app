package com.example.induzteckapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.induzteckapp.data.InduztekPreferencias
import com.example.induzteckapp.data.UsuarioRepository
import com.example.induzteckapp.model.InicioUiState
import com.example.induzteckapp.model.RolUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InicioViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencias = InduztekPreferencias(application)
    private val repository = UsuarioRepository(application)
    private val _uiState = MutableStateFlow(InicioUiState())
    val uiState: StateFlow<InicioUiState> = _uiState.asStateFlow()

    init { cargarUsuarioSesionActiva() }

    fun cargarUsuarioSesionActiva() {
        if (preferencias.leerToken().isNullOrBlank()) {
            _uiState.value = InicioUiState()
            return
        }
        viewModelScope.launch {
            try {
                val usuario = repository.obtenerMiPerfil()
                val esTecnico = usuario.rol == RolUsuario.TECNICO_INGENIERO
                val esAdmin = usuario.rol == RolUsuario.ADMIN
                _uiState.update {
                    it.copy(
                        usuario = usuario,
                        esTecnico = esTecnico,
                        esCliente = usuario.rol == RolUsuario.CLIENTE,
                        esAdmin = esAdmin,
                        tituloSaludo = when {
                            esAdmin -> "Hola, ${usuario.nombre}"
                            esTecnico -> "Hola, Técnico ${usuario.nombre}"
                            else -> "Hola, ${usuario.nombre}"
                        },
                        descripcionBienvenida = when {
                            esAdmin -> "Administra las cuentas de clientes y del personal técnico."
                            esTecnico -> "Espacio de trabajo operativo. Registra protocolos e inspecciona instalaciones."
                            else -> "Consulta el estado de tus instalaciones y respaldos digitales."
                        },
                        mostrarBotonCrearProtocolo = esTecnico
                    )
                }
            } catch (_: Exception) {
                preferencias.borrarSesion()
                _uiState.value = InicioUiState()
            }
        }
    }

    fun cerrarSesion() {
        preferencias.borrarSesion()
        _uiState.value = InicioUiState()
    }
}
