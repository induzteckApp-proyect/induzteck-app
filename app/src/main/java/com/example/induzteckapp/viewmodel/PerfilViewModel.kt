package com.example.induzteckapp.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.induzteckapp.data.ConvertidorImagen
import com.example.induzteckapp.data.InduztekDbHelper
import com.example.induzteckapp.data.InduztekPreferencias
import com.example.induzteckapp.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PerfilViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = InduztekDbHelper(application)
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

    private val _telefono = MutableStateFlow("")
    val telefono: StateFlow<String> = _telefono.asStateFlow()

    private val _avatarBytes = MutableStateFlow<ByteArray?>(null)
    val avatarBytes: StateFlow<ByteArray?> = _avatarBytes.asStateFlow()

    private val _mensajeState = MutableStateFlow<String?>(null)
    val mensajeState: StateFlow<String?> = _mensajeState.asStateFlow()

    private var idUsuarioActivo: Long = -1L

    init {
        cargarPerfil()
    }

    fun cargarPerfil() {
        idUsuarioActivo = preferencias.leerSesion()
        if (idUsuarioActivo == -1L) return

        viewModelScope.launch(Dispatchers.IO) {
            val user = dbHelper.obtenerUsuarioPorId(idUsuarioActivo)
            if (user != null) {
                _usuarioState.value = user
                _nombre.value = user.nombre
                _apellido.value = user.apellido ?: ""
                _giro.value = user.giro ?: ""
                _contactoNombre.value = user.contactoNombre ?: ""
                _telefono.value = user.telefono
                _avatarBytes.value = user.avatar
            }
        }
    }

    fun onNombreChanged(nuevo: String) { _nombre.value = nuevo }
    fun onApellidoChanged(nuevo: String) { _apellido.value = nuevo }
    fun onGiroChanged(nuevo: String) { _giro.value = nuevo }
    fun onContactoNombreChanged(nuevo: String) { _contactoNombre.value = nuevo }
    fun onTelefonoChanged(nuevo: String) { _telefono.value = nuevo }

    fun limpiarMensaje() {
        _mensajeState.value = null
    }

    fun onFotoCapturada(bitmap: Bitmap) {
        if (idUsuarioActivo == -1L) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bytes = ConvertidorImagen.aBytes(bitmap)
                _avatarBytes.value = bytes
                dbHelper.actualizarAvatarBlob(idUsuarioActivo, bytes)
                _mensajeState.value = "Avatar actualizado correctamente desde la cámara."
            } catch (_: Exception) {
                _mensajeState.value = "Error al guardar la foto de la cámara."
            }
        }
    }

    fun onImagenSeleccionada(context: Context, uri: Uri) {
        if (idUsuarioActivo == -1L) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bitmap = ConvertidorImagen.desdeUri(context, uri)
                if (bitmap != null) {
                    val bytes = ConvertidorImagen.aBytes(bitmap)
                    _avatarBytes.value = bytes
                    dbHelper.actualizarAvatarBlob(idUsuarioActivo, bytes)
                    _mensajeState.value = "Avatar actualizado correctamente desde la galería."
                } else {
                    _mensajeState.value = "No se pudo procesar la imagen seleccionada."
                }
            } catch (_: Exception) {
                _mensajeState.value = "Error al procesar la imagen seleccionada."
            }
        }
    }

    fun guardarCambiosPerfil() {
        if (idUsuarioActivo == -1L) return

        viewModelScope.launch(Dispatchers.IO) {
            val actualizados = dbHelper.actualizarPerfilUsuario(
                idUsuario = idUsuarioActivo,
                nombre = _nombre.value.trim(),
                apellido = _apellido.value.trim().ifEmpty { null },
                giro = _giro.value.trim().ifEmpty { null },
                contactoNombre = _contactoNombre.value.trim().ifEmpty { null },
                telefono = _telefono.value.trim()
            )

            if (actualizados > 0) {
                _mensajeState.value = "Perfil actualizado con éxito."
                cargarPerfil()
            } else {
                _mensajeState.value = "No se pudieron guardar los cambios."
            }
        }
    }
}
