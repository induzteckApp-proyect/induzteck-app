package com.example.induzteckapp.model

data class InstalacionConsulta (
    val idInstalacion: Int,
    val nombre: String,
    val equipos: List<EquipoConsulta> = emptyList(),
    val isExpanded: Boolean = false
)
