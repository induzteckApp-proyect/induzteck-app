package com.example.induzteckapp.navigation

sealed class Ruta(val ruta: String) {
    data object Login : Ruta("login")
    data object ActivarCuenta : Ruta("activar_cuenta")
    data object RecuperarContrasena : Ruta("recuperar_contrasena")
    data object Inicio : Ruta("inicio")
    data object CrearProtocolo : Ruta("crear_protocolo?borradorId={borradorId}") {
        fun nueva() = "crear_protocolo"
        fun recuperar(id: Long) = "crear_protocolo?borradorId=$id"
    }
    data object ListaBorradores : Ruta("lista_borradores")
    data object HistorialProtocolos : Ruta("historial_protocolos")
    data object ListaClientes : Ruta("lista_clientes")
    data object ListaInstalaciones : Ruta("lista_instalaciones")
    data object DetalleRespaldo : Ruta("detalle_respaldo/{idProtocolo}") {
        fun construir(idProtocolo: Int) = "detalle_respaldo/$idProtocolo"
    }
    data object Perfil : Ruta("perfil")
    data object AdminUsuarios : Ruta("admin_usuarios")
}
