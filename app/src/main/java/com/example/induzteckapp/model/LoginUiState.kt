package com.example.induzteckapp.model

data class LoginUiState (

    val correo: String = "",
    val contrasena: String = "",
    val contrasenaVisible: Boolean = false,

    val errorCorreo: String? = null,
    val errorContrasena: String? = null,
    val mensajeError: String? = null

)