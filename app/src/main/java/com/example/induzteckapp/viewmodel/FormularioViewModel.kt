// induzteckApp/FormularioViewModel.kt
// Corresponde al Registro de Usuarios

package com.example.induzteckapp.viewmodel

import android.app.Application
import android.content.ContentValues
import androidx.lifecycle.AndroidViewModel
import com.example.induzteckapp.data.InduztekDbHelper
import com.example.induzteckapp.model.FormularioUiState
import com.example.induzteckapp.model.RolUsuario
import com.example.induzteckapp.model.Usuario
import com.example.induzteckapp.util.generarResumenContrasena
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FormularioViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = InduztekDbHelper(application)

    private val _uiState = MutableStateFlow(FormularioUiState())
    val uiState: StateFlow<FormularioUiState> = _uiState.asStateFlow()

    private val patronNombre = Regex("^[\\p{L}]+(?:[ '\\-][\\p{L}]+)*$")
    private val patronCorreo = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val patronTelefono = Regex("^[0-9]{8,12}$")
    private val patronRut = Regex("^[0-9]{7,8}-?[0-9Kk]$")
    private val patronContrasena = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9\\s])\\S{8,}$")

    fun onRolSeleccionado(rol: RolUsuario) {
        _uiState.update { estado ->
            val esTecnico = rol == RolUsuario.TECNICO_INGENIERO
            estado.copy(
                rol = rol,
                errorRol = null,
                esEmpresa = if (esTecnico) false else estado.esEmpresa,
                giro = if (esTecnico) "" else estado.giro,
                contactoNombre = if (esTecnico) "" else estado.contactoNombre,
                contactoCargo = if (esTecnico) "" else estado.contactoCargo,
                errorGiro = null,
                errorContactoNombre = null,
                errorContactoCargo = null,
                errorApellido = if (esTecnico || estado.esEmpresa) null else estado.errorApellido,
                registroExitoso = false
            )
        }
    }

    fun onEsEmpresaCambia(valor: Boolean) {
        _uiState.update { estado ->
            val esEmpresaValida = valor && estado.rol == RolUsuario.CLIENTE
            estado.copy(
                esEmpresa = esEmpresaValida,
                apellido = if (esEmpresaValida) "" else estado.apellido,
                giro = if (esEmpresaValida) estado.giro else "",
                contactoNombre = if (esEmpresaValida) estado.contactoNombre else "",
                contactoCargo = if (esEmpresaValida) estado.contactoCargo else "",
                errorApellido = null,
                errorGiro = null,
                errorContactoNombre = null,
                errorContactoCargo = null,
                registroExitoso = false
            )
        }
    }

    fun onRutCambia(valor: String) {
        _uiState.update { it.copy(rut = valor, errorRut = null, registroExitoso = false) }
    }

    fun onNombreCambia(valor: String) {
        _uiState.update { it.copy(nombre = valor, errorNombre = null, registroExitoso = false) }
    }

    fun onApellidoCambia(valor: String) {
        _uiState.update { it.copy(apellido = valor, errorApellido = null, registroExitoso = false) }
    }

    fun onGiroCambia(valor: String) {
        _uiState.update { it.copy(giro = valor, errorGiro = null, registroExitoso = false) }
    }

    fun onContactoNombreCambia(valor: String) {
        _uiState.update { it.copy(contactoNombre = valor, errorContactoNombre = null, registroExitoso = false) }
    }

    fun onContactoCargoCambia(valor: String) {
        _uiState.update { it.copy(contactoCargo = valor, errorContactoCargo = null, registroExitoso = false) }
    }

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor, errorCorreo = null, registroExitoso = false) }
    }

    fun onTelefonoCambia(valor: String) {
        val soloDigitos = valor.filter { it in '0'..'9' }
        _uiState.update { it.copy(telefono = soloDigitos, errorTelefono = null, registroExitoso = false) }
    }

    fun onContrasenaCambia(valor: String) {
        _uiState.update {
            it.copy(
                contrasena = valor,
                errorContrasena = null,
                errorConfirmarContrasena = if (valor == it.confirmarContrasena) null else it.errorConfirmarContrasena,
                registroExitoso = false
            )
        }
    }

    fun onConfirmarContrasenaCambia(valor: String) {
        _uiState.update {
            it.copy(
                confirmarContrasena = valor,
                errorConfirmarContrasena = null,
                registroExitoso = false
            )
        }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    fun onAlternarVisibilidadConfirmacion() {
        _uiState.update { it.copy(confirmarContrasenaVisible = !it.confirmarContrasenaVisible) }
    }

    fun onAceptaTerminos(valor: Boolean) {
        _uiState.update { it.copy(aceptaTerminos = valor, errorTermino = null, registroExitoso = false) }
    }

    fun onLimpiar() {
        _uiState.value = FormularioUiState()
    }

    private fun validarRut(rutIngresado: String): Boolean {
        val rut = rutIngresado.trim().replace(".", "").uppercase()
        if (!patronRut.matches(rut)) return false
        val rutSinGuion = rut.replace("-", "")
        val cuerpo = rutSinGuion.dropLast(1)
        val digitoVerificadorIngresado = rutSinGuion.last()
        if (!cuerpo.all { it in '0'..'9' }) return false

        var suma = 0
        var factor = 2
        for (digito in cuerpo.reversed()) {
            suma += digito.digitToInt() * factor
            factor++
            if (factor > 7) factor = 2
        }
        val resto = suma % 11
        val resultado = 11 - resto
        val digitoVerificadorCalculado = when (resultado) {
            11 -> '0'
            10 -> 'K'
            else -> resultado.digitToChar()
        }
        return digitoVerificadorIngresado == digitoVerificadorCalculado
    }

    /**
     * Valida y registra el usuario directamente en SQLite
     */
    fun onRegistrar(): Usuario? {
        val estado = _uiState.value

        val errorRol = if (estado.rol == null) "Debe seleccionar un tipo de usuario" else null
        val errorRut = when {
            estado.rut.isBlank() -> "El RUT es obligatorio"
            !validarRut(estado.rut) -> "Ingrese un RUT válido, incluyendo su dígito verificador"
            else -> null
        }
        val errorNombre = when {
            estado.nombre.isBlank() -> "El nombre o razón social es obligatorio"
            estado.nombre.trim().length < 2 -> "Debe ingresar al menos 2 caracteres"
            !patronNombre.matches(estado.nombre.trim()) -> "Use solo letras, espacios o apóstrofes"
            else -> null
        }
        val errorApellido = when {
            estado.rol == RolUsuario.TECNICO_INGENIERO && !estado.esEmpresa && estado.apellido.isBlank() -> "El apellido es obligatorio"
            estado.rol == RolUsuario.CLIENTE && !estado.esEmpresa && estado.apellido.isBlank() -> "El apellido es obligatorio"
            estado.apellido.isNotBlank() && !patronNombre.matches(estado.apellido.trim()) -> "Caracteres no permitidos"
            else -> null
        }
        val correoNormalizado = estado.correo.trim()
        val errorCorreo = when {
            correoNormalizado.isBlank() -> "El correo electrónico es obligatorio"
            !patronCorreo.matches(correoNormalizado) -> "Ingrese un correo electrónico válido"
            else -> null
        }
        val errorTelefono = when {
            estado.telefono.isBlank() -> "El teléfono es obligatorio"
            !patronTelefono.matches(estado.telefono) -> "Ingrese entre 8 y 12 dígitos"
            else -> null
        }
        val requiereDatosEmpresa = estado.rol == RolUsuario.CLIENTE && estado.esEmpresa
        val errorGiro = if (requiereDatosEmpresa && estado.giro.isBlank()) "El giro es obligatorio" else null
        val errorContactoNombre = if (requiereDatosEmpresa && estado.contactoNombre.isBlank()) "Nombre de contacto obligatorio" else null
        val errorContactoCargo = if (requiereDatosEmpresa && estado.contactoCargo.isBlank()) "Cargo obligatorio" else null

        val errorContrasena = when {
            estado.contrasena.isBlank() -> "La contraseña es obligatoria"
            !patronContrasena.matches(estado.contrasena) -> "Use al menos 8 caracteres, mayúscula, minúscula, número y símbolo"
            else -> null
        }
        val errorConfirmarContrasena = if (estado.contrasena != estado.confirmarContrasena) "Las contraseñas no coinciden" else null
        val errorTermino = if (!estado.aceptaTerminos) "Debe aceptar los términos y condiciones" else null

        val hayErrores = listOf(errorRol, errorRut, errorNombre, errorApellido, errorCorreo, errorTelefono, errorGiro, errorContactoNombre, errorContactoCargo, errorContrasena, errorConfirmarContrasena, errorTermino).any { it != null }

        _uiState.update {
            it.copy(
                errorRol = errorRol, errorRut = errorRut, errorNombre = errorNombre,
                errorApellido = errorApellido, errorCorreo = errorCorreo, errorTelefono = errorTelefono,
                errorGiro = errorGiro, errorContactoNombre = errorContactoNombre, errorContactoCargo = errorContactoCargo,
                errorContrasena = errorContrasena, errorConfirmarContrasena = errorConfirmarContrasena,
                errorTermino = errorTermino, registroExitoso = !hayErrores
            )
        }

        if (hayErrores) return null

        // Inserción en SQLite
        val idRolDb = if (estado.rol == RolUsuario.TECNICO_INGENIERO) 1L else 2L
        val nombreCompleto = if (estado.apellido.isNotBlank()) "${estado.nombre.trim()} ${estado.apellido.trim()}" else estado.nombre.trim()
        val passwordHash = generarResumenContrasena(estado.contrasena)

        val valores = ContentValues().apply {
            put("id_rol", idRolDb)
            put("rut", estado.rut.trim().uppercase())
            put("nombre", nombreCompleto)
            put("correo", correoNormalizado)
            put("telefono", estado.telefono)
            put("password_hash", passwordHash)
            put("activo", 1)
            if (estado.rol == RolUsuario.CLIENTE) {
                put("giro", estado.giro.trim().ifBlank { null })
                put("contacto_nombre", estado.contactoNombre.trim().ifBlank { null })
                put("contacto_cargo", estado.contactoCargo.trim().ifBlank { null })
            }
        }

        return try {
            val db = dbHelper.writableDatabase
            val idGenerado = db.insert(InduztekDbHelper.TABLA_USUARIO, null, valores)
            if (idGenerado != -1L) {
                Usuario(
                    idUsuario = idGenerado,
                    rol = estado.rol!!,
                    rut = estado.rut.trim().uppercase(),
                    nombre = estado.nombre.trim(),
                    apellido = estado.apellido.trim().ifBlank { null },
                    correo = correoNormalizado,
                    telefono = estado.telefono,
                    passwordHash = passwordHash,
                    activo = true
                )
            } else {
                _uiState.update { it.copy(errorCorreo = "El correo o RUT ya se encuentran registrados.") }
                null
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(errorCorreo = "Error al guardar en la base de datos.") }
            null
        }
    }
}
