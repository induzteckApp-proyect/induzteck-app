package com.example.induzteckapp.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reutiliza la misma operación de cambio de estado desde Historial y ListarClientes.
 * Las reglas definitivas siguen centralizadas en InduztekDbHelper.
 */
class EstadoProtocoloRepository(context: Context) {
    private val db = InduztekDbHelper(context.applicationContext)
    private val preferencias = InduztekPreferencias(context.applicationContext)

    fun cerrar() { db.close() }

    suspend fun cambiarEstado(idProtocolo: Long, nuevoEstado: String, motivo: String = "") {
        val idUsuario = preferencias.leerSesion()
        require(idUsuario > 0L) { "No hay una sesión activa." }
        withContext(Dispatchers.IO) {
            db.cambiarEstadoProtocolo(idProtocolo, nuevoEstado, idUsuario, motivo)
        }
    }
}
