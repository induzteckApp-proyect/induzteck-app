package com.example.induzteckapp.model

data class ClienteProtocolo(
    val idUsuario: Long,
    val rut: String,
    val nombre: String,
    val correo: String
) {
    val nombreDesplegable: String
        get() = "$nombre — $rut"
}