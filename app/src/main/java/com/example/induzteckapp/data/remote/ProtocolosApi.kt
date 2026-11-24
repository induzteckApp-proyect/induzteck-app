package com.example.induzteckapp.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ProtocolosApi {
    @GET("api/protocolos/catalogos/tipos-prueba")
    suspend fun tiposPrueba(@Query("idTipoEquipo") idTipoEquipo: Long): List<TipoPruebaDto>

    @GET("api/protocolos/catalogos/tipos-prueba/{idTipoPrueba}/parametros")
    suspend fun parametros(@Path("idTipoPrueba") idTipoPrueba: Long): List<ParametroDto>

    @POST("api/protocolos/borradores")
    suspend fun crearBorrador(@Body request: BorradorRequestDto): ProtocoloDto

    @PUT("api/protocolos/{id}/borrador")
    suspend fun actualizarCabecera(@Path("id") id: Long, @Body request: BorradorRequestDto): ProtocoloDto

    @POST("api/protocolos/{id}/pruebas/seleccionadas")
    suspend fun guardarPruebasSeleccionadas(@Path("id") id: Long, @Body request: GuardarPruebasSeleccionadasRequestDto): ProtocoloDto

    @POST("api/protocolos/{id}/pruebas")
    suspend fun guardarPrueba(@Path("id") id: Long, @Body request: GuardarPruebaRequestDto): ProtocoloDto

    @PUT("api/protocolos/{id}/estado")
    suspend fun cambiarEstado(@Path("id") id: Long, @Body request: CambioEstadoRequestDto): ProtocoloDto

    @POST("api/protocolos/{id}/preparar-edicion")
    suspend fun prepararEdicion(@Path("id") id: Long): ProtocoloDto

    @POST("api/protocolos/{id}/reemplazar")
    suspend fun reemplazar(@Path("id") id: Long, @Body request: BorradorRequestDto): ProtocoloDto

    @GET("api/protocolos/borradores/me") suspend fun borradores(): List<ProtocoloDto>
    @GET("api/protocolos/historial") suspend fun historial(): List<ProtocoloDto>
    @GET("api/protocolos/equipos/{idEquipo}") suspend fun protocolosEquipo(@Path("idEquipo") idEquipo: Long): List<ProtocoloDto>
    @GET("api/protocolos/clientes/me") suspend fun protocolosCliente(): List<ProtocoloDto>
    @GET("api/protocolos/{id}") suspend fun detalle(@Path("id") id: Long): ProtocoloDto
}
