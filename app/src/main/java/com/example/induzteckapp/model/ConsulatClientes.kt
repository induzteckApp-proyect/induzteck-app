package com.example.induzteckapp.model

data class ClienteConsulta(
    val idUsuario: Int,
    val nombreRazonSocial: String,
    val rut: String,
    val instalaciones: List<InstalacionConsulta> = emptyList(),
    val isExpanded: Boolean = false
)

data class InstalacionConsulta(
    val idInstalacion: Int,
    val nombre: String,
    val equipos: List<EquipoConsulta> = emptyList()
)

data class EquipoConsulta(
    val idEquipo: Int,
    val tag: String,
    val tipoEquipo: String,
    val instalacionNombre: String,
    val historialProtocolos: List<ProtocoloResumen> = emptyList(),
    val isExpanded: Boolean = false
)

data class ProtocoloResumen(
    val idProtocolo: Int,
    val numeroProtocolo: String,
    val fechaHora: String,
    val estado: String,
    val resultadoGlobal: String,
    val tecnicoResponsable: String
)