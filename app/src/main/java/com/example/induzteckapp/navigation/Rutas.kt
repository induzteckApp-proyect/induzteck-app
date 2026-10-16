package com.example.induzteckapp.navigation

// Rutas principales de InduzteckApp.
sealed class Ruta(val ruta: String) {

    data object Login : Ruta("login")

    data object Registro : Ruta("registro")

    data object Inicio : Ruta("inicio")
}