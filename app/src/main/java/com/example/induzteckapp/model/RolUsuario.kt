// com.example.induzteckapp/model/RolUsuario.kt

package com.example.induzteckapp.model

enum class RolUsuario(
    val codigo: String,
    val etiqueta: String
) {
    CLIENTE(
        codigo = "CLIENTE",
        etiqueta = "Cliente"
    ),
    TECNICO_INGENIERO(
        codigo = "TECNICO_INGENIERO",
        etiqueta = "Técnico / Ingeniero"
    )
}
