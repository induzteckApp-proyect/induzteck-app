package com.example.induzteckapp.data

import android.content.Context
import com.example.induzteckapp.data.remote.*
import com.example.induzteckapp.model.*

class ActivosRepository(context: Context) {
    private val appContext = context.applicationContext
    private val api = RetrofitClient.activosApi(appContext)

    suspend fun obtenerRegiones(): List<Region> = ejecutarApi { api.regiones() }
        .map { Region(it.id, it.nombre) }

    suspend fun obtenerComunas(): List<Comuna> = ejecutarApi { api.comunas() }
        .map { Comuna(it.id, it.idRegion, it.nombre) }

    suspend fun obtenerTiposEquipo(): List<TipoEquipo> = ejecutarApi { api.tiposEquipo() }
        .map { TipoEquipo(it.id, it.nombre, it.clasificacion) }

    suspend fun obtenerClientesProtocolo(): List<ClienteProtocolo> = ejecutarApi { api.clientes() }
        .map { ClienteProtocolo(it.idUsuario, it.rut, it.nombre, it.correo) }

    suspend fun listarClientesConsulta(filtro: String = ""): List<ClienteConsulta> = ejecutarApi { api.clientes(filtro.ifBlank { null }) }
        .map { cliente ->
            ClienteConsulta(
                idUsuario = cliente.idUsuario.toInt(),
                nombreRazonSocial = cliente.nombre,
                rut = cliente.rut,
                correo = cliente.correo,
                instalaciones = cliente.instalaciones.map { bloque ->
                    InstalacionConsulta(
                        idInstalacion = bloque.instalacion.id.toInt(),
                        nombre = bloque.instalacion.nombre,
                        equipos = bloque.equipos.map { equipo ->
                            EquipoConsulta(
                                idEquipo = equipo.id.toInt(),
                                tag = equipo.tag,
                                tipoEquipo = equipo.tipoEquipo,
                                instalacionNombre = bloque.instalacion.nombre
                            )
                        }
                    )
                }
            )
        }

    suspend fun obtenerInstalacionesPorCliente(idCliente: Long): List<Instalacion> = ejecutarApi { api.instalacionesCliente(idCliente) }
        .map { it.aModelo() }

    suspend fun obtenerEquiposPorInstalacion(idInstalacion: Long): List<Equipo> = ejecutarApi { api.equiposInstalacion(idInstalacion) }
        .map { it.aModelo() }

    suspend fun obtenerEquipo(idEquipo: Long): Equipo = ejecutarApi { api.equipo(idEquipo) }.aModelo()

    suspend fun obtenerInstalacion(idInstalacion: Long): Instalacion = ejecutarApi { api.instalacion(idInstalacion) }.aModelo()

    suspend fun insertarInstalacionProtocolo(
        idUsuarioCliente: Long, nombre: String, ubicacion: String,
        idComuna: Long, observacionesAcceso: String?
    ): Long = ejecutarApi {
        api.crearInstalacion(CrearInstalacionRequestDto(
            idUsuarioCliente, nombre, ubicacion.ifBlank { null }, idComuna, observacionesAcceso
        ))
    }.id

    suspend fun insertarEquipoProtocolo(
        idInstalacion: Long, tag: String, idTipoEquipo: Long,
        marca: String?, modelo: String?, numeroSerie: String?, potenciaKva: Double?,
        tensiones: Double?, corriente: Double?, maxContactosUohm: Double?
    ): Long = ejecutarApi {
        api.crearEquipo(CrearEquipoRequestDto(idInstalacion, tag, idTipoEquipo, marca, modelo,
            numeroSerie, potenciaKva, tensiones, corriente, maxContactosUohm))
    }.id

    private fun InstalacionDto.aModelo() = Instalacion(
        idInstalacion = id, idUsuarioCliente = idUsuarioCliente, nombre = nombre,
        ubicacion = ubicacion, idComuna = idComuna, nombreComuna = comuna,
        observacionesAcceso = observacionesAcceso
    )

    private fun EquipoDto.aModelo() = Equipo(
        idEquipo = id, idInstalacion = idInstalacion, tag = tag, idTipoEquipo = idTipoEquipo,
        nombreTipoEquipo = tipoEquipo, clasificacionTipo = clasificacion, marca = marca,
        modelo = modelo, numeroSerie = numeroSerie, potenciaKva = potenciaKva,
        tensiones = tensiones, corriente = corriente, maxContactosUohm = maxContactosUohm
    )
}



/*
package com.example.induzteckapp.data

import android.content.Context
import com.example.induzteckapp.data.remote.*
import com.example.induzteckapp.model.*

class ActivosRepository(context: Context) {
    private val appContext = context.applicationContext
    private val api = RetrofitClient.activosApi(appContext)

    suspend fun obtenerRegiones(): List<Region> = ejecutarApi { api.regiones() }
        .map { Region(it.id, it.nombre) }

    suspend fun obtenerComunas(): List<Comuna> = ejecutarApi { api.comunas() }
        .map { Comuna(it.id, it.idRegion, it.nombre) }

    suspend fun obtenerTiposEquipo(): List<TipoEquipo> = ejecutarApi { api.tiposEquipo() }
        .map { TipoEquipo(it.id, it.nombre, it.clasificacion) }

    suspend fun obtenerClientesProtocolo(): List<ClienteProtocolo> = ejecutarApi { api.clientes() }
        .map { ClienteProtocolo(it.idUsuario, it.rut, it.nombre, it.correo) }

    suspend fun listarClientesConsulta(filtro: String = ""): List<ClienteConsulta> = ejecutarApi { api.clientes(filtro.ifBlank { null }) }
        .map { cliente ->
            ClienteConsulta(
                idUsuario = cliente.idUsuario.toInt(),
                nombreRazonSocial = cliente.nombre,
                rut = cliente.rut,
                instalaciones = cliente.instalaciones.map { bloque ->
                    InstalacionConsulta(
                        idInstalacion = bloque.instalacion.id.toInt(),
                        nombre = bloque.instalacion.nombre,
                        equipos = bloque.equipos.map { equipo ->
                            EquipoConsulta(
                                idEquipo = equipo.id.toInt(),
                                tag = equipo.tag,
                                tipoEquipo = equipo.tipoEquipo,
                                instalacionNombre = bloque.instalacion.nombre
                            )
                        }
                    )
                }
            )
        }

    suspend fun obtenerInstalacionesPorCliente(idCliente: Long): List<Instalacion> = ejecutarApi { api.instalacionesCliente(idCliente) }
        .map { it.aModelo() }

    suspend fun obtenerEquiposPorInstalacion(idInstalacion: Long): List<Equipo> = ejecutarApi { api.equiposInstalacion(idInstalacion) }
        .map { it.aModelo() }

    suspend fun obtenerEquipo(idEquipo: Long): Equipo = ejecutarApi { api.equipo(idEquipo) }.aModelo()

    suspend fun obtenerInstalacion(idInstalacion: Long): Instalacion = ejecutarApi { api.instalacion(idInstalacion) }.aModelo()

    suspend fun insertarInstalacionProtocolo(
        idUsuarioCliente: Long, nombre: String, ubicacion: String,
        idComuna: Long, observacionesAcceso: String?
    ): Long = ejecutarApi {
        api.crearInstalacion(CrearInstalacionRequestDto(
            idUsuarioCliente, nombre, ubicacion.ifBlank { null }, idComuna, observacionesAcceso
        ))
    }.id

    suspend fun insertarEquipoProtocolo(
        idInstalacion: Long, tag: String, idTipoEquipo: Long,
        marca: String?, modelo: String?, numeroSerie: String?, potenciaKva: Double?,
        tensiones: Double?, corriente: Double?, maxContactosUohm: Double?
    ): Long = ejecutarApi {
        api.crearEquipo(CrearEquipoRequestDto(idInstalacion, tag, idTipoEquipo, marca, modelo,
            numeroSerie, potenciaKva, tensiones, corriente, maxContactosUohm))
    }.id

    private fun InstalacionDto.aModelo() = Instalacion(
        idInstalacion = id, idUsuarioCliente = idUsuarioCliente, nombre = nombre,
        ubicacion = ubicacion, idComuna = idComuna, nombreComuna = comuna,
        observacionesAcceso = observacionesAcceso
    )

    private fun EquipoDto.aModelo() = Equipo(
        idEquipo = id, idInstalacion = idInstalacion, tag = tag, idTipoEquipo = idTipoEquipo,
        nombreTipoEquipo = tipoEquipo, clasificacionTipo = clasificacion, marca = marca,
        modelo = modelo, numeroSerie = numeroSerie, potenciaKva = potenciaKva,
        tensiones = tensiones, corriente = corriente, maxContactosUohm = maxContactosUohm
    )
}

*/