package com.example.induzteckapp.model


data class TipoPrueba(
    val idTipoPrueba: Long,
    val nombre: String
) {
    val nombreDesplegable: String
        get() = nombre
}