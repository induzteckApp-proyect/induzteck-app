package com.example.induzteckapp.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.induzteckapp.model.RolUsuario
import com.example.induzteckapp.model.Usuario

/**
 * Puente temporal para las claves foráneas de los módulos que aún usan SQLite.
 * No autentica usuarios ni guarda contraseñas: la autenticación vive en el microservicio.
 */
class UsuarioReferenciaLocal(context: Context) {
    private val helper = InduztekDbHelper(context.applicationContext)

    fun obtenerOCrear(usuario: Usuario): Long {
        // ADMIN no utiliza las tablas locales de protocolos/equipos.
        if (usuario.rol == RolUsuario.ADMIN) return usuario.idUsuario ?: -1L

        val db = helper.writableDatabase
        var transaccionIniciada = false
        try {
            db.beginTransaction()
            transaccionIniciada = true
            val existente = db.rawQuery(
                "SELECT id_usuario FROM ${InduztekDbHelper.TABLA_USUARIO} WHERE correo = ? LIMIT 1",
                arrayOf(usuario.correo)
            ).use { cursor ->
                if (cursor.moveToFirst()) cursor.getLong(0) else null
            } ?: db.rawQuery(
                "SELECT id_usuario FROM ${InduztekDbHelper.TABLA_USUARIO} WHERE REPLACE(rut, '.', '') = ? LIMIT 1",
                arrayOf(usuario.rut)
            ).use { cursor ->
                if (cursor.moveToFirst()) cursor.getLong(0) else null
            }
            if (existente != null) {
                val actualizacion = ContentValues().apply {
                    put("nombre", usuario.nombre)
                    put("apellido", usuario.apellido)
                    put("correo", usuario.correo)
                    put("telefono", usuario.telefono)
                    put("giro", usuario.giro)
                    put("contacto_nombre", usuario.contactoNombre)
                    put("contacto_cargo", usuario.contactoCargo)
                }
                db.update(
                    InduztekDbHelper.TABLA_USUARIO,
                    actualizacion,
                    "id_usuario = ?",
                    arrayOf(existente.toString())
                )
                db.setTransactionSuccessful()
                return existente
            }

            val codigoRol = usuario.rol.codigo
            val idRol = db.rawQuery(
                "SELECT id_rol FROM ${InduztekDbHelper.TABLA_ROL} WHERE codigo = ? LIMIT 1",
                arrayOf(codigoRol)
            ).use { cursor ->
                if (cursor.moveToFirst()) cursor.getLong(0) else null
            } ?: throw IllegalStateException("No existe el rol local $codigoRol para relacionar los datos de prueba.")

            val valores = ContentValues().apply {
                put("id_rol", idRol)
                put("rut", usuario.rut)
                put("nombre", usuario.nombre)
                put("apellido", usuario.apellido)
                put("correo", usuario.correo)
                put("telefono", usuario.telefono)
                // Marcador local no utilizable como contraseña. Nunca se consulta para autenticar.
                put("password_hash", "AUTENTICACION_EN_MICROSERVICIO")
                put("activo", 1)
                put("giro", usuario.giro)
                put("contacto_nombre", usuario.contactoNombre)
                put("contacto_cargo", usuario.contactoCargo)
            }
            db.insertWithOnConflict(
                InduztekDbHelper.TABLA_USUARIO, null, valores, SQLiteDatabase.CONFLICT_IGNORE
            )

            val nuevoId = db.rawQuery(
                "SELECT id_usuario FROM ${InduztekDbHelper.TABLA_USUARIO} WHERE correo = ? LIMIT 1",
                arrayOf(usuario.correo)
            ).use { cursor ->
                if (cursor.moveToFirst()) cursor.getLong(0) else null
            } ?: throw IllegalStateException("No fue posible crear la referencia local del usuario.")

            db.setTransactionSuccessful()
            return nuevoId
        } finally {
            if (transaccionIniciada) db.endTransaction()
            helper.close()
        }
    }
}
