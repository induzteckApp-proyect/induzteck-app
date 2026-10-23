package com.example.induzteckapp.model

/**
 * Representa un tipo de prueba eléctrica disponible
 * para un determinado tipo de equipo.
 */
data class TipoPrueba(
    val idTipoPrueba: Long,
    val nombre: String
) {
    val nombreDesplegable: String
        get() = nombre
}