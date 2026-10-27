package com.example.induzteckapp.model

data class ProtocoloHistorial(
    val idProtocolo: Long,
    val numero: String,
    val cliente: String,
    val instalacion: String,
    val ubicacion: String,
    val tagEquipo: String,
    val tipoEquipo: String,
    val marca: String,
    val modelo: String,
    val tecnico: String,
    val fechaHora: String,
    val temperaturaC: Double,
    val humedadPct: Double,
    val estadoCodigo: String,
    val estadoNombre: String,
    val observaciones: String,
    val pruebas: List<PruebaHistorial>
) {
    val resultadoGlobal: Int?
        get() = pruebas.mapNotNull { if (it.realizada) it.idResultado else null }.maxOrNull()
}

data class PruebaHistorial(
    val idPrueba: Long,
    val nombre: String,
    val elemento: String,
    val realizada: Boolean,
    val idResultado: Int?,
    val mediciones: List<MedicionHistorial>
)

data class MedicionHistorial(
    val nombre: String,
    val valor: Double,
    val unidad: String
)
