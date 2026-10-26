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

data class ProtocoloDetalle(
    val idProtocolo: Int,
    val numeroProtocolo: String,
    val fechaHora: String,
    val estadoCodigo: String,
    val estadoEtiqueta: String, // "Formalizado" o "Pendiente de formalización"
    val resultadoGlobal: String, // "Aprobado", "Observado", "Rechazado"
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
