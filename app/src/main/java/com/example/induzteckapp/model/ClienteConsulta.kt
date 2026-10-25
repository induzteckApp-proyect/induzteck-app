package com.example.induzteckapp.model

data class ClienteConsulta (
    val idUsuario: Int,
    val nombreRazonSocial: String,
    val rut: String,
    val instalaciones: List<InstalacionConsulta> = emptyList(),
    val isExpanded: Boolean = false
)