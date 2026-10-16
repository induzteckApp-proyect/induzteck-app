package com.example.induzteckapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.induzteckapp.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class UsuariosViewModel : ViewModel() {

    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    /**
     * Registra una cuenta nueva.
     *
     * Devuelve false si el correo ya existe.
     */
    fun registrar(usuario: Usuario): Boolean {
        val correoExiste = _usuarios.value.any {
            it.correo.equals(usuario.correo, ignoreCase = true)
        }

        if (correoExiste) {
            return false
        }

        _usuarios.update { lista ->
            lista + usuario
        }

        // El registro NO inicia la sesión automáticamente.
        return true
    }

    fun iniciarSesion(usuario: Usuario) {
        val usuarioRegistrado = _usuarios.value.firstOrNull {
            it.idUsuario == usuario.idUsuario &&
                    it.correo.equals(usuario.correo, ignoreCase = true)
        }

        if (usuarioRegistrado != null && usuarioRegistrado.activo) {
            _usuarioActual.value = usuarioRegistrado
        }
    }

    fun cerrarSesion() {
        _usuarioActual.value = null
    }
}