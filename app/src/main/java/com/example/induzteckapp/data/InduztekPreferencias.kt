
package com.example.induzteckapp.data

import android.content.Context
import androidx.core.content.edit

class InduztekPreferencias(context: Context) {
    private val prefs = context.getSharedPreferences("induztek_sesion", Context.MODE_PRIVATE)
    private val tokenSeguro = TokenSeguroStorage(context)

    companion object {
        private const val CLAVE_ID_USUARIO = "id_usuario"
        private const val CLAVE_ROL = "rol_usuario"
        private const val SIN_SESION = -1L
    }

    // Compatibilidad con pantallas antiguas que todavía consultan el ID de sesión.
    fun guardarSesion(idUsuario: Long) {
        prefs.edit { putLong(CLAVE_ID_USUARIO, idUsuario) }
    }

    fun guardarSesion(idUsuario: Long, token: String, rol: String) {
        prefs.edit {
            putLong(CLAVE_ID_USUARIO, idUsuario)
            putString(CLAVE_ROL, rol)
            remove("jwt_token")
        }
        tokenSeguro.guardarToken(token)
    }

    fun leerSesion(): Long = prefs.getLong(CLAVE_ID_USUARIO, SIN_SESION)
    fun leerToken(): String? = tokenSeguro.leerToken()
    fun leerRol(): String? = prefs.getString(CLAVE_ROL, null)

    fun borrarSesion() {
        prefs.edit {
            remove(CLAVE_ID_USUARIO)
            remove(CLAVE_ROL)
            remove("jwt_token") // limpia el token en texto plano de versiones anteriores
        }
        tokenSeguro.borrarToken()
    }
}
