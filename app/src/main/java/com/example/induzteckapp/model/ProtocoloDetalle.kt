package com.example.induzteckapp.model

data class ProtocoloDetalle(
    val idProtocolo: Int,
    val numeroProtocolo: String,
    val fechaHora: String,
    val estadoCodigo: String,
    val estadoEtiqueta: String, 
    val resultadoGlobal: String, 
    val tecnicoResponsable: String,
    val clienteNombre: String,
    val instalacionNombre: String,
    val equipoTag: String,
    val tipoEquipo: String,
    val temperatura: String = "20°C",
    val humedad: String = "45%",
    val observaciones: String = "",
    val pruebas: List<PruebaDetalle> = emptyList()
)
