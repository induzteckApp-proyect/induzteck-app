package com.example.induzteckapp.util

import java.security.MessageDigest


fun generarResumenContrasena(contrasena: String): String {
    val bytes = MessageDigest
        .getInstance("SHA-256")
        .digest(contrasena.toByteArray(Charsets.UTF_8))

    return bytes.joinToString("") { byte ->
        "%02x".format(byte)
    }
}


fun verificarContrasena(
    contrasena: String,
    resumenGuardado: String
): Boolean {
    val resumenIngresado = generarResumenContrasena(contrasena)

    return MessageDigest.isEqual(
        resumenIngresado.toByteArray(Charsets.UTF_8),
        resumenGuardado.toByteArray(Charsets.UTF_8)
    )
}