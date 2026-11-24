package com.example.induzteckapp.data.remote

import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> ejecutarApi(llamada: suspend () -> T): T {
    try {
        return llamada()
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        val cuerpo = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
        val mensaje = runCatching { Gson().fromJson(cuerpo, ErrorApiResponse::class.java)?.message }.getOrNull()
        throw IllegalStateException(mensaje ?: when (e.code()) {
            400 -> "La solicitud contiene datos inválidos."
            401 -> "La sesión no es válida o las credenciales son incorrectas."
            403 -> "No tienes permisos para realizar esta operación."
            404 -> "No se encontró el recurso solicitado."
            409 -> "El registro ya existe o no puede modificarse en este estado."
            else -> "Error de comunicación con el servidor (${e.code()})."
        }, e)
    } catch (e: IOException) {
        throw IllegalStateException("No fue posible conectar con Induztek. Comprueba que el backend esté iniciado y que la URL sea correcta.", e)
    }
}
