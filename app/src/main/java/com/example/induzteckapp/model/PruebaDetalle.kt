package com.example.induzteckapp.model

data class PruebaDetalle(
    val idPrueba: Int,
    val tipoPruebaNombre: String,
    val elemento: String,
    val momento: String,
    val valorMedido: String,
    val unidadMedida: String = "",
    val resultado: String
)
