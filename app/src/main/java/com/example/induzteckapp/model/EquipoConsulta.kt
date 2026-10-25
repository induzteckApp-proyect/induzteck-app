package com.example.induzteckapp.model

data class EquipoConsulta (
    val idEquipo: Int,
    val tag: String,
    val tipoEquipo: String,
    val instalacionNombre: String,
    val historialProtocolos: List<ProtocoloResumen> = emptyList(),
    val isExpanded: Boolean = false
)