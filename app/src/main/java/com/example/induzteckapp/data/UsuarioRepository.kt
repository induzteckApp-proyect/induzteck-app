package com.example.induzteckapp.data

import android.content.Context
import com.example.induzteckapp.data.remote.*
import com.example.induzteckapp.model.Usuario
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class UsuarioRepository(context: Context) {
    private val appContext = context.applicationContext
    private val api = RetrofitClient.usuarioApi(appContext)
    private val avatarApi = RetrofitClient.avatarApi(appContext)
    private val preferencias = InduztekPreferencias(appContext)

    suspend fun iniciarSesion(correo: String, contrasena: String): Usuario {
        val respuesta = ejecutarApi { api.login(LoginRequest(correo.trim().lowercase(), contrasena)) }
        val usuario = respuesta.usuario.aUsuario()
        val idRemoto = usuario.idUsuario ?: throw IllegalStateException("El servidor no devolvió el identificador del usuario.")
        preferencias.guardarSesion(idRemoto, respuesta.token, respuesta.usuario.rol)
        return usuario
    }

    suspend fun obtenerMiPerfil(): Usuario = ejecutarApi { api.miPerfil() }.aUsuario()

    suspend fun actualizarPerfil(
        nombre: String, apellido: String, telefono: String,
        giro: String, contactoNombre: String, contactoCargo: String
    ): Usuario = ejecutarApi {
        api.actualizarPerfil(ActualizarPerfilRequest(
            nombre.trim(), apellido.trim().ifBlank { null }, telefono.trim(),
            giro.trim().ifBlank { null }, contactoNombre.trim().ifBlank { null },
            contactoCargo.trim().ifBlank { null }
        ))
    }.aUsuario()

    suspend fun cambiarContrasena(actual: String, nueva: String): String =
        ejecutarApi { api.cambiarContrasena(CambiarContrasenaRequest(actual, nueva)) }.mensaje

    suspend fun subirAvatar(bytes: ByteArray, mimeType: String = "image/jpeg"): Usuario = ejecutarApi {
        val body = bytes.toRequestBody(mimeType.toMediaType())
        val parte = MultipartBody.Part.createFormData("archivo", "avatar.jpg", body)
        avatarApi.subir(parte)
    }.aUsuario()

    suspend fun solicitarRecuperacion(correo: String): MensajeResponse =
        ejecutarApi { api.recuperar(RecuperarRequest(correo.trim().lowercase())) }

    suspend fun restablecerContrasena(token: String, nueva: String): String =
        ejecutarApi { api.restablecer(RestablecerRequest(token.trim(), nueva)) }.mensaje

    suspend fun activarCuenta(token: String, nueva: String): Usuario =
        ejecutarApi { api.activar(ActivarRequest(token.trim(), nueva)) }.aUsuario()

    suspend fun crearUsuario(
        rut: String, nombre: String, apellido: String, correo: String, telefono: String,
        rol: String, giro: String, contactoNombre: String, contactoCargo: String
    ): MensajeResponse = ejecutarApi {
        api.crearUsuario(CrearUsuarioRequest(
            rut.trim(), nombre.trim(), apellido.trim().ifBlank { null },
            correo.trim().lowercase(), telefono.trim(), rol,
            giro.trim().ifBlank { null }, contactoNombre.trim().ifBlank { null },
            contactoCargo.trim().ifBlank { null }
        ))
    }
}
