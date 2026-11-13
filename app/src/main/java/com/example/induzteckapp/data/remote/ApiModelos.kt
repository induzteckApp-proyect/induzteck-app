package com.example.induzteckapp.data.remote

import com.example.induzteckapp.model.RolUsuario
import com.example.induzteckapp.model.Usuario

data class LoginRequest(val correo: String, val contrasena: String)
data class CrearUsuarioRequest(
    val rut: String, val nombre: String, val apellido: String?,
    val correo: String, val telefono: String, val rol: String,
    val giro: String?, val contactoNombre: String?, val contactoCargo: String?
)
data class ActualizarPerfilRequest(
    val nombre: String, val apellido: String?, val telefono: String,
    val giro: String?, val contactoNombre: String?, val contactoCargo: String?
)
data class CambiarContrasenaRequest(val actual: String, val nueva: String)
data class RecuperarRequest(val correo: String)
data class RestablecerRequest(val token: String, val nuevaContrasena: String)
data class ActivarRequest(val token: String, val nuevaContrasena: String)

data class UsuarioResponse(
    val id: Long,
    val rut: String,
    val nombre: String,
    val apellido: String?,
    val correo: String,
    val telefono: String,
    val rol: String,
    val activo: Boolean,
    val giro: String?,
    val contactoNombre: String?,
    val contactoCargo: String?,
    val avatarUrl: String?,
    val creadoEn: String?
) {
    fun aUsuario() = Usuario(
        idUsuario = id,
        rol = runCatching { RolUsuario.valueOf(rol) }.getOrDefault(RolUsuario.CLIENTE),
        rut = rut, nombre = nombre, apellido = apellido, correo = correo,
        telefono = telefono, passwordHash = "", activo = activo,
        giro = giro, contactoNombre = contactoNombre, contactoCargo = contactoCargo,
        avatarUrl = avatarUrl
    )
}

data class AuthResponse(val token: String, val tipo: String, val usuario: UsuarioResponse)
data class MensajeResponse(val mensaje: String, val tokenDesarrollo: String? = null)
data class ErrorApiResponse(val message: String? = null, val error: String? = null)
