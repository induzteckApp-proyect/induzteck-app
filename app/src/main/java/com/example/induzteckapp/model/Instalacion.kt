// app/src/main/java/com/example/induzteckapp/model/
package com.example.induzteckapp.model

data class Instalacion(
    val idInstalacion: Long,
    val idUsuarioCliente: Long,
    val nombre: String,
    val ubicacion: String?,
    val idComuna: Long,
    val nombreComuna: String,
    val observacionesAcceso: String?
)