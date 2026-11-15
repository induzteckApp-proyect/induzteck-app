package com.example.induzteckapp.data.remote

data class RegionDto(val id: Long, val nombre: String)
data class ComunaDto(val id: Long, val nombre: String, val idRegion: Long, val region: String)
data class TipoEquipoDto(val id: Long, val nombre: String, val clasificacion: String)
data class InstalacionDto(
    val id: Long,
    val idUsuarioCliente: Long,
    val nombre: String,
    val ubicacion: String?,
    val idComuna: Long,
    val comuna: String,
    val idRegion: Long,
    val region: String,
    val observacionesAcceso: String?
)
data class EquipoDto(
    val id: Long,
    val idInstalacion: Long,
    val tag: String,
    val idTipoEquipo: Long,
    val tipoEquipo: String,
    val clasificacion: String,
    val marca: String?,
    val modelo: String?,
    val numeroSerie: String?,
    val potenciaKva: Double?,
    val tensiones: Double?,
    val corriente: Double?,
    val maxContactosUohm: Double?
)
data class InstalacionConEquiposDto(val instalacion: InstalacionDto, val equipos: List<EquipoDto>)
data class ClienteActivosDto(
    val idUsuario: Long,
    val rut: String,
    val nombre: String,
    val correo: String,
    val instalaciones: List<InstalacionConEquiposDto>
)
data class CrearInstalacionRequestDto(
    val idUsuarioCliente: Long,
    val nombre: String,
    val ubicacion: String?,
    val idComuna: Long,
    val observacionesAcceso: String?
)
data class CrearEquipoRequestDto(
    val idInstalacion: Long,
    val tag: String,
    val idTipoEquipo: Long,
    val marca: String?,
    val modelo: String?,
    val numeroSerie: String?,
    val potenciaKva: Double?,
    val tensiones: Double?,
    val corriente: Double?,
    val maxContactosUohm: Double?
)
