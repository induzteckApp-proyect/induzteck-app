// com.example.induzteckapp/data/InduztekPreferencias
package com.example.induzteckapp.data

import android.content.Context
import androidx.core.content.edit

class InduztekPreferencias(context: Context) {

    private val prefs = context.getSharedPreferences("induztek_sesion", Context.MODE_PRIVATE)

    companion object {
        private const val CLAVE_ID_USUARIO = "id_usuario"
        private const val SIN_SESION = -1L
    }

    fun guardarSesion(idUsuario: Long) {
        prefs.edit { putLong(CLAVE_ID_USUARIO, idUsuario) }
    }

    fun leerSesion(): Long = prefs.getLong(CLAVE_ID_USUARIO, SIN_SESION)

    fun borrarSesion() {
        prefs.edit { remove(CLAVE_ID_USUARIO) }
    }
}