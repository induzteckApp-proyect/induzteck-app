// com.example.induzteckapp/model/InicioUiState.kt
package com.example.induzteckapp.model

data class InicioUiState (
    val usuario: Usuario? = null
    /*
    * Es deliberadamente pequeño.
    * Más adelante agregaremos aquí los
    * datos de resumen que necesite mostrar el Dashboard,
    * sin mezclar las responsabilidades de
    * la interfaz con las del ViewModel.
    * */

)