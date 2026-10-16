package com.example.induzteckapp.util

import java.security.MessageDigest

/**
 * Utilidad temporal para la demostración local.
 *
 * SHA-256 evita guardar la contraseña directamente,
 * pero NO es suficiente para proteger contraseñas
 * en una aplicación de producción.
 */
fun generarResumenContrasena(contrasena: String): String {
    val bytes = MessageDigest
        .getInstance("SHA-256")
        .digest(contrasena.toByteArray(Charsets.UTF_8))

    return bytes.joinToString("") { byte ->
        "%02x".format(byte)
    }
}

/**
 * Compara la contraseña ingresada con el resumen
 * guardado en el objeto Usuario.
 */
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