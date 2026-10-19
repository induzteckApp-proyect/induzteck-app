package com.example.induzteckapp.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class InduztekDbHelper(context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        private const val NOMBRE_BD = "induztek.db"
        private const val VERSION_BD = 1

        // Tablas del Sistema
        const val TABLA_ROL = "rol"
        const val TABLA_PERMISO = "permiso"
        const val TABLA_ROL_PERMISO = "rol_permiso"
        const val TABLA_USUARIO = "usuario"
        const val TABLA_REGION = "region"
        const val TABLA_COMUNA = "comuna"
        const val TABLA_INSTALACION = "instalacion"
        const val TABLA_TIPO_EQUIPO = "tipo_equipo"
        const val TABLA_EQUIPO = "equipo"
        const val TABLA_ESTADO_PROTOCOLO = "estado_protocolo"
        const val TABLA_PROTOCOLO = "protocolo"
        const val TABLA_TIPO_PRUEBA = "tipo_prueba"
        const val TABLA_PARAMETRO = "parametro"
        const val TABLA_PRUEBA = "prueba"
        const val TABLA_MEDICION = "medicion"

        // Sentencias SQL de Creación (DDL)
        private const val SQL_CREAR_ROL = """
            CREATE TABLE $TABLA_ROL (
                id_rol INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo TEXT UNIQUE NOT NULL,
                nombre TEXT NOT NULL
            )
        """

        private const val SQL_CREAR_PERMISO = """
            CREATE TABLE $TABLA_PERMISO (
                id_permiso INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo TEXT UNIQUE NOT NULL,
                descripcion TEXT
            )
        """

        private const val SQL_CREAR_ROL_PERMISO = """
            CREATE TABLE $TABLA_ROL_PERMISO (
                id_rol INTEGER NOT NULL,
                id_permiso INTEGER NOT NULL,
                PRIMARY KEY (id_rol, id_permiso),
                FOREIGN KEY(id_rol) REFERENCES $TABLA_ROL(id_rol),
                FOREIGN KEY(id_permiso) REFERENCES $TABLA_PERMISO(id_permiso)
            )
        """

        private const val SQL_CREAR_USUARIO = """
            CREATE TABLE $TABLA_USUARIO (
                id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
                id_rol INTEGER NOT NULL,
                rut TEXT UNIQUE NOT NULL,
                nombre TEXT NOT NULL,
                correo TEXT UNIQUE NOT NULL,
                telefono TEXT,
                password_hash TEXT NOT NULL,
                activo INTEGER NOT NULL DEFAULT 1,
                giro TEXT,
                contacto_nombre TEXT,
                contacto_cargo TEXT,
                avatar_uri TEXT,
                FOREIGN KEY(id_rol) REFERENCES $TABLA_ROL(id_rol)
            )
        """

        private const val SQL_CREAR_REGION = """
            CREATE TABLE $TABLA_REGION (
                id_region INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL
            )
        """

        private const val SQL_CREAR_COMUNA = """
            CREATE TABLE $TABLA_COMUNA (
                id_comuna INTEGER PRIMARY KEY AUTOINCREMENT,
                id_region INTEGER NOT NULL,
                nombre TEXT NOT NULL,
                FOREIGN KEY(id_region) REFERENCES $TABLA_REGION(id_region)
            )
        """

        private const val SQL_CREAR_INSTALACION = """
            CREATE TABLE $TABLA_INSTALACION (
                id_instalacion INTEGER PRIMARY KEY AUTOINCREMENT,
                id_usuario_cliente INTEGER NOT NULL,
                nombre TEXT NOT NULL,
                ubicacion TEXT,
                id_comuna INTEGER NOT NULL,
                observaciones_acceso TEXT,
                FOREIGN KEY(id_usuario_cliente) REFERENCES $TABLA_USUARIO(id_usuario),
                FOREIGN KEY(id_comuna) REFERENCES $TABLA_COMUNA(id_comuna)
            )
        """

        private const val SQL_CREAR_TIPO_EQUIPO = """
            CREATE TABLE $TABLA_TIPO_EQUIPO (
                id_tipo_equipo INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT UNIQUE NOT NULL,
                clasificacion TEXT NOT NULL
            )
        """

        private const val SQL_CREAR_EQUIPO = """
            CREATE TABLE $TABLA_EQUIPO (
                id_equipo INTEGER PRIMARY KEY AUTOINCREMENT,
                id_instalacion INTEGER NOT NULL,
                tag TEXT NOT NULL,
                id_tipo_equipo INTEGER NOT NULL,
                marca TEXT,
                modelo TEXT,
                numero_serie TEXT,
                potencia_kva REAL,
                tensiones REAL,
                corriente REAL,
                max_contactos_uohm REAL,
                FOREIGN KEY(id_instalacion) REFERENCES $TABLA_INSTALACION(id_instalacion),
                FOREIGN KEY(id_tipo_equipo) REFERENCES $TABLA_TIPO_EQUIPO(id_tipo_equipo),
                UNIQUE(id_instalacion, tag)
            )
        """

        private const val SQL_CREAR_ESTADO_PROTOCOLO = """
            CREATE TABLE $TABLA_ESTADO_PROTOCOLO (
                id_estado INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo TEXT UNIQUE NOT NULL,
                nombre TEXT NOT NULL
            )
        """

        private const val SQL_CREAR_PROTOCOLO = """
            CREATE TABLE $TABLA_PROTOCOLO (
                id_protocolo INTEGER PRIMARY KEY AUTOINCREMENT,
                numero TEXT UNIQUE NOT NULL,
                id_equipo INTEGER NOT NULL,
                id_usuario_registra INTEGER NOT NULL,
                fecha_hora_prueba TEXT NOT NULL,
                temperatura_c REAL NOT NULL,
                humedad_pct REAL NOT NULL,
                observaciones TEXT,
                id_estado INTEGER NOT NULL,
                id_protocolo_reemplazado INTEGER,
                FOREIGN KEY(id_equipo) REFERENCES $TABLA_EQUIPO(id_equipo),
                FOREIGN KEY(id_usuario_registra) REFERENCES $TABLA_USUARIO(id_usuario),
                FOREIGN KEY(id_estado) REFERENCES $TABLA_ESTADO_PROTOCOLO(id_estado),
                FOREIGN KEY(id_protocolo_reemplazado) REFERENCES $TABLA_PROTOCOLO(id_protocolo)
            )
        """

        private const val SQL_CREAR_TIPO_PRUEBA = """
            CREATE TABLE $TABLA_TIPO_PRUEBA (
                id_tipo_prueba INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL
            )
        """

        private const val SQL_CREAR_PARAMETRO = """
            CREATE TABLE $TABLA_PARAMETRO (
                id_parametro INTEGER PRIMARY KEY AUTOINCREMENT,
                id_tipo_prueba INTEGER NOT NULL,
                codigo TEXT NOT NULL,
                nombre TEXT NOT NULL,
                unidad TEXT NOT NULL,
                FOREIGN KEY(id_tipo_prueba) REFERENCES $TABLA_TIPO_PRUEBA(id_tipo_prueba)
            )
        """

        private const val SQL_CREAR_PRUEBA = """
            CREATE TABLE $TABLA_PRUEBA (
                id_prueba INTEGER PRIMARY KEY AUTOINCREMENT,
                id_protocolo INTEGER NOT NULL,
                id_tipo_prueba INTEGER NOT NULL,
                elemento TEXT,
                momento TEXT NOT NULL,
                realizada INTEGER NOT NULL,
                motivo_no_realizada TEXT,
                id_resultado INTEGER,
                FOREIGN KEY(id_protocolo) REFERENCES $TABLA_PROTOCOLO(id_protocolo),
                FOREIGN KEY(id_tipo_prueba) REFERENCES $TABLA_TIPO_PRUEBA(id_tipo_prueba)
            )
        """

        private const val SQL_CREAR_MEDICION = """
            CREATE TABLE $TABLA_MEDICION (
                id_medicion INTEGER PRIMARY KEY AUTOINCREMENT,
                id_prueba INTEGER NOT NULL,
                id_parametro INTEGER NOT NULL,
                valor REAL NOT NULL,
                FOREIGN KEY(id_prueba) REFERENCES $TABLA_PRUEBA(id_prueba),
                FOREIGN KEY(id_parametro) REFERENCES $TABLA_PARAMETRO(id_parametro),
                UNIQUE(id_prueba, id_parametro)
            )
        """
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREAR_ROL)
        db.execSQL(SQL_CREAR_PERMISO)
        db.execSQL(SQL_CREAR_ROL_PERMISO)
        db.execSQL(SQL_CREAR_USUARIO)
        db.execSQL(SQL_CREAR_REGION)
        db.execSQL(SQL_CREAR_COMUNA)
        db.execSQL(SQL_CREAR_INSTALACION)
        db.execSQL(SQL_CREAR_TIPO_EQUIPO)
        db.execSQL(SQL_CREAR_EQUIPO)
        db.execSQL(SQL_CREAR_ESTADO_PROTOCOLO)
        db.execSQL(SQL_CREAR_PROTOCOLO)
        db.execSQL(SQL_CREAR_TIPO_PRUEBA)
        db.execSQL(SQL_CREAR_PARAMETRO)
        db.execSQL(SQL_CREAR_PRUEBA)
        db.execSQL(SQL_CREAR_MEDICION)

        // 1. Datos Semilla: Roles y Permisos
        db.execSQL("INSERT INTO $TABLA_ROL (codigo, nombre) VALUES ('TECNICO', 'Técnico / Ingenieria'), ('CLIENTE', 'Usuario Cliente')")
        db.execSQL("INSERT INTO $TABLA_PERMISO (codigo, descripcion) VALUES ('LEER', 'Consultar datos e historiales'), ('CREACION', 'Registrar instalaciones, equipos y pruebas'), ('EDICION', 'Modificar borradores'), ('FORMALIZACION', 'Formalizar protocolos'), ('ELIMINACION', 'Anular registros'), ('AGREGAR_PERSONAL', 'Agregar una cuenta de tecnico/ingeniero')")

        db.execSQL("INSERT INTO $TABLA_ROL_PERMISO (id_rol, id_permiso) SELECT 1, id_permiso FROM $TABLA_PERMISO")
        db.execSQL("INSERT INTO $TABLA_ROL_PERMISO (id_rol, id_permiso) SELECT 2, id_permiso FROM $TABLA_PERMISO WHERE codigo = 'LEER'")

        // 2. Datos Semilla: Estados de Protocolo
        db.execSQL("INSERT INTO $TABLA_ESTADO_PROTOCOLO (codigo, nombre) VALUES ('BORRADOR', 'Borrador'), ('REGISTRADO_TERRENO', 'Registrado en terreno'), ('OBSERVADO', 'Observado'), ('FORMALIZADO', 'Formalizado'), ('ANULADO', 'Anulado')")

        // 3. Datos Semilla: Tipos de Equipo (Anexo 9)
        db.execSQL("INSERT INTO $TABLA_TIPO_EQUIPO (nombre, clasificacion) VALUES ('Transformador', 'Primario'), ('Celda MT', 'Secundario'), ('Interruptor', 'Primario'), ('Relé de protección', 'Secundario'), ('Cable de poder', 'Primario')")

        // 4. Datos Semilla: Regiones y Comunas de ejemplo
        db.execSQL("INSERT INTO $TABLA_REGION (nombre) VALUES ('Región Metropolitana')")
        db.execSQL("INSERT INTO $TABLA_COMUNA (id_region, nombre) VALUES (1, 'Lampa'), (1, 'San Bernardo'), (1, 'Til Til'), (1, 'La Florida'), (1, 'Puente Alto'), (1, 'Huechuraba')")

        // 5. Datos Semilla: Usuario Técnico de ejemplo (Password: "123456" con SHA-256)
        val hashPrueba = "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92"
        db.execSQL("INSERT INTO $TABLA_USUARIO (id_rol, rut, nombre, correo, telefono, password_hash, activo) VALUES (1, '17.284.519-3', 'Matías Godoy Herrera', 'mgodoy@induztek.ejemplo', '56900000101', '$hashPrueba', 1)")

        // 6. Datos Semilla: Cliente e Instalación (Anexo 9)
        db.execSQL("INSERT INTO $TABLA_USUARIO (id_rol, rut, nombre, correo, telefono, password_hash, activo, giro, contacto_nombre, contacto_cargo) VALUES (2, '76.482.915-8', 'Envasadora del Pacífico S.A.', 'mfuentes@envpacifico.ejemplo', '56900000101', '$hashPrueba', 1, 'Agroindustria', 'Marcela Fuentes Rojas', 'Jefe de Planta')")
        db.execSQL("INSERT INTO $TABLA_INSTALACION (id_usuario_cliente, nombre, ubicacion, id_comuna, observaciones_acceso) VALUES (2, 'Planta Lampa', 'Sector industrial', 1, 'Inducción obligatoria de 30 min')")

        // 7. Datos Semilla: Equipo de ejemplo (Transformador TR-01)
        db.execSQL("INSERT INTO $TABLA_EQUIPO (id_instalacion, tag, id_tipo_equipo, marca, modelo, numero_serie, potencia_kva, tensiones, corriente) VALUES (1, 'TR-01', 1, 'Rhona', 'RH-18733', '2012', 1500.0, 12.0, 0.0)")

        // 8. Datos Semilla: Tipos de Prueba y Parámetros (Para las validaciones de la HU-04 y HU-05)
        db.execSQL("INSERT INTO $TABLA_TIPO_PRUEBA (nombre) VALUES ('Resistencia de Aislación')") // id_tipo_prueba = 1
        db.execSQL("INSERT INTO $TABLA_PARAMETRO (id_tipo_prueba, codigo, nombre, unidad) VALUES (1, 'T_PRUEBA', 'Tensión de prueba', 'kV')")     // id_parametro = 1
        db.execSQL("INSERT INTO $TABLA_PARAMETRO (id_tipo_prueba, codigo, nombre, unidad) VALUES (1, 'LEC_1MIN', 'Lectura a 1 min', 'MΩ')")       // id_parametro = 2
        db.execSQL("INSERT INTO $TABLA_PARAMETRO (id_tipo_prueba, codigo, nombre, unidad) VALUES (1, 'LEC_10MIN', 'Lectura a 10 min', 'MΩ')")    // id_parametro = 3

        // 9. Datos Semilla: Protocolo de ejemplo formalizado (Para que el cliente e ingeniero vean historial inicial - HU-02 / HU-03)
        db.execSQL("INSERT INTO $TABLA_PROTOCOLO (numero, id_equipo, id_usuario_registra, fecha_hora_prueba, temperatura_c, humedad_pct, observaciones, id_estado) VALUES ('PT-2026-0401', 1, 1, '2026-04-10 10:30:00', 22.5, 55.0, 'Prueba inicial de transformador satisfactoria.', 4)") // id_estado 4 = Formalizado

        // 10. Datos Semilla: Prueba y Mediciones asociadas al Protocolo de ejemplo (Cumple con RN-06 para cálculo de IP = 10980 / 6100 = 1.80 -> Observado)
        db.execSQL("INSERT INTO $TABLA_PRUEBA (id_protocolo, id_tipo_prueba, elemento, momento, realizada, id_resultado) VALUES (1, 1, 'Devanado AT-BT', 'UNICA', 1, 2)") // id_resultado 2 = Observado
        db.execSQL("INSERT INTO $TABLA_MEDICION (id_prueba, id_parametro, valor) VALUES (1, 1, 5.0)")  // 5.0 kV
        db.execSQL("INSERT INTO $TABLA_MEDICION (id_prueba, id_parametro, valor) VALUES (1, 2, 6100.0)") // 6100 MΩ (Lectura 1 min)
        db.execSQL("INSERT INTO $TABLA_MEDICION (id_prueba, id_parametro, valor) VALUES (1, 3, 10980.0)") // 10980 MΩ (Lectura 10 min)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // En desarrollo académico se puede realizar re-creación o control incremental de versiones.
    }

    // ==========================================
    // MÉTODOS ÚTILES PARA CONSULTAS (HU-01 a HU-03)
    // ==========================================

    fun buscarUsuarioPorCorreo(correo: String): Cursor {
        return readableDatabase.rawQuery(
            "SELECT * FROM $TABLA_USUARIO WHERE correo = ?",
            arrayOf(correo)
        )
    }

    fun actualizarAvatarUsuario(idUsuario: Long, avatarUri: String): Int {
        val valores = ContentValues().apply {
            put("avatar_uri", avatarUri)
        }
        return writableDatabase.update(
            TABLA_USUARIO,
            valores,
            "id_usuario = ?",
            arrayOf(idUsuario.toString())
        )
    }

    fun actualizarPassword(correo: String, nuevoHash: String): Int {
        val valores = ContentValues().apply {
            put("password_hash", nuevoHash)
        }
        return writableDatabase.update(
            TABLA_USUARIO,
            valores,
            "correo = ?",
            arrayOf(correo)
        )
    }
}