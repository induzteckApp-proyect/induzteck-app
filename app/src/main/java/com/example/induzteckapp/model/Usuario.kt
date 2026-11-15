package com.example.induzteckapp.model

data class Usuario(
    val idUsuario: Long? = null,
    val rol: RolUsuario,
    val rut: String,
    val nombre: String,
    val apellido: String? = null,
    val correo: String,
    val telefono: String,
    // El backend nunca entrega ni recibe este campo en respuestas.
    // Se conserva vacío para compatibilidad con código legado que usa el modelo.
    val passwordHash: String = "",
    val activo: Boolean = true,
    val giro: String? = null,
    val contactoNombre: String? = null,
    val contactoCargo: String? = null,
    // El avatar local puede seguir existiendo durante la transición.
    val avatar: ByteArray? = null,
    val avatarUrl: String? = null
)
