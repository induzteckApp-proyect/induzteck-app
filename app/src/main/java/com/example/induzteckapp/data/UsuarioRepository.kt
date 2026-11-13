package com.example.induzteckapp.data

import android.content.Context
import com.example.induzteckapp.data.remote.*
import com.example.induzteckapp.model.Usuario
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class UsuarioRepository(context: Context) {
    private val appContext = context.applicationContext
    private val api = RetrofitClient.usuarioApi(appContext)
    private val avatarApi = RetrofitClient.avatarApi(appContext)
    private val preferencias = InduztekPreferencias(appContext)

    suspend fun iniciarSesion(correo: String, contrasena: String): Usuario {
        val respuesta = ejecutar { api.login(LoginRequest(correo.trim().lowercase(), contrasena)) }
        // Crea/resuelve una referencia local sin guardar contraseña ni autenticar en SQLite.
        // Se necesita temporalmente para las FK de protocolos/equipos que aún no migran.
        val idLocal = resolverIdLocal(respuesta.usuario.aUsuario())
        preferencias.guardarSesion(idLocal, respuesta.token, respuesta.usuario.rol)
        return respuesta.usuario.aUsuario().copy(idUsuario = idLocal)
    }

    suspend fun obtenerMiPerfil(): Usuario {
        val usuario = ejecutar { api.miPerfil() }.aUsuario()
        val idLocal = resolverIdLocal(usuario)
        return usuario.copy(idUsuario = idLocal)
    }

    suspend fun actualizarPerfil(
        nombre: String, apellido: String, telefono: String,
        giro: String, contactoNombre: String, contactoCargo: String
    ): Usuario {
        val usuario = ejecutar {
            api.actualizarPerfil(
                ActualizarPerfilRequest(
                    nombre.trim(), apellido.trim().ifBlank { null }, telefono.trim(),
                    giro.trim().ifBlank { null }, contactoNombre.trim().ifBlank { null },
                    contactoCargo.trim().ifBlank { null }
                )
            )
        }.aUsuario()
        val idLocal = resolverIdLocal(usuario)
        return usuario.copy(idUsuario = idLocal)
    }

    suspend fun cambiarContrasena(actual: String, nueva: String): String =
        ejecutar { api.cambiarContrasena(CambiarContrasenaRequest(actual, nueva)) }.mensaje

    suspend fun subirAvatar(bytes: ByteArray, mimeType: String = "image/jpeg"): Usuario =
        ejecutar {
            val body = bytes.toRequestBody(mimeType.toMediaType())
            val parte = MultipartBody.Part.createFormData("archivo", "avatar.jpg", body)
            avatarApi.subir(parte)
        }.aUsuario()

    suspend fun solicitarRecuperacion(correo: String): MensajeResponse =
        ejecutar { api.recuperar(RecuperarRequest(correo.trim().lowercase())) }

    suspend fun restablecerContrasena(token: String, nueva: String): String =
        ejecutar { api.restablecer(RestablecerRequest(token.trim(), nueva)) }.mensaje

    suspend fun activarCuenta(token: String, nueva: String): Usuario =
        ejecutar { api.activar(ActivarRequest(token.trim(), nueva)) }.aUsuario()

    suspend fun crearUsuario(
        rut: String, nombre: String, apellido: String, correo: String, telefono: String,
        rol: String, giro: String, contactoNombre: String, contactoCargo: String
    ): MensajeResponse = ejecutar {
        api.crearUsuario(
            CrearUsuarioRequest(
                rut.trim(), nombre.trim(), apellido.trim().ifBlank { null },
                correo.trim().lowercase(), telefono.trim(), rol,
                giro.trim().ifBlank { null }, contactoNombre.trim().ifBlank { null },
                contactoCargo.trim().ifBlank { null }
            )
        )
    }

    private fun resolverIdLocal(usuario: Usuario): Long {
        return runCatching {
            UsuarioReferenciaLocal(appContext).obtenerOCrear(usuario)
        }.getOrDefault(usuario.idUsuario ?: -1L)
    }

    private suspend fun <T> ejecutar(llamada: suspend () -> T): T {
        try {
            return llamada()
        } catch (e: CancellationException) {
            throw e
        } catch (e: retrofit2.HttpException) {
            val cuerpo = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
            val mensaje = runCatching {
                Gson().fromJson(cuerpo, ErrorApiResponse::class.java)?.message
            }.getOrNull()
            throw IllegalStateException(mensaje ?: when (e.code()) {
                401 -> "La sesión no es válida o las credenciales son incorrectas."
                403 -> "No tienes permisos para realizar esta operación."
                404 -> "No se encontró el recurso solicitado."
                409 -> "El registro ya existe o no puede modificarse en este estado."
                else -> "Error de comunicación con el servidor (${e.code()})."
            })
        } catch (e: java.io.IOException) {
            throw IllegalStateException("No fue posible conectar con Induztek. Comprueba que el backend esté iniciado y que la URL sea correcta.")
        }
    }
}
