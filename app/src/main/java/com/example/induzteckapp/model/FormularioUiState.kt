


package com.example.induzteckapp.model


data class FormularioUiState(

    

    val rol: RolUsuario? = null,

    
    val esEmpresa: Boolean = false,
    val rut: String = "",
    val nombre: String = "",
    val apellido: String = "",

    
    val correo: String = "",
    val telefono: String = "",

    
    val giro: String = "",
    val contactoNombre: String = "",
    val contactoCargo: String = "",

    
    val contrasena: String = "",
    val confirmarContrasena: String = "",

    
    val aceptaTerminos: Boolean = false,

    

    val contrasenaVisible: Boolean = false,
    val confirmarContrasenaVisible: Boolean = false,

    

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

    

    val registroExitoso: Boolean = false
)

