package com.example.induzteckapp.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ActivosApi {
    @GET("api/activos/regiones") suspend fun regiones(): List<RegionDto>
    @GET("api/activos/comunas") suspend fun comunas(): List<ComunaDto>
    @GET("api/activos/tipos-equipo") suspend fun tiposEquipo(): List<TipoEquipoDto>
    @GET("api/activos/clientes") suspend fun clientes(@Query("filtro") filtro: String? = null): List<ClienteActivosDto>
    @GET("api/activos/clientes/{idCliente}/instalaciones") suspend fun instalacionesCliente(@Path("idCliente") idCliente: Long): List<InstalacionDto>
    @GET("api/activos/instalaciones/{idInstalacion}") suspend fun instalacion(@Path("idInstalacion") idInstalacion: Long): InstalacionDto
    @GET("api/activos/instalaciones/{idInstalacion}/equipos") suspend fun equiposInstalacion(@Path("idInstalacion") idInstalacion: Long): List<EquipoDto>
    @GET("api/activos/equipos/{idEquipo}") suspend fun equipo(@Path("idEquipo") idEquipo: Long): EquipoDto
    @POST("api/activos/instalaciones") suspend fun crearInstalacion(@Body request: CrearInstalacionRequestDto): InstalacionDto
    @POST("api/activos/equipos") suspend fun crearEquipo(@Body request: CrearEquipoRequestDto): EquipoDto
}
