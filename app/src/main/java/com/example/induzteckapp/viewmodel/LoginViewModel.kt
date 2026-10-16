package com.example.induzteckapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.induzteckapp.model.LoginUiState
import com.example.induzteckapp.model.Usuario
import com.example.induzteckapp.util.verificarContrasena
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val patronCorreo =
        Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun onCorreoCambia(valor: String) {
        _uiState.update {
            it.copy(
                correo = valor,
                errorCorreo = null,
                mensajeError = null
            )
        }
    }

    fun onContrasenaCambia(valor: String) {
        _uiState.update {
            it.copy(
                contrasena = valor,
                errorContrasena = null,
                mensajeError = null
            )
        }
    }

    fun onAlternarVisibilidadContrasena() {
        _uiState.update {
            it.copy(
                contrasenaVisible = !it.contrasenaVisible
            )
        }
    }

    /**
     * Devuelve el usuario si las credenciales son válidas.
     * Devuelve null cuando existe algún error.
     */
    fun validarCredenciales(
        usuarios: List<Usuario>
    ): Usuario? {

        val estado = _uiState.value
        val correoNormalizado = estado.correo.trim()

        val errorCorreo = when {
            correoNormalizado.isBlank() ->
                "Ingresa tu correo electrónico"

            correoNormalizado.length > 254 ->
                "El correo electrónico es demasiado largo"

            !patronCorreo.matches(correoNormalizado) ->
                "Ingresa un correo electrónico válido"

            else -> null
        }

        val errorContrasena = when {
            estado.contrasena.isBlank() ->
                "Ingresa tu contraseña"

            else -> null
        }

        if (errorCorreo != null || errorContrasena != null) {
            _uiState.update {
                it.copy(
                    errorCorreo = errorCorreo,
                    errorContrasena = errorContrasena,
                    mensajeError = null
                )
            }

            return null
        }

        val usuario = usuarios.firstOrNull {
            it.correo.equals(correoNormalizado, ignoreCase = true)
        }

        val credencialesValidas =
            usuario != null &&
                    usuario.activo &&
                    verificarContrasena(
                        contrasena = estado.contrasena,
                        resumenGuardado = usuario.passwordHash
                    )

        if (!credencialesValidas) {
            _uiState.update {
                it.copy(
                    errorCorreo = null,
                    errorContrasena = null,
                    mensajeError =
                        "Correo o contraseña incorrectos, o cuenta no disponible."
                )
            }

            return null
        }

        _uiState.update {
            it.copy(
                errorCorreo = null,
                errorContrasena = null,
                mensajeError = null
            )
        }

        return usuario
    }

    fun limpiarFormulario() {
        _uiState.value = LoginUiState()
    }
}