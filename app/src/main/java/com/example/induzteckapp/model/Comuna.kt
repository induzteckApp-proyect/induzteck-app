package com.example.induzteckapp.model

data class Comuna(
    val idComuna: Long,
    val idRegion: Long,
    val nombre: String
)

/*
data class Comuna(
    val idComuna: Long,
    val idRegion: Long,
    val nombre: String,
    val nombreRegion: String? = null
) {
    val nombreDesplegable: String
        get() = if (nombreRegion.isNullOrBlank()) {
            nombre
        } else {
            "$nombre, $nombreRegion"
        }
}

*/