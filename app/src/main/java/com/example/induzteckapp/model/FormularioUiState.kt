// com.example.induzteckapp/model/FormularioUiState.kt


package com.example.induzteckapp.model

// Estado de la pantalla de registro de Induzteck.
data class FormularioUiState(

    // === Valores de los campos del formulario ===

    val rol: RolUsuario? = null,

    // Identificación personal o empresarial
    val esEmpresa: Boolean = false,
    val rut: String = "",
    val nombre: String = "",
    val apellido: String = "",

    // Datos de contacto
    val correo: String = "",
    val telefono: String = "",

    // Datos comerciales exclusivos del cliente empresa
    val giro: String = "",
    val contactoNombre: String = "",
    val contactoCargo: String = "",

    // Seguridad de la cuenta
    val contrasena: String = "",
    val confirmarContrasena: String = "",

    // Aceptación de condiciones
    val aceptaTerminos: Boolean = false,

    // === Estados visuales ===

    val contrasenaVisible: Boolean = false,
    val confirmarContrasenaVisible: Boolean = false,

    // === Errores de validación ===

    val errorRol: String? = null,
    val errorRut: String? = null,
    val errorNombre: String? = null,
    val errorApellido: String? = null,
    val errorCorreo: String? = null,
    val errorTelefono: String? = null,
    val errorGiro: String? = null,
    val errorContactoNombre: String? = null,
    val errorContactoCargo: String? = null,
    val errorContrasena: String? = null,
    val errorConfirmarContrasena: String? = null,
    val errorTermino: String? = null,

    // === Resultado del formulario ===

    val registroExitoso: Boolean = false
)


/*
package com.example.induzteckapp.model

enum class NivelExperiencia(val etiqueta: String){

    PRINCIPIANTE("Principiante"),
    INTERMEDIO("Intermedio"),
    AVANZADO("Avanzado")

}

// Variables o datos de la pantalla:

data class FormularioUiState(

    // Valores de los campos del formulario

    val nombre : String = "",
    val correo : String = "",
    val contrasena : String = "",
    val nivel : NivelExperiencia = NivelExperiencia.PRINCIPIANTE,
    val recibirNoticias : Boolean = false,
    val aceptaTerminos : Boolean = false,

    // Estado Visual de la contraseña

    val contrasenaVisible: Boolean = false,

    // Errores de validacion de los campos

    val errorNombre : String? = null,
    val errorCorreo: String? = null,
    val errorContrasena: String? = null,
    val errorTermino : String? = null,

    // Resultado exitoso:

    val registroExitoso: Boolean = false

)

*/