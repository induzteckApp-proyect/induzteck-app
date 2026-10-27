
package com.example.induzteckapp.model

data class InicioUiState (
    val usuario: Usuario? = null,
    val esTecnico: Boolean = false,
    val esCliente: Boolean = false,
    val tituloSaludo: String = "",
    val descripcionBienvenida: String = "",
    val mostrarBotonCrearProtocolo: Boolean = false

)