// induzteckApp/LoginViewModel.kt

package com.example.induzteckapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.induzteckapp.data.InduztekDbHelper
import com.example.induzteckapp.data.InduztekPreferencias
import com.example.induzteckapp.model.LoginUiState
import com.example.induzteckapp.model.RolUsuario
import com.example.induzteckapp.model.Usuario
import com.example.induzteckapp.util.generarResumenContrasena
import com.example.induzteckapp.util.verificarContrasena
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = InduztekDbHelper(application)
    private val preferencias = InduztekPreferencias(application)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val patronCorreo = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    // Mismo patrón estricto de contraseña utilizado en FormularioViewModel
    private val patronContrasena = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9\\s])\\S{8,}$")

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor, errorCorreo = null, mensajeError = null) }
    }

    fun onContrasenaCambia(valor: String) {
        _uiState.update { it.copy(contrasena = valor, errorContrasena = null, mensajeError = null) }
    }

    fun onAlternarVisibilidadContrasena() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    /**
     * Valida credenciales consultando SQLite y guarda la sesión en SharedPreferences
     */
    fun validarCredenciales(): Usuario? {
        val estado = _uiState.value
        val correoNormalizado = estado.correo.trim()

        val errorCorreo = when {
            correoNormalizado.isBlank() -> "Ingresa tu correo electrónico"
            !patronCorreo.matches(correoNormalizado) -> "Ingresa un correo electrónico válido"
            else -> null
        }

        val errorContrasena = if (estado.contrasena.isBlank()) "Ingresa tu contraseña" else null

        if (errorCorreo != null || errorContrasena != null) {
            _uiState.update { it.copy(errorCorreo = errorCorreo, errorContrasena = errorContrasena) }
            return null
        }

        var usuarioEncontrado: Usuario? = null
        val cursor = dbHelper.buscarUsuarioPorCorreo(correoNormalizado)

        cursor.use {
            if (it.moveToFirst()) {
                val id = it.getLong(it.getColumnIndexOrThrow("id_usuario"))
                val idRol = it.getLong(it.getColumnIndexOrThrow("id_rol"))
                val rut = it.getString(it.getColumnIndexOrThrow("rut"))
                val nombre = it.getString(it.getColumnIndexOrThrow("nombre"))
                val correo = it.getString(it.getColumnIndexOrThrow("correo"))
                val telefono = it.getString(it.getColumnIndexOrThrow("telefono"))
                val passwordHash = it.getString(it.getColumnIndexOrThrow("password_hash"))
                val activo = it.getInt(it.getColumnIndexOrThrow("activo")) == 1
                val avatarUri = it.getString(it.getColumnIndexOrThrow("avatar_uri"))

                val rolEnum = if (idRol == 1L) RolUsuario.TECNICO_INGENIERO else RolUsuario.CLIENTE

                val passwordValida = verificarContrasena(estado.contrasena, passwordHash)

                if (activo && passwordValida) {
                    usuarioEncontrado = Usuario(
                        idUsuario = id,
                        rol = rolEnum,
                        rut = rut,
                        nombre = nombre,
                        correo = correo,
                        telefono = telefono ?: "",
                        passwordHash = passwordHash,
                        activo = activo,
                        avatarUri = avatarUri
                    )
                    // Guardar sesión localmente
                    preferencias.guardarSesion(id)
                }
            }
        }

        if (usuarioEncontrado == null) {
            _uiState.update {
                it.copy(mensajeError = "Correo o contraseña incorrectos, o cuenta inactiva.")
            }
        } else {
            _uiState.update { it.copy(errorCorreo = null, errorContrasena = null, mensajeError = null) }
        }

        return usuarioEncontrado
    }

    /**
     * Función obligatoria: Recuperación / Restablecimiento de contraseña (HU-09)
     * Utiliza el mismo patrón y restricciones estrictas de contraseña que el registro.
     */
    fun recuperarContrasena(correo: String, nuevaContrasena: String, onResultado: (Boolean, String) -> Unit) {
        val correoNorm = correo.trim()
        if (correoNorm.isBlank() || !patronCorreo.matches(correoNorm)) {
            onResultado(false, "Ingresa un correo electrónico válido.")
            return
        }

        // Validación estricta unificada con el Regex de FormularioViewModel
        if (!patronContrasena.matches(nuevaContrasena)) {
            onResultado(false, "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un símbolo.")
            return
        }

        val cursor = dbHelper.buscarUsuarioPorCorreo(correoNorm)
        cursor.use {
            if (it.moveToFirst()) {
                val nuevoHash = generarResumenContrasena(nuevaContrasena)
                val filas = dbHelper.actualizarPassword(correoNorm, nuevoHash)
                if (filas > 0) {
                    onResultado(true, "Contraseña restablecida con éxito. Ya puedes iniciar sesión.")
                } else {
                    onResultado(false, "No se pudo actualizar la contraseña en la base de datos.")
                }
            } else {
                onResultado(false, "El correo no se encuentra registrado en el sistema.")
            }
        }
    }

    fun limpiarFormulario() {
        _uiState.value = LoginUiState()
    }
}