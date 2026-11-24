package com.example.induzteckapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.induzteckapp.data.UsuarioRepository
import com.example.induzteckapp.model.FormularioUiState
import com.example.induzteckapp.model.RolUsuario
import com.example.induzteckapp.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FormularioViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UsuarioRepository(application)
    private val _uiState = MutableStateFlow(FormularioUiState())
    val uiState: StateFlow<FormularioUiState> = _uiState.asStateFlow()

    private val patronNombre = Regex("^[\\p{L}][\\p{L} .'-]*$")
    private val patronCorreo = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val patronTelefono = Regex("^[0-9]{8,12}$")

    fun onRolSeleccionado(rol: RolUsuario) {
        _uiState.update { estado ->
            val esEmpresa = rol == RolUsuario.CLIENTE && estado.esEmpresa
            estado.copy(
                rol = rol,
                esEmpresa = esEmpresa,
                apellido = if (esEmpresa) "" else estado.apellido,
                giro = if (rol == RolUsuario.CLIENTE && esEmpresa) estado.giro else "",
                contactoNombre = if (rol == RolUsuario.CLIENTE && esEmpresa) estado.contactoNombre else "",
                contactoCargo = if (rol == RolUsuario.CLIENTE && esEmpresa) estado.contactoCargo else "",
                errorRol = null, errorGiro = null, errorContactoNombre = null, errorContactoCargo = null,
                registroExitoso = false, mensajeExito = null, mensajeError = null
            )
        }
    }

    fun onEsEmpresaCambia(valor: Boolean) {
        _uiState.update { estado ->
            val esEmpresa = valor && estado.rol == RolUsuario.CLIENTE
            estado.copy(
                esEmpresa = esEmpresa,
                apellido = if (esEmpresa) "" else estado.apellido,
                giro = if (esEmpresa) estado.giro else "",
                contactoNombre = if (esEmpresa) estado.contactoNombre else "",
                contactoCargo = if (esEmpresa) estado.contactoCargo else "",
                errorApellido = null, errorGiro = null, errorContactoNombre = null, errorContactoCargo = null,
                registroExitoso = false,
                mensajeError = null
            )
        }
    }

    fun onRutCambia(valor: String) = actualizar { copy(rut = valor, errorRut = null, registroExitoso = false) }
    fun onNombreCambia(valor: String) = actualizar { copy(nombre = valor, errorNombre = null, registroExitoso = false) }
    fun onApellidoCambia(valor: String) = actualizar { copy(apellido = valor, errorApellido = null, registroExitoso = false) }
    fun onGiroCambia(valor: String) = actualizar { copy(giro = valor, errorGiro = null, registroExitoso = false) }
    fun onContactoNombreCambia(valor: String) = actualizar { copy(contactoNombre = valor, errorContactoNombre = null, registroExitoso = false) }
    fun onContactoCargoCambia(valor: String) = actualizar { copy(contactoCargo = valor, errorContactoCargo = null, registroExitoso = false) }
    fun onCorreoCambia(valor: String) = actualizar { copy(correo = valor, errorCorreo = null, registroExitoso = false) }
    fun onTelefonoCambia(valor: String) = actualizar { copy(telefono = valor.filter(Char::isDigit), errorTelefono = null, registroExitoso = false) }

    fun onContrasenaCambia(valor: String) = Unit
    fun onConfirmarContrasenaCambia(valor: String) = Unit
    fun onAlternarVisibilidad() = Unit
    fun onAlternarVisibilidadConfirmacion() = Unit
    fun onAceptaTerminos(valor: Boolean) = Unit

    fun onLimpiar() { _uiState.value = FormularioUiState() }
    fun limpiarMensajeError() { _uiState.update { it.copy(mensajeError = null) } }

    fun onRegistrar() {
        val estado = _uiState.value
        _uiState.update { it.copy(mensajeError = null) }
        val rol = estado.rol
        val rutNormalizado = normalizarRut(estado.rut)
        val correoNormalizado = estado.correo.trim().lowercase()
        val requiereApellido = rol != null && !(rol == RolUsuario.CLIENTE && estado.esEmpresa)
        val requiereEmpresa = rol == RolUsuario.CLIENTE && estado.esEmpresa

        val erroresEmpresa = if (requiereEmpresa) validarDatosEmpresa(
            estado.giro, estado.contactoNombre, estado.contactoCargo
        ) else emptyMap()
        val errores = FormularioUiState(
            errorRol = if (rol == null) "Debe seleccionar un tipo de usuario." else null,
            errorRut = when {
                estado.rut.isBlank() -> "El RUT es obligatorio."
                !validarRut(estado.rut) -> "Ingrese un RUT válido, incluyendo su dígito verificador."
                else -> null
            },
            errorNombre = when {
                estado.nombre.isBlank() -> "El nombre o razón social es obligatorio."
                estado.nombre.trim().length !in 2..100 -> "Debe ingresar entre 2 y 100 caracteres."
                !patronNombre.matches(estado.nombre.trim()) -> "Use letras, espacios, puntos, apóstrofes o guiones."
                else -> null
            },
            errorApellido = when {
                requiereApellido && estado.apellido.isBlank() -> "El apellido es obligatorio."
                estado.apellido.isNotBlank() && estado.apellido.trim().length !in 2..100 -> "El apellido debe tener entre 2 y 100 caracteres."
                estado.apellido.isNotBlank() && !patronNombre.matches(estado.apellido.trim()) -> "Caracteres no permitidos."
                else -> null
            },
            errorCorreo = when {
                correoNormalizado.isBlank() -> "El correo electrónico es obligatorio."
                !patronCorreo.matches(correoNormalizado) -> "Ingrese un correo electrónico válido."
                else -> null
            },
            errorTelefono = when {
                estado.telefono.isBlank() -> "El teléfono es obligatorio."
                !patronTelefono.matches(estado.telefono) -> "Ingrese entre 8 y 12 dígitos."
                else -> null
            },
            errorGiro = erroresEmpresa["giro"],
            errorContactoNombre = erroresEmpresa["contactoNombre"],
            errorContactoCargo = erroresEmpresa["contactoCargo"]
        )
        val hayErrores = listOf(
            errores.errorRol, errores.errorRut, errores.errorNombre, errores.errorApellido,
            errores.errorCorreo, errores.errorTelefono, errores.errorGiro,
            errores.errorContactoNombre, errores.errorContactoCargo
        ).any { it != null }
        if (hayErrores) {
            _uiState.update { it.copy(
                errorRol = errores.errorRol, errorRut = errores.errorRut,
                errorNombre = errores.errorNombre, errorApellido = errores.errorApellido,
                errorCorreo = errores.errorCorreo, errorTelefono = errores.errorTelefono,
                errorGiro = errores.errorGiro, errorContactoNombre = errores.errorContactoNombre,
                errorContactoCargo = errores.errorContactoCargo, registroExitoso = false
            ) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, mensajeExito = null, registroExitoso = false) }
            try {
                val respuesta = repository.crearUsuario(
                    rut = rutNormalizado,
                    nombre = estado.nombre.trim(),
                    apellido = if (requiereApellido) estado.apellido.trim() else "",
                    correo = correoNormalizado,
                    telefono = estado.telefono,
                    rol = rol!!.codigo,
                    giro = if (requiereEmpresa) estado.giro else "",
                    contactoNombre = if (requiereEmpresa) estado.contactoNombre else "",
                    contactoCargo = if (requiereEmpresa) estado.contactoCargo else ""
                )
                _uiState.update { it.copy(
                    guardando = false, registroExitoso = true,
                    mensajeExito = respuesta.mensaje,

                    ) }
            } catch (e: Exception) {
                val mensaje = e.message ?: "No fue posible crear la cuenta."
                _uiState.update { it.copy(
                    guardando = false,
                    errorCorreo = if (mensaje.contains("correo", true) || mensaje.contains("registrado", true)) mensaje else null,
                    errorRut = if (mensaje.contains("RUT", true)) mensaje else null,
                    mensajeExito = null,
                    mensajeError = mensaje
                ) }
            }
        }
    }

    private fun actualizar(cambio: FormularioUiState.() -> FormularioUiState) {
        _uiState.update { cambio(it).copy(mensajeError = null) }
    }

    private fun normalizarRut(valor: String): String {
        val limpio = valor.trim().replace(".", "").replace("-", "").uppercase()
        return if (limpio.length < 2) limpio else "${limpio.dropLast(1)}-${limpio.last()}"
    }


    companion object {
        private val patronRut = Regex("^[0-9]{7,8}-?[0-9Kk]$")

        fun validarRut(valor: String): Boolean {
            val limpio = valor.trim().replace(".", "").replace("-", "").uppercase()
            if (!patronRut.matches(limpio)) return false
            val cuerpo = limpio.dropLast(1)
            var suma = 0
            var factor = 2
            for (digito in cuerpo.reversed()) {
                suma += digito.digitToInt() * factor
                factor = if (factor == 7) 2 else factor + 1
            }
            val dvCalculado = when (val resultado = 11 - suma % 11) {
                11 -> '0'
                10 -> 'K'
                else -> resultado.digitToChar()
            }
            return limpio.last() == dvCalculado
        }

        fun validarDatosEmpresa(giro: String, contactoNombre: String, contactoCargo: String): Map<String, String> {
            val errores = mutableMapOf<String, String>()
            val giroLimpio = giro.trim()
            val nombreLimpio = contactoNombre.trim()
            val cargoLimpio = contactoCargo.trim()

            if (giroLimpio.isNotBlank() &&
                (giroLimpio.length !in 2..150 || !Regex("^[\\p{L}][\\p{L} ]*$").matches(giroLimpio))) {
                errores["giro"] = "Ingresa un giro comercial válido."
            }

            if (nombreLimpio.isNotBlank() &&
                (nombreLimpio.length !in 2..120 || !Regex("^[\\p{L}][\\p{L} .'-]*$").matches(nombreLimpio))) {
                errores["contactoNombre"] = "Ingresa un nombre de contacto válido."
            }

            if (cargoLimpio.isNotBlank() &&
                (cargoLimpio.length !in 2..120 || !Regex("^[\\p{L}][\\p{L} .'-]*$").matches(cargoLimpio))) {
                errores["contactoCargo"] = "Ingresa un cargo válido."
            }
            return errores
        }
    }
}
