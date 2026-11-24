package com.example.induzteckapp.model

enum class RolUsuario(val codigo: String, val etiqueta: String) {
    ADMIN("ADMIN", "Administrador"),
    CLIENTE("CLIENTE", "Cliente"),
    TECNICO_INGENIERO("TECNICO_INGENIERO", "Técnico / Ingeniero");

    companion object {
        fun desdeCodigo(codigo: String): RolUsuario = when (codigo.uppercase()) {
            "ADMIN" -> ADMIN
            "TECNICO", "TECNICO_INGENIERO" -> TECNICO_INGENIERO
            else -> CLIENTE
        }
    }
}
