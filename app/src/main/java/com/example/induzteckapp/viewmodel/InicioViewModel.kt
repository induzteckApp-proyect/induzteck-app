// com.example.induzteckapp/viewmodel/InicioViewModel.kt

package com.example.induzteckapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.induzteckapp.data.InduztekDbHelper
import com.example.induzteckapp.data.InduztekPreferencias
import com.example.induzteckapp.model.InicioUiState
import com.example.induzteckapp.model.RolUsuario
import com.example.induzteckapp.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InicioViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = InduztekDbHelper(application)
    private val preferencias = InduztekPreferencias(application)

    private val _uiState = MutableStateFlow(InicioUiState())
    val uiState: StateFlow<InicioUiState> = _uiState.asStateFlow()

    init {
        cargarUsuarioSesionActiva()
    }

    /**
     * Carga automáticamente al usuario activo desde SharedPreferences y SQLite,
     * calculando las propiedades visuales necesarias para desacoplar la vista.
     */
    fun cargarUsuarioSesionActiva() {
        val idUsuario = preferencias.leerSesion()
        if (idUsuario != -1L) {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT * FROM ${InduztekDbHelper.TABLA_USUARIO} WHERE id_usuario = ?",
                arrayOf(idUsuario.toString())
            )
            cursor.use {
                if (it.moveToFirst()) {
                    val id = it.getLong(it.getColumnIndexOrThrow("id_usuario"))
                    val idRol = it.getLong(it.getColumnIndexOrThrow("id_rol"))
                    val rut = it.getString(it.getColumnIndexOrThrow("rut"))
                    val nombre = it.getString(it.getColumnIndexOrThrow("nombre"))
                    val correo = it.getString(it.getColumnIndexOrThrow("correo"))
                    val telefono = it.getString(it.getColumnIndexOrThrow("telefono"))
                    val passwordHash = it.getString(it.getColumnIndexOrThrow("password_hash"))
                    val activo = it.getInt(it.getColumnIndexOrThrow("activo")) == 1
                    val avatarUri = it.getString(it.getColumnIndexOrThrow("avatar_uri"))

                    val rolEnum = if (idRol == 1L) RolUsuario.TECNICO_INGENIERO else RolUsuario.CLIENTE
                    val esTecnico = rolEnum == RolUsuario.TECNICO_INGENIERO

                    val usuario = Usuario(
                        idUsuario = id,
                        rol = rolEnum,
                        rut = rut,
                        nombre = nombre,
                        correo = correo,
                        telefono = telefono ?: "",
                        passwordHash = passwordHash,
                        activo = activo,
                        avatarUri = avatarUri
                    )

                    _uiState.update { state ->
                        state.copy(
                            usuario = usuario,
                            esTecnico = esTecnico,
                            esCliente = !esTecnico,
                            tituloSaludo = if (esTecnico) "Hola, Técnico ${usuario.nombre}" else "Hola, ${usuario.nombre}",
                            descripcionBienvenida = if (esTecnico)
                                "Espacio de trabajo operativo. Registra protocolos en terreno e inspecciona las instalaciones."
                            else
                                "Espacio de consulta de cliente. Visualiza el estado de tus instalaciones y revisiones eléctricas.",
                            mostrarBotonCrearProtocolo = esTecnico
                        )
                    }
                }
            }
        }
    }

    fun cerrarSesion() {
        preferencias.borrarSesion()
        _uiState.update { InicioUiState() }
    }
}

/*
package com.example.induzteckapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.induzteckapp.data.InduztekDbHelper
import com.example.induzteckapp.data.InduztekPreferencias
import com.example.induzteckapp.model.InicioUiState
import com.example.induzteckapp.model.RolUsuario
import com.example.induzteckapp.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InicioViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = InduztekDbHelper(application)
    private val preferencias = InduztekPreferencias(application)

    private val _uiState = MutableStateFlow(InicioUiState())
    val uiState: StateFlow<InicioUiState> = _uiState.asStateFlow()

    init {
        cargarUsuarioSesionActiva()
    }

    /**
     * Carga automáticamente al usuario activo usando SharedPreferences y SQLite
     */
    fun cargarUsuarioSesionActiva() {
        val idUsuario = preferencias.leerSesion()
        if (idUsuario != -1L) {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery("SELECT * FROM ${InduztekDbHelper.TABLA_USUARIO} WHERE id_usuario = ?", arrayOf(idUsuario.toString()))
            cursor.use {
                if (it.moveToFirst()) {
                    val id = it.getLong(it.getColumnIndexOrThrow("id_usuario"))
                    val idRol = it.getLong(it.getColumnIndexOrThrow("id_rol"))
                    val rut = it.getString(it.getColumnIndexOrThrow("rut"))
                    val nombre = it.getString(it.getColumnIndexOrThrow("nombre"))
                    val correo = it.getString(it.getColumnIndexOrThrow("correo"))
                    val telefono = it.getString(it.getColumnIndexOrThrow("telefono"))
                    val passwordHash = it.getString(it.getColumnIndexOrThrow("password_hash"))
                    val activo = it.getInt(it.getColumnIndexOrThrow("activo")) == 1
                    val avatarUri = it.getString(it.getColumnIndexOrThrow("avatar_uri"))

                    val rolEnum = if (idRol == 1L) RolUsuario.TECNICO_INGENIERO else RolUsuario.CLIENTE

                    val usuario = Usuario(
                        idUsuario = id,
                        rol = rolEnum,
                        rut = rut,
                        nombre = nombre,
                        correo = correo,
                        telefono = telefono ?: "",
                        passwordHash = passwordHash,
                        activo = activo,
                        avatarUri = avatarUri
                    )
                    _uiState.update { it.copy(usuario = usuario) }
                }
            }
        }
    }



    // ya no es necesario
    //fun actualizarUsuario(usuario: Usuario?) {
    //    _uiState.update { it.copy(usuario = usuario) }
    //}

    fun cerrarSesion() {
        preferencias.borrarSesion()
        _uiState.update { it.copy(usuario = null) }
    }
}
*/