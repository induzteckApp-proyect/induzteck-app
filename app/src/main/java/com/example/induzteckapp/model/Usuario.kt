package com.example.induzteckapp.model

data class Usuario(
    val idUsuario: Int? = null,
    val rol: RolUsuario,
    val rut: String,
    val nombre: String,
    val apellido: String? = null,
    val correo: String,
    val telefono: String,
    val passwordHash: String,
    val activo: Boolean = true,
    val giro: String? = null,
    val contactoNombre: String? = null,
    val contactoCargo: String? = null
)