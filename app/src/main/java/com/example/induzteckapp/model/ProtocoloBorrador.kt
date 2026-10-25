package com.example.induzteckapp.model

data class ProtocoloBorrador(
    val idProtocolo: Long,
    val numero: String,
    val tagEquipo: String,
    val tipoEquipo: String,
    val fechaHora: String,
    val temperaturaC: Double,
    val humedadPct: Double
)