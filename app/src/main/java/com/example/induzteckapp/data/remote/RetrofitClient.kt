package com.example.induzteckapp.data.remote

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    const val BASE_URL = com.example.induzteckapp.BuildConfig.API_BASE_URL

    @Volatile private var api: UsuarioApi? = null
    @Volatile private var avatar: AvatarApi? = null
    @Volatile private var activos: ActivosApi? = null
    @Volatile private var protocolos: ProtocolosApi? = null

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

    fun activosApi(context: Context): ActivosApi {
        return activos ?: synchronized(this) {
            activos ?: crear(context.applicationContext).create(ActivosApi::class.java).also { activos = it }
        }
    }

    fun protocolosApi(context: Context): ProtocolosApi {
        return protocolos ?: synchronized(this) {
            protocolos ?: crear(context.applicationContext).create(ProtocolosApi::class.java).also { protocolos = it }
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
