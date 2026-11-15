package com.example.induzteckapp.data.remote

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AvatarApi {
    @Multipart
    @POST("api/usuarios/me/avatar")
    suspend fun subir(@Part archivo: MultipartBody.Part): UsuarioResponse
}
