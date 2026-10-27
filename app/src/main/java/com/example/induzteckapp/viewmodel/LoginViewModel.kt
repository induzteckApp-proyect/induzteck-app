package com.example.induzteckapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.induzteckapp.data.InduztekDbHelper
import com.example.induzteckapp.data.InduztekPreferencias
import com.example.induzteckapp.model.LoginUiState
import com.example.induzteckapp.model.Usuario
import com.example.induzteckapp.util.generarResumenContrasena
import com.example.induzteckapp.util.verificarContrasena
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel(application: Application) :
    AndroidViewModel(application) {

    private val dbHelper = InduztekDbHelper(application)
    private val preferencias = InduztekPreferencias(application)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val patronCorreo =
        Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    private val patronContrasena =
        Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9\\s])\\S{8,}$")

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

    fun validarCredenciales(): Usuario? {
        val estado = _uiState.value
        val correoNormalizado = estado.correo.trim()

        val errorCorreo = when {
            correoNormalizado.isBlank() ->
                "Ingresa tu correo electrónico"

            !patronCorreo.matches(correoNormalizado) ->
                "Ingresa un correo electrónico válido"

            else -> null
        }

        val errorContrasena =
            if (estado.contrasena.isBlank()) {
                "Ingresa tu contraseña"
            } else {
                null
            }

        if (errorCorreo != null || errorContrasena != null) {
            _uiState.update {
                it.copy(
                    errorCorreo = errorCorreo,
                    errorContrasena = errorContrasena
                )
            }
            return null
        }

        val usuario = dbHelper.obtenerUsuarioPorCorreo(
            correoNormalizado
        )

        val credencialesValidas =
            usuario != null &&
                    usuario.activo &&
                    verificarContrasena(
                        estado.contrasena,
                        usuario.passwordHash
                    )

        if (!credencialesValidas) {
            _uiState.update {
                it.copy(
                    mensajeError =
                        "Correo o contraseña incorrectos, o cuenta inactiva."
                )
            }
            return null
        }

        preferencias.guardarSesion(usuario!!.idUsuario!!)

        _uiState.update {
            it.copy(
                errorCorreo = null,
                errorContrasena = null,
                mensajeError = null
            )
        }

        return usuario
    }

    fun recuperarContrasena(
        correo: String,
        nuevaContrasena: String,
        onResultado: (Boolean, String) -> Unit
    ) {
        val correoNormalizado = correo.trim()

        if (
            correoNormalizado.isBlank() ||
            !patronCorreo.matches(correoNormalizado)
        ) {
            onResultado(
                false,
                "Ingresa un correo electrónico válido."
            )
            return
        }

        if (!patronContrasena.matches(nuevaContrasena)) {
            onResultado(
                false,
                "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un símbolo."
            )
            return
        }

        val usuario = dbHelper.obtenerUsuarioPorCorreo(
            correoNormalizado
        )

        if (usuario == null) {
            onResultado(
                false,
                "El correo no se encuentra registrado en el sistema."
            )
            return
        }

        val nuevoHash = generarResumenContrasena(nuevaContrasena)
        val filas = dbHelper.actualizarPassword(
            correoNormalizado,
            nuevoHash
        )

        if (filas > 0) {
            onResultado(
                true,
                "Contraseña restablecida con éxito. Ya puedes iniciar sesión."
            )
        } else {
            onResultado(
                false,
                "No se pudo actualizar la contraseña en la base de datos."
            )
        }
    }

    fun limpiarFormulario() {
        _uiState.value = LoginUiState()
    }
}
