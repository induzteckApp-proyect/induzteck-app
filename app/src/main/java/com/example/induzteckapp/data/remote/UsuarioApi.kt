package com.example.induzteckapp.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface UsuarioApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/activar")
    suspend fun activar(@Body request: ActivarRequest): UsuarioResponse

    @POST("api/auth/recuperar")
    suspend fun recuperar(@Body request: RecuperarRequest): MensajeResponse

    @POST("api/auth/restablecer")
    suspend fun restablecer(@Body request: RestablecerRequest): MensajeResponse

    @POST("api/usuarios")
    suspend fun crearUsuario(@Body request: CrearUsuarioRequest): MensajeResponse

    @GET("api/usuarios/me")
    suspend fun miPerfil(): UsuarioResponse

    @PUT("api/usuarios/me")
    suspend fun actualizarPerfil(@Body request: ActualizarPerfilRequest): UsuarioResponse

    @PUT("api/usuarios/me/contrasena")
    suspend fun cambiarContrasena(@Body request: CambiarContrasenaRequest): MensajeResponse
}
