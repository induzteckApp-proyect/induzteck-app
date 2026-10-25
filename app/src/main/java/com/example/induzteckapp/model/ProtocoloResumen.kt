package com.example.induzteckapp.model

data class ProtocoloResumen (
    val idProtocolo: Int,
    val numeroProtocolo: String,
    val fechaHora: String,
    val estado: String,
    val resultadoGlobal: String,
    val tecnicoResponsable: String
)