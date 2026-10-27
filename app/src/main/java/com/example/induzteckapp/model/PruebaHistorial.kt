package com.example.induzteckapp.model

data class PruebaHistorial(
    val idPrueba: Long,
    val nombre: String,
    val elemento: String,
    val realizada: Boolean,
    val idResultado: Int?,
    val mediciones: List<MedicionHistorial>
)
