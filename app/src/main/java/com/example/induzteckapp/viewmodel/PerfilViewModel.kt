package com.example.induzteckapp.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.induzteckapp.data.ConvertidorImagen
import com.example.induzteckapp.data.InduztekPreferencias
import com.example.induzteckapp.data.UsuarioRepository
import com.example.induzteckapp.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PerfilViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UsuarioRepository(application)
    private val preferencias = InduztekPreferencias(application)

    private val _usuarioState = MutableStateFlow<Usuario?>(null)
    val usuarioState: StateFlow<Usuario?> = _usuarioState.asStateFlow()
    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre.asStateFlow()
    private val _apellido = MutableStateFlow("")
    val apellido: StateFlow<String> = _apellido.asStateFlow()
    private val _giro = MutableStateFlow("")
    val giro: StateFlow<String> = _giro.asStateFlow()
    private val _contactoNombre = MutableStateFlow("")
    val contactoNombre: StateFlow<String> = _contactoNombre.asStateFlow()
    private val _contactoCargo = MutableStateFlow("")
    val contactoCargo: StateFlow<String> = _contactoCargo.asStateFlow()
    private val _telefono = MutableStateFlow("")
    val telefono: StateFlow<String> = _telefono.asStateFlow()
    private val _avatarBytes = MutableStateFlow<ByteArray?>(null)
    val avatarBytes: StateFlow<ByteArray?> = _avatarBytes.asStateFlow()
    private val _mensajeState = MutableStateFlow<String?>(null)
    val mensajeState: StateFlow<String?> = _mensajeState.asStateFlow()
    private val _errorNombre = MutableStateFlow<String?>(null)
    val errorNombre: StateFlow<String?> = _errorNombre.asStateFlow()
    private val _errorApellido = MutableStateFlow<String?>(null)
    val errorApellido: StateFlow<String?> = _errorApellido.asStateFlow()
    private val _errorTelefono = MutableStateFlow<String?>(null)
    val errorTelefono: StateFlow<String?> = _errorTelefono.asStateFlow()
    private val _errorContacto = MutableStateFlow<String?>(null)
    val errorContacto: StateFlow<String?> = _errorContacto.asStateFlow()
    private val _errorGiro = MutableStateFlow<String?>(null)
    val errorGiro: StateFlow<String?> = _errorGiro.asStateFlow()
    private val _errorContactoCargo = MutableStateFlow<String?>(null)
    val errorContactoCargo: StateFlow<String?> = _errorContactoCargo.asStateFlow()

    private val patronTexto = Regex("^[\\p{L}][\\p{L} .'-]*$")
    private val patronTelefono = Regex("^[0-9]{8,12}$")
    private val patronGiro = Regex("^[\\p{L}\\p{N}][\\p{L}\\p{N} .,'&()/-]*$")
    private val patronClave = Regex("^(?=.*\\p{Lu})(?=.*\\p{Ll})(?=.*[0-9])(?=.*[!@#$%&*()_+\\-=\\[\\]{};':\\\\|,.<>/?¿¡~^])\\S{8,72}$")

    init { cargarPerfil() }

    fun cargarPerfil() {
        if (preferencias.leerSesion() <= 0L || preferencias.leerToken().isNullOrBlank()) {
            _mensajeState.value = "No hay una sesión válida. Inicia sesión nuevamente."
            return
        }
        viewModelScope.launch {
            try {
                val user = repository.obtenerMiPerfil()
                _usuarioState.value = user
                _nombre.value = user.nombre
                _apellido.value = user.apellido.orEmpty()
                _giro.value = user.giro.orEmpty()
                _contactoNombre.value = user.contactoNombre.orEmpty()
                _contactoCargo.value = user.contactoCargo.orEmpty()
                _telefono.value = user.telefono
                _avatarBytes.value = null
            } catch (e: Exception) {
                _mensajeState.value = e.message ?: "No se pudo cargar el perfil."
            }
        }
    }

    fun onNombreChanged(v: String) { _nombre.value=v; _errorNombre.value=null }
    fun onApellidoChanged(v: String) { _apellido.value=v; _errorApellido.value=null }
    fun onGiroChanged(v: String) { _giro.value=v }
    fun onContactoNombreChanged(v: String) { _contactoNombre.value=v; _errorContacto.value=null }
    fun onContactoCargoChanged(v: String) { _contactoCargo.value=v }
    fun onTelefonoChanged(v: String) { _telefono.value=v.filter(Char::isDigit); _errorTelefono.value=null }
    fun limpiarMensaje() { _mensajeState.value=null }

    fun guardarCambiosPerfil() {
        val nombre = _nombre.value.trim()
        val apellido = _apellido.value.trim()
        val telefono = _telefono.value.trim()
        _errorNombre.value = when {
            nombre.isBlank() -> "El nombre es obligatorio."
            nombre.length < 2 || !patronTexto.matches(nombre) -> "Ingresa un nombre válido, solo con letras."
            else -> null
        }
        _errorApellido.value = if (apellido.isNotBlank() && !patronTexto.matches(apellido))
            "Ingresa un apellido válido." else null
        _errorTelefono.value = if (!patronTelefono.matches(telefono))
            "El teléfono debe contener entre 8 y 12 dígitos." else null
        _errorContacto.value = if (_contactoNombre.value.isNotBlank() &&
            !patronTexto.matches(_contactoNombre.value.trim())) "El nombre de contacto no es válido." else null
        val esCliente = _usuarioState.value?.rol == com.example.induzteckapp.model.RolUsuario.CLIENTE
        _errorGiro.value = when {
            esCliente && _giro.value.isBlank() -> "El giro es obligatorio."
            _giro.value.isNotBlank() && !patronGiro.matches(_giro.value.trim()) -> "El giro contiene caracteres no permitidos."
            else -> null
        }
        _errorContactoCargo.value = when {
            esCliente && _contactoCargo.value.isBlank() -> "El cargo del contacto es obligatorio."
            _contactoCargo.value.isNotBlank() && !patronTexto.matches(_contactoCargo.value.trim()) -> "El cargo contiene caracteres no permitidos."
            else -> null
        }
        if (esCliente && _contactoNombre.value.isBlank()) {
            _errorContacto.value = "El nombre de contacto es obligatorio."
        }
        if (listOf(_errorNombre.value,_errorApellido.value,_errorTelefono.value,_errorContacto.value,_errorGiro.value,_errorContactoCargo.value).any { it!=null }) return

        viewModelScope.launch {
            try {
                val actualizado = repository.actualizarPerfil(
                    nombre, apellido, telefono, _giro.value,
                    _contactoNombre.value, _contactoCargo.value
                )
                _usuarioState.value=actualizado
                _mensajeState.value="Perfil actualizado correctamente."
            } catch(e:Exception) { _mensajeState.value=e.message ?: "No se pudo guardar el perfil." }
        }
    }

    fun cambiarContrasena(actual: String, nueva: String, confirmar: String) {
        when {
            actual.isBlank() -> _mensajeState.value="Ingresa tu contraseña actual."
            !patronClave.matches(nueva) -> _mensajeState.value="La nueva contraseña debe tener 8 a 72 caracteres, mayúscula, minúscula, número y símbolo."
            nueva != confirmar -> _mensajeState.value="La confirmación no coincide."
            actual == nueva -> _mensajeState.value="La nueva contraseña debe ser distinta de la actual."
            else -> viewModelScope.launch {
                try {
                    _mensajeState.value=repository.cambiarContrasena(actual,nueva)
                } catch(e:Exception) { _mensajeState.value=e.message ?: "No se pudo cambiar la contraseña." }
            }
        }
    }

    fun onFotoCapturada(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                val bytes = ConvertidorImagen.aBytes(bitmap)
                _avatarBytes.value = bytes
                val actualizado = repository.subirAvatar(bytes)
                _usuarioState.value = actualizado
                _avatarBytes.value = null
                _mensajeState.value = "Avatar actualizado en el servidor."
            } catch (e: Exception) {
                _mensajeState.value = e.message ?: "No se pudo subir el avatar."
            }
        }
    }

    fun onImagenSeleccionada(context: android.content.Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val bitmap = ConvertidorImagen.desdeUri(context, uri)
                if (bitmap != null) {
                    val bytes = ConvertidorImagen.aBytes(bitmap)
                    _avatarBytes.value = bytes
                    val actualizado = repository.subirAvatar(bytes)
                    _usuarioState.value = actualizado
                    _avatarBytes.value = null
                    _mensajeState.value = "Avatar actualizado en el servidor."
                } else {
                    _mensajeState.value = "No se pudo procesar la imagen."
                }
            } catch (e: Exception) {
                _mensajeState.value = e.message ?: "No se pudo subir el avatar."
            }
        }
    }
}
