// com.example.induzteckapp/navigation/Ruta.kt
package com.example.induzteckapp.navigation

// Rutas principales de InduzteckApp.
sealed class Ruta(val ruta: String) {
    data object Login : Ruta("login")
    data object Registro : Ruta("registro")
    data object RecuperarContrasena : Ruta("recuperar_contrasena")
    data object Inicio : Ruta("inicio")
    data object CrearProtocolo : Ruta("crear_protocolo?borradorId={borradorId}") {
        fun nueva() = "crear_protocolo"
        fun recuperar(id: Long) = "crear_protocolo?borradorId=$id"
    }
    data object ListaBorradores : Ruta("lista_borradores")

    data object ListaClientes : Ruta("lista_clientes")  // Vista extendida (Técnico)

    data object ListaInstalaciones : Ruta("lista_instalaciones") // Vista extendida (Cliente)
}

/*
// com.example.induzteckapp/navigation/Ruta.kt
package com.example.induzteckapp.navigation

// Rutas principales de InduzteckApp.
sealed class Ruta(val ruta: String) {
    data object Login : Ruta("login")
    data object Registro : Ruta("registro")
    data object RecuperarContrasena : Ruta("recuperar_contrasena")
    data object Inicio : Ruta("inicio")
    data object CrearProtocolo : Ruta("crear_protocolo")

    data object ListaClientes : Ruta("lista_clientes")  // Vista extendida (Técnico)

    data object ListaInstalaciones : Ruta("lista_instalaciones") // Vista extendida (Cliente)
}

*/