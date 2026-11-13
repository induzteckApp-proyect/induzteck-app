package com.example.induzteckapp.data.remote

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Emulador Android -> PC. En un teléfono físico reemplaza 10.0.2.2
    // por la IP LAN del computador, por ejemplo http://192.168.1.20:8090/
    const val BASE_URL = "http://10.0.2.2:8090/"

    @Volatile private var api: UsuarioApi? = null
    @Volatile private var avatar: AvatarApi? = null

    fun usuarioApi(context: Context): UsuarioApi {
        return api ?: synchronized(this) {
            api ?: crear(context.applicationContext).create(UsuarioApi::class.java).also { api = it }
        }
    }

    fun avatarApi(context: Context): AvatarApi {
        return avatar ?: synchronized(this) {
            avatar ?: crear(context.applicationContext).create(AvatarApi::class.java).also { avatar = it }
        }
    }

    private fun crear(context: Context): Retrofit {
        val interceptor = Interceptor { chain ->
            val token = com.example.induzteckapp.data.InduztekPreferencias(context).leerToken()
            val request = chain.request().newBuilder().apply {
                if (!token.isNullOrBlank()) addHeader("Authorization", "Bearer $token")
                addHeader("Accept", "application/json")
            }.build()
            chain.proceed(request)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
