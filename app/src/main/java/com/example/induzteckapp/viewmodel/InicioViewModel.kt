package com.example.induzteckapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.induzteckapp.model.InicioUiState
import com.example.induzteckapp.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InicioViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(InicioUiState())
    val uiState: StateFlow<InicioUiState> = _uiState.asStateFlow()

    /**
     * Actualiza el usuario que tiene la sesión activa.
     * El contenido de Inicio depende de este usuario.
     */
    fun actualizarUsuario(usuario: Usuario?) {
        _uiState.update {
            it.copy(usuario = usuario)
        }
    }
}

/*
* Este es el punto de partida de la futura lógica de Inicio.
* No crearemos una pantalla distinta para Cliente y Técnico:
* ambas experiencias estarán dentro de InicioScreen,
* y el contenido se decidirá a partir de usuario.rol.
* */