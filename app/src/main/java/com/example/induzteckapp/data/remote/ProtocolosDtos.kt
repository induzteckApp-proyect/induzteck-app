package com.example.induzteckapp.data.remote

data class TipoPruebaDto(val id: Long, val nombre: String)
data class ParametroDto(val id: Long, val idTipoPrueba: Long, val codigo: String, val nombre: String, val unidad: String)
data class BorradorRequestDto(
    val idEquipo: Long,
    val fechaHoraPrueba: String,
    val temperaturaC: Double,
    val humedadPct: Double,
    val observaciones: String? = null,
    val idsTiposPrueba: List<Long> = emptyList()
)
data class MedicionRequestDto(val idParametro: Long, val valor: Double)
data class GuardarPruebaRequestDto(
    val idPrueba: Long? = null,
    val idTipoPrueba: Long,
    val elemento: String? = null,
    val momento: String = "UNICA",
    val realizada: Boolean = true,
    val motivoNoRealizada: String? = null,
    val idResultado: Int? = null,
    val mediciones: List<MedicionRequestDto> = emptyList()
)
data class GuardarPruebasSeleccionadasRequestDto(val idsTiposPrueba: List<Long>)
data class CambioEstadoRequestDto(val estado: String, val motivo: String? = null)
data class MedicionDto(val id: Long, val idParametro: Long, val parametro: String, val unidad: String, val valor: Double)
data class PruebaDto(
    val id: Long,
    val idTipoPrueba: Long,
    val tipoPrueba: String,
    val elemento: String?,
    val momento: String,
    val realizada: Boolean,
    val motivoNoRealizada: String?,
    val idResultado: Int?,
    val resultado: String,
    val mediciones: List<MedicionDto>
)
data class ProtocoloDto(
    val id: Long,
    val numero: String,
    val idEquipo: Long,
    val idUsuarioRegistra: Long,
    val fechaHoraPrueba: String,
    val temperaturaC: Double,
    val humedadPct: Double,
    val observaciones: String?,
    val estadoCodigo: String,
    val estadoNombre: String,
    val idProtocoloReemplazado: Long?,
    val pruebas: List<PruebaDto>,
    val resultadoGlobal: String,
    val idUsuarioCliente: Long,
    val clienteNombre: String,
    val clienteCorreo: String,
    val instalacionNombre: String,
    val ubicacion: String?,
    val comuna: String,
    val region: String,
    val tagEquipo: String,
    val tipoEquipo: String,
    val marca: String?,
    val modelo: String?,
    val numeroSerie: String?,
    val tecnicoNombre: String,
    val tecnicoCorreo: String
)
