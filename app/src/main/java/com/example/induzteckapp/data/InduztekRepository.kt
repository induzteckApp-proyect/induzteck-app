package com.example.induzteckapp.data

import android.content.ContentValues
import com.example.induzteckapp.model.*

class InduztekRepository(context: android.content.Context) {
    private val dbHelper = InduztekDbHelper(context)

    fun obtenerUsuarioPorCorreo(correo: String): Usuario? = dbHelper.obtenerUsuarioPorCorreo(correo)
    fun obtenerUsuarioPorId(idUsuario: Long): Usuario? = dbHelper.obtenerUsuarioPorId(idUsuario)
    fun existeUsuarioPorRut(rut: String): Boolean = dbHelper.existeUsuarioPorRut(rut)
    fun existeUsuarioPorCorreo(correo: String): Boolean = dbHelper.existeUsuarioPorCorreo(correo)
    fun insertarUsuario(valores: ContentValues): Long = dbHelper.insertarUsuario(valores)
    fun actualizarPassword(correo: String, nuevoHash: String): Int = dbHelper.actualizarPassword(correo, nuevoHash)
    fun actualizarAvatarBlob(idUsuario: Long, imagen: ByteArray?): Int = dbHelper.actualizarAvatarBlob(idUsuario, imagen)
    fun actualizarPerfilUsuario(idUsuario: Long, nombre: String, apellido: String?, giro: String?, contactoNombre: String?, telefono: String): Int =
        dbHelper.actualizarPerfilUsuario(idUsuario, nombre, apellido, giro, contactoNombre, telefono)

    fun obtenerClientesConActivos(filtro: String = ""): List<ClienteConsulta> = dbHelper.obtenerClientesConActivos(filtro)
    fun obtenerHistorialEquipo(idEquipo: Int): List<ProtocoloResumen> = dbHelper.obtenerHistorialEquipo(idEquipo)
    fun obtenerHistorialProtocolos(): List<ProtocoloHistorial> = dbHelper.obtenerHistorialProtocolos()
    fun obtenerDetalleProtocolo(idProtocolo: Int): ProtocoloDetalle? = dbHelper.obtenerDetalleProtocolo(idProtocolo)
    fun obtenerClientesProtocolo(): List<ClienteProtocolo> = dbHelper.obtenerClientesProtocolo()
    fun obtenerInstalacionesPorCliente(idUsuarioCliente: Long): List<Instalacion> = dbHelper.obtenerInstalacionesPorCliente(idUsuarioCliente)
    fun obtenerEquiposPorInstalacion(idInstalacion: Long): List<Equipo> = dbHelper.obtenerEquiposPorInstalacion(idInstalacion)
    fun obtenerComunas(): List<Comuna> = dbHelper.obtenerComunas()
    fun obtenerRegiones(): List<Region> = dbHelper.obtenerRegiones()
    fun obtenerTiposEquipo(): List<TipoEquipo> = dbHelper.obtenerTiposEquipo()
    fun obtenerPruebasCompatibles(idTipoEquipo: Long): List<TipoPrueba> = dbHelper.obtenerPruebasCompatibles(idTipoEquipo)
    fun obtenerParametrosPorTipoPrueba(idTipoPrueba: Long): List<ParametroPrueba> = dbHelper.obtenerParametrosPorTipoPrueba(idTipoPrueba)

    fun cambiarEstadoProtocolo(idProtocolo: Long, nuevoEstado: String, idUsuario: Long, motivo: String = "") =
        dbHelper.cambiarEstadoProtocolo(idProtocolo, nuevoEstado, idUsuario, motivo)

    fun registrarProtocolo(
        idEquipo: Long,
        idUsuarioRegistra: Long,
        idTipoPrueba: Long,
        fechaHora: String,
        temperaturaC: Double,
        humedadPct: Double,
        mediciones: Map<String, Double>,
        elemento: String? = null
    ): String = dbHelper.registrarProtocolo(idEquipo, idUsuarioRegistra, idTipoPrueba, fechaHora, temperaturaC, humedadPct, mediciones, elemento)
}
