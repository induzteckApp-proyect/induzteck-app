package com.example.induzteckapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.induzteckapp.data.UsuarioRepository
import com.example.induzteckapp.model.LoginUiState
import com.example.induzteckapp.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UsuarioRepository(application)
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val patronCorreo = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val patronContrasena =
        Regex("^(?=.*\\p{Lu})(?=.*\\p{Ll})(?=.*[0-9])(?=.*[!@#$%&*()_+\\-=\\[\\]{};':\\\\|,.<>/?¿¡~^])\\S{8,72}$")

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor, errorCorreo = null, mensajeError = null) }
    }

    fun onContrasenaCambia(valor: String) {
        _uiState.update { it.copy(contrasena = valor, errorContrasena = null, mensajeError = null) }
    }

    fun onAlternarVisibilidadContrasena() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    fun iniciarSesion(onResultado: (Usuario?) -> Unit) {
        val estado = _uiState.value
        val correo = estado.correo.trim()
        val errorCorreo = when {
            correo.isBlank() -> "Ingresa tu correo electrónico."
            !patronCorreo.matches(correo) -> "Ingresa un correo electrónico válido."
            else -> null
        }
        val errorClave = if (estado.contrasena.isBlank()) "Ingresa tu contraseña." else null
        if (errorCorreo != null || errorClave != null) {
            _uiState.update { it.copy(errorCorreo = errorCorreo, errorContrasena = errorClave) }
            onResultado(null)
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, mensajeError = null) }
            try {
                val usuario = repository.iniciarSesion(correo, estado.contrasena)
                _uiState.update { it.copy(cargando = false, errorCorreo = null, errorContrasena = null) }
                onResultado(usuario)
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, mensajeError = e.message ?: "No se pudo iniciar sesión.") }
                onResultado(null)
            }
        }
    }

    fun solicitarRecuperacion(correo: String, onResultado: (Boolean, String, String?) -> Unit) {
        if (!patronCorreo.matches(correo.trim())) {
            onResultado(false, "Ingresa un correo electrónico válido.", null)
            return
        }
        viewModelScope.launch {
            try {
                val respuesta = repository.solicitarRecuperacion(correo)
                onResultado(true, respuesta.mensaje, respuesta.tokenDesarrollo)
            } catch (e: Exception) {
                onResultado(false, e.message ?: "No se pudo solicitar la recuperación.", null)
            }
        }
    }

    fun restablecerContrasena(token: String, nueva: String, confirmar: String, onResultado: (Boolean, String) -> Unit) {
        if (token.isBlank()) { onResultado(false, "Ingresa el token recibido por correo."); return }
        if (!patronContrasena.matches(nueva)) {
            onResultado(false, "La clave debe tener 8 a 72 caracteres, mayúscula, minúscula, número y símbolo.")
            return
        }
        if (nueva != confirmar) { onResultado(false, "Las contraseñas no coinciden."); return }
        viewModelScope.launch {
            try { onResultado(true, repository.restablecerContrasena(token, nueva)) }
            catch (e: Exception) { onResultado(false, e.message ?: "No se pudo cambiar la contraseña.") }
        }
    }

    fun activarCuenta(token: String, nueva: String, confirmar: String, onResultado: (Boolean, String) -> Unit) {
        if (token.isBlank()) { onResultado(false, "Ingresa el token de invitación."); return }
        if (!patronContrasena.matches(nueva)) {
            onResultado(false, "La clave debe tener 8 a 72 caracteres, mayúscula, minúscula, número y símbolo.")
            return
        }
        if (nueva != confirmar) { onResultado(false, "Las contraseñas no coinciden."); return }
        viewModelScope.launch {
            try {
                repository.activarCuenta(token, nueva)
                onResultado(true, "Cuenta activada. Ya puedes iniciar sesión.")
            } catch (e: Exception) { onResultado(false, e.message ?: "No se pudo activar la cuenta.") }
        }
    }

    fun limpiarFormulario() { _uiState.value = LoginUiState() }
}
