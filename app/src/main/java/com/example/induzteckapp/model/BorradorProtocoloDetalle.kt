package com.example.induzteckapp.model

data class BorradorProtocoloDetalle(
    val idProtocolo: Long,
    val idUsuarioCliente: Long,
    val idInstalacion: Long,
    val idEquipo: Long,
    val temperaturaC: Double,
    val humedadPct: Double
)