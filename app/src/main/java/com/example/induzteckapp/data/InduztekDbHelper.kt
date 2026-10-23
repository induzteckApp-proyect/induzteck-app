
package com.example.induzteckapp.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri

import com.example.induzteckapp.model.RolUsuario
import com.example.induzteckapp.model.Usuario

import com.example.induzteckapp.model.ClienteProtocolo
import com.example.induzteckapp.model.Comuna
import com.example.induzteckapp.model.Equipo
import com.example.induzteckapp.model.Instalacion
import com.example.induzteckapp.model.TipoEquipo
import com.example.induzteckapp.model.TipoPrueba

import android.database.sqlite.SQLiteConstraintException

class InduztekDbHelper(context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    private val contexto = context.applicationContext

    companion object {

        private const val NOMBRE_BD = "induztek.db"
        private const val VERSION_BD = 2

        // =====================================================
        // TABLAS
        // =====================================================

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
        const val TABLA_TIPO_PRUEBA_EQUIPO = "tipo_prueba_equipo"
        const val TABLA_PARAMETRO = "parametro"
        const val TABLA_PRUEBA = "prueba"
        const val TABLA_MEDICION = "medicion"

        // Resultados de las pruebas.
        // 1 = Aprobado, 2 = Observado, 3 = Rechazado.
        const val RESULTADO_APROBADO = 1
        const val RESULTADO_OBSERVADO = 2
        const val RESULTADO_RECHAZADO = 3

        // =====================================================
        // SENTENCIAS SQL: CREACIÓN DE TABLAS
        // =====================================================

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
                FOREIGN KEY(id_rol)
                    REFERENCES $TABLA_ROL(id_rol),
                FOREIGN KEY(id_permiso)
                    REFERENCES $TABLA_PERMISO(id_permiso)
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
                avatar BLOB,
                FOREIGN KEY(id_rol)
                    REFERENCES $TABLA_ROL(id_rol)
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
                FOREIGN KEY(id_region)
                    REFERENCES $TABLA_REGION(id_region)
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
                FOREIGN KEY(id_usuario_cliente)
                    REFERENCES $TABLA_USUARIO(id_usuario),
                FOREIGN KEY(id_comuna)
                    REFERENCES $TABLA_COMUNA(id_comuna)
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
                FOREIGN KEY(id_instalacion)
                    REFERENCES $TABLA_INSTALACION(id_instalacion),
                FOREIGN KEY(id_tipo_equipo)
                    REFERENCES $TABLA_TIPO_EQUIPO(id_tipo_equipo),
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
                FOREIGN KEY(id_equipo)
                    REFERENCES $TABLA_EQUIPO(id_equipo),
                FOREIGN KEY(id_usuario_registra)
                    REFERENCES $TABLA_USUARIO(id_usuario),
                FOREIGN KEY(id_estado)
                    REFERENCES $TABLA_ESTADO_PROTOCOLO(id_estado),
                FOREIGN KEY(id_protocolo_reemplazado)
                    REFERENCES $TABLA_PROTOCOLO(id_protocolo)
            )
        """

        private const val SQL_CREAR_TIPO_PRUEBA = """
            CREATE TABLE $TABLA_TIPO_PRUEBA (
                id_tipo_prueba INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL
            )
        """

        private const val SQL_CREAR_TIPO_PRUEBA_EQUIPO = """
            CREATE TABLE $TABLA_TIPO_PRUEBA_EQUIPO (
                id_tipo_equipo INTEGER NOT NULL,
                id_tipo_prueba INTEGER NOT NULL,
                PRIMARY KEY (id_tipo_equipo, id_tipo_prueba),
                FOREIGN KEY(id_tipo_equipo)
                    REFERENCES $TABLA_TIPO_EQUIPO(id_tipo_equipo),
                FOREIGN KEY(id_tipo_prueba)
                    REFERENCES $TABLA_TIPO_PRUEBA(id_tipo_prueba)
            )
        """

        private const val SQL_CREAR_PARAMETRO = """
            CREATE TABLE $TABLA_PARAMETRO (
                id_parametro INTEGER PRIMARY KEY AUTOINCREMENT,
                id_tipo_prueba INTEGER NOT NULL,
                codigo TEXT NOT NULL,
                nombre TEXT NOT NULL,
                unidad TEXT NOT NULL,
                FOREIGN KEY(id_tipo_prueba)
                    REFERENCES $TABLA_TIPO_PRUEBA(id_tipo_prueba)
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
                FOREIGN KEY(id_protocolo)
                    REFERENCES $TABLA_PROTOCOLO(id_protocolo),
                FOREIGN KEY(id_tipo_prueba)
                    REFERENCES $TABLA_TIPO_PRUEBA(id_tipo_prueba)
            )
        """

        private const val SQL_CREAR_MEDICION = """
            CREATE TABLE $TABLA_MEDICION (
                id_medicion INTEGER PRIMARY KEY AUTOINCREMENT,
                id_prueba INTEGER NOT NULL,
                id_parametro INTEGER NOT NULL,
                valor REAL NOT NULL,
                FOREIGN KEY(id_prueba)
                    REFERENCES $TABLA_PRUEBA(id_prueba),
                FOREIGN KEY(id_parametro)
                    REFERENCES $TABLA_PARAMETRO(id_parametro),
                UNIQUE(id_prueba, id_parametro)
            )
        """

        // =====================================================
        // SENTENCIAS SQL: DATOS SEMILLA
        // =====================================================

        private const val SQL_PRECARGAR_ROLES = """
            INSERT OR IGNORE INTO $TABLA_ROL (codigo, nombre)
            VALUES
                ('TECNICO', 'Técnico / Ingeniero'),
                ('CLIENTE', 'Usuario Cliente')
        """

        private const val SQL_PRECARGAR_PERMISOS = """
            INSERT OR IGNORE INTO $TABLA_PERMISO
                (codigo, descripcion)
            VALUES
                ('LEER', 'Consultar datos e historiales'),
                ('CREACION', 'Registrar instalaciones, equipos y pruebas'),
                ('EDICION', 'Modificar borradores'),
                ('FORMALIZACION', 'Formalizar protocolos'),
                ('ELIMINACION', 'Anular registros'),
                ('AGREGAR_PERSONAL', 'Agregar una cuenta de técnico/ingeniero')
        """

        private const val SQL_PRECARGAR_PERMISOS_TECNICO = """
            INSERT OR IGNORE INTO $TABLA_ROL_PERMISO
                (id_rol, id_permiso)
            SELECT r.id_rol, p.id_permiso
            FROM $TABLA_ROL r
            CROSS JOIN $TABLA_PERMISO p
            WHERE r.codigo = 'TECNICO'
        """

        private const val SQL_PRECARGAR_PERMISOS_CLIENTE = """
            INSERT OR IGNORE INTO $TABLA_ROL_PERMISO
                (id_rol, id_permiso)
            SELECT r.id_rol, p.id_permiso
            FROM $TABLA_ROL r
            INNER JOIN $TABLA_PERMISO p
                ON p.codigo = 'LEER'
            WHERE r.codigo = 'CLIENTE'
        """

        private const val SQL_PRECARGAR_ESTADOS = """
            INSERT OR IGNORE INTO $TABLA_ESTADO_PROTOCOLO
                (codigo, nombre)
            VALUES
                ('BORRADOR', 'Borrador'),
                ('REGISTRADO_TERRENO', 'Registrado en terreno'),
                ('OBSERVADO', 'Observado'),
                ('FORMALIZADO', 'Formalizado'),
                ('ANULADO', 'Anulado')
        """

        private const val SQL_PRECARGAR_TIPOS_EQUIPO = """
            INSERT OR IGNORE INTO $TABLA_TIPO_EQUIPO
                (nombre, clasificacion)
            VALUES
                ('Transformador', 'Primario'),
                ('Celda MT', 'Secundario'),
                ('Interruptor', 'Primario'),
                ('Relé de protección', 'Secundario'),
                ('Cable de poder', 'Primario')
        """

        private const val SQL_INSERTAR_USUARIO = """
            INSERT INTO $TABLA_USUARIO (
                id_rol, rut, nombre, correo, telefono,
                password_hash, activo, giro,
                contacto_nombre, contacto_cargo
            )
            SELECT
                r.id_rol, ?, ?, ?, ?, ?, 1, ?, ?, ?
            FROM $TABLA_ROL r
            WHERE r.codigo = ?
              AND NOT EXISTS (
                  SELECT 1
                  FROM $TABLA_USUARIO u
                  WHERE u.rut = ? OR u.correo = ?
              )
        """

        private const val SQL_INSERTAR_REGION = """
            INSERT INTO $TABLA_REGION (nombre)
            SELECT ?
            WHERE NOT EXISTS (
                SELECT 1
                FROM $TABLA_REGION
                WHERE nombre = ?
            )
        """

        private const val SQL_INSERTAR_COMUNA = """
            INSERT INTO $TABLA_COMUNA (id_region, nombre)
            SELECT r.id_region, ?
            FROM $TABLA_REGION r
            WHERE r.nombre = ?
              AND NOT EXISTS (
                  SELECT 1
                  FROM $TABLA_COMUNA c
                  WHERE c.nombre = ?
                    AND c.id_region = r.id_region
              )
        """

        private const val SQL_INSERTAR_INSTALACION = """
            INSERT INTO $TABLA_INSTALACION (
                id_usuario_cliente, nombre, ubicacion,
                id_comuna, observaciones_acceso
            )
            SELECT
                u.id_usuario, ?, ?, c.id_comuna, ?
            FROM $TABLA_USUARIO u
            CROSS JOIN $TABLA_COMUNA c
            INNER JOIN $TABLA_REGION r
                ON r.id_region = c.id_region
            WHERE u.rut = ?
              AND c.nombre = ?
              AND r.nombre = ?
              AND NOT EXISTS (
                  SELECT 1
                  FROM $TABLA_INSTALACION i
                  WHERE i.id_usuario_cliente = u.id_usuario
                    AND i.nombre = ?
              )
        """

        private const val SQL_INSERTAR_EQUIPO = """
            INSERT INTO $TABLA_EQUIPO (
                id_instalacion, tag, id_tipo_equipo,
                marca, modelo, numero_serie,
                potencia_kva, tensiones, corriente,
                max_contactos_uohm
            )
            SELECT
                i.id_instalacion, ?, t.id_tipo_equipo,
                ?, ?, ?, ?, ?, ?, ?
            FROM $TABLA_INSTALACION i
            INNER JOIN $TABLA_TIPO_EQUIPO t
                ON t.nombre = ?
            WHERE i.nombre = ?
              AND NOT EXISTS (
                  SELECT 1
                  FROM $TABLA_EQUIPO e
                  WHERE e.id_instalacion = i.id_instalacion
                    AND e.tag = ?
              )
        """

        private const val SQL_INSERTAR_PROTOCOLO = """
            INSERT INTO $TABLA_PROTOCOLO (
                numero, id_equipo, id_usuario_registra,
                fecha_hora_prueba, temperatura_c,
                humedad_pct, observaciones, id_estado
            )
            SELECT
                ?, e.id_equipo, u.id_usuario,
                ?, ?, ?, ?, s.id_estado
            FROM $TABLA_EQUIPO e
            INNER JOIN $TABLA_INSTALACION i
                ON i.id_instalacion = e.id_instalacion
            INNER JOIN $TABLA_USUARIO u
                ON u.rut = ?
            INNER JOIN $TABLA_ESTADO_PROTOCOLO s
                ON s.codigo = ?
            WHERE e.tag = ?
              AND i.nombre = ?
              AND NOT EXISTS (
                  SELECT 1
                  FROM $TABLA_PROTOCOLO p
                  WHERE p.numero = ?
              )
        """
    }

    // =========================================================
    // CONFIGURACIÓN Y CICLO DE VIDA DE LA BASE DE DATOS
    // =========================================================

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
        db.execSQL(SQL_CREAR_TIPO_PRUEBA_EQUIPO)
        db.execSQL(SQL_CREAR_PARAMETRO)
        db.execSQL(SQL_CREAR_PRUEBA)
        db.execSQL(SQL_CREAR_MEDICION)

        precargarDatos(db)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {

        if (oldVersion < 2) {

            // Se conserva toda la información existente.
            // Solo se agrega la columna BLOB si aún no existe.
            if (!existeColumna(db, TABLA_USUARIO, "avatar")) {
                db.execSQL(
                    "ALTER TABLE $TABLA_USUARIO ADD COLUMN avatar BLOB"
                )
            }

            // Crear la nueva tabla de compatibilidad sin duplicarla.
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS $TABLA_TIPO_PRUEBA_EQUIPO (
                    id_tipo_equipo INTEGER NOT NULL,
                    id_tipo_prueba INTEGER NOT NULL,
                    PRIMARY KEY (id_tipo_equipo, id_tipo_prueba),
                    FOREIGN KEY(id_tipo_equipo)
                        REFERENCES $TABLA_TIPO_EQUIPO(id_tipo_equipo),
                    FOREIGN KEY(id_tipo_prueba)
                        REFERENCES $TABLA_TIPO_PRUEBA(id_tipo_prueba)
                )
                """.trimIndent()
            )

            // Completar datos de demostración sin recrear las tablas.
            precargarDatos(db)
        }
    }

    private fun existeColumna(
        db: SQLiteDatabase,
        tabla: String,
        columna: String
    ): Boolean {

        return db.rawQuery(
            "PRAGMA table_info($tabla)",
            null
        ).use { cursor ->

            var existe = false

            while (cursor.moveToNext()) {
                if (cursor.getString(
                        cursor.getColumnIndexOrThrow("name")
                    ) == columna
                ) {
                    existe = true
                    break
                }
            }

            existe
        }
    }

    // =========================================================
    // PRECARGA GENERAL
    // =========================================================

    private fun precargarDatos(db: SQLiteDatabase) {

        db.execSQL(SQL_PRECARGAR_ROLES)
        db.execSQL(SQL_PRECARGAR_PERMISOS)
        db.execSQL(SQL_PRECARGAR_PERMISOS_TECNICO)
        db.execSQL(SQL_PRECARGAR_PERMISOS_CLIENTE)
        db.execSQL(SQL_PRECARGAR_ESTADOS)
        db.execSQL(SQL_PRECARGAR_TIPOS_EQUIPO)

        precargarRegionesYComunas(db)
        precargarUsuarios(db)
        precargarInstalaciones(db)
        precargarEquipos(db)
        precargarTiposPrueba(db)
        precargarCompatibilidadPruebas(db)
        precargarProtocolos(db)
        precargarPruebasYMediciones(db)
    }

    // =========================================================
    // REGIONES Y COMUNAS
    // =========================================================

    private fun precargarRegionesYComunas(db: SQLiteDatabase) {

        val regiones = listOf(
            "Región Metropolitana" to listOf(
                "Lampa",
                "San Bernardo",
                "Til Til",
                "La Florida",
                "Puente Alto",
                "Huechuraba"
            ),
            "Región de Valparaíso" to listOf(
                "Viña del Mar",
                "Valparaíso",
                "Quilpué",
                "Concón",
                "Los Andes"
            ),
            "Región del Biobío" to listOf(
                "Concepción",
                "Talcahuano",
                "Coronel"
            ),
            "Región de O'Higgins" to listOf(
                "Rancagua",
                "Machalí",
                "San Fernando"
            ),
            "Región del Maule" to listOf(
                "Talca",
                "Curicó",
                "Linares"
            )
        )

        regiones.forEach { (region, comunas) ->

            db.execSQL(
                SQL_INSERTAR_REGION,
                arrayOf(region, region)
            )

            comunas.forEach { comuna ->
                db.execSQL(
                    SQL_INSERTAR_COMUNA,
                    arrayOf(comuna, region, comuna)
                )
            }
        }
    }

    // =========================================================
    // USUARIOS DE DEMOSTRACIÓN
    // =========================================================

    private fun precargarUsuarios(db: SQLiteDatabase) {

        // SHA-256 de la contraseña de demostración: 123456.
        val hashPrueba =
            "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92"

        // Se conservan los correos originales de las cuentas demo
        // para no romper el acceso que ya utilizan los ViewModels.

        insertarUsuario(
            db, "17.284.519-3", "Matías Godoy Herrera",
            "tecnico@induztek.cl", "56900000101", hashPrueba,
            rol = "TECNICO"
        )

        insertarUsuario(
            db, "76.482.915-8", "Envasadora del Pacífico S.A.",
            "cliente@ejemplo.cl", "56900000101", hashPrueba,
            giro = "Agroindustria",
            contacto = "Marcela Fuentes Rojas",
            cargo = "Jefe de Planta",
            rol = "CLIENTE"
        )

        insertarUsuario(
            db, "96.731.540-0", "Sanitaria Valle Central S.A.",
            "jarancibia@svcentral.ejemplo", "56900000102",
            hashPrueba, giro = "Servicios sanitarios",
            contacto = "Jorge Arancibia Leiva",
            cargo = "Contacto de operaciones",
            rol = "CLIENTE"
        )

        insertarUsuario(
            db, "77.105.362-9", "Minera Cerro Alto SpA",
            "pnavarro@cerroalto.ejemplo", "56900000103",
            hashPrueba, giro = "Minería",
            contacto = "Patricio Navarro Soto",
            cargo = "Contacto de faena",
            rol = "CLIENTE"
        )

        insertarUsuario(
            db, "76.903.218-5", "Clínica Los Aromos Ltda.",
            "cmendez@losaromos.ejemplo", "56900000104",
            hashPrueba, giro = "Salud",
            contacto = "Carolina Méndez Pizarro",
            cargo = "Contacto de mantenimiento",
            rol = "CLIENTE"
        )

        insertarUsuario(
            db, "99.524.170-6", "Cementos Lomas Blancas S.A.",
            "lbravo@lomasblancas.ejemplo", "56900000105",
            hashPrueba, giro = "Construcción",
            contacto = "Luis Bravo Castillo",
            cargo = "Contacto de planta",
            rol = "CLIENTE"
        )

        insertarUsuario(
            db, "78.214.635-1", "Data Center Andes Norte SpA",
            "fordenes@andesnorte.ejemplo", "56900000106",
            hashPrueba, giro = "Tecnología",
            contacto = "Felipe Órdenes Vera",
            cargo = "Contacto de infraestructura",
            rol = "CLIENTE"
        )

        insertarUsuario(
            db, "19.412.087-7", "Javiera Tapia Muñoz",
            "jtapia@induztek.ejemplo", "56900000107",
            hashPrueba, rol = "TECNICO"
        )

        insertarUsuario(
            db, "26.104.583-4", "Carlos Rondón Pérez",
            "crondon@induztek.ejemplo", "56900000108",
            hashPrueba, rol = "TECNICO"
        )

        insertarUsuario(
            db, "20.137.648-3", "Diego Valenzuela Ríos",
            "dvalenzuela@induztek.ejemplo", "56900000109",
            hashPrueba, rol = "TECNICO"
        )

        insertarUsuario(
            db, "15.906.334-8", "Andrea Contreras Silva",
            "acontreras@induztek.ejemplo", "56900000110",
            hashPrueba, rol = "TECNICO"
        )

        insertarUsuario(
            db, "16.553.270-8", "Sebastián Morales Gutiérrez",
            "smorales@induztek.ejemplo", "56900000111",
            hashPrueba, rol = "TECNICO"
        )

        insertarUsuario(
            db, "13.871.905-7", "Ricardo Espinoza Lagos",
            "respinoza@induztek.ejemplo", "56900000112",
            hashPrueba, rol = "TECNICO"
        )
    }

    private fun insertarUsuario(
        db: SQLiteDatabase,
        rut: String,
        nombre: String,
        correo: String,
        telefono: String,
        hash: String,
        giro: String? = null,
        contacto: String? = null,
        cargo: String? = null,
        rol: String
    ) {

        db.execSQL(
            SQL_INSERTAR_USUARIO,
            arrayOf(
                rut,
                nombre,
                correo,
                telefono,
                hash,
                giro,
                contacto,
                cargo,
                rol,
                rut,
                correo
            )
        )
    }

    // =========================================================
    // INSTALACIONES
    // =========================================================

    private fun precargarInstalaciones(db: SQLiteDatabase) {

        insertarInstalacion(
            db,
            rutCliente = "76.482.915-8",
            nombre = "Planta Lampa",
            ubicacion = "Sector industrial",
            comuna = "Lampa",
            region = "Región Metropolitana",
            observaciones = "Inducción obligatoria de 30 min"
        )

        insertarInstalacion(
            db,
            rutCliente = "96.731.540-0",
            nombre = "Planta Elevadora Sur",
            ubicacion = "Sector rural poniente",
            comuna = "San Bernardo",
            region = "Región Metropolitana",
            observaciones = "Sala eléctrica subterránea, sin señal"
        )

        insertarInstalacion(
            db,
            rutCliente = "77.105.362-9",
            nombre = "Faena Til Til",
            ubicacion = "Acceso por Ruta 5 Norte",
            comuna = "Til Til",
            region = "Región Metropolitana",
            observaciones = "Sin señal en toda la faena"
        )

        insertarInstalacion(
            db,
            rutCliente = "76.903.218-5",
            nombre = "Edificio Clínico",
            ubicacion = "Zona urbana",
            comuna = "La Florida",
            region = "Región Metropolitana",
            observaciones = "Cortes solo domingos 06:00–10:00"
        )

        insertarInstalacion(
            db,
            rutCliente = "99.524.170-6",
            nombre = "Planta Molienda",
            ubicacion = "Sector precordillera",
            comuna = "Puente Alto",
            region = "Región Metropolitana",
            observaciones = "Uso obligatorio de respirador"
        )

        insertarInstalacion(
            db,
            rutCliente = "78.214.635-1",
            nombre = "Site Huechuraba",
            ubicacion = "Parque empresarial",
            comuna = "Huechuraba",
            region = "Región Metropolitana",
            observaciones = "Acompañamiento permanente del cliente"
        )
    }

    private fun insertarInstalacion(
        db: SQLiteDatabase,
        rutCliente: String,
        nombre: String,
        ubicacion: String,
        comuna: String,
        region: String,
        observaciones: String
    ) {

        db.execSQL(
            SQL_INSERTAR_INSTALACION,
            arrayOf(
                nombre,
                ubicacion,
                observaciones,
                rutCliente,
                comuna,
                region,
                nombre
            )
        )
    }

    // =========================================================
    // EQUIPOS
    // =========================================================

    private fun precargarEquipos(db: SQLiteDatabase) {

        insertarEquipo(
            db, "Planta Lampa", "TR-01", "Transformador",
            "Rhona", "RH-18733", "2012",
            1500.0, 12.0, 0.0
        )

        insertarEquipo(
            db, "Planta Lampa", "TR-02", "Transformador",
            "Rhona", "RH-21904", "2017",
            1000.0, 12.0, 0.0
        )

        insertarEquipo(
            db, "Planta Elevadora Sur", "TR-A", "Transformador",
            "Tusan", "TS-5521", "2009",
            2500.0, 23.0, 0.0
        )

        insertarEquipo(
            db, "Edificio Clínico", "CEL-MT-03", "Celda MT",
            "Ormazabal", "OR-77812", "2018",
            null, 12.0, 630.0
        )

        insertarEquipo(
            db, "Faena Til Til", "INT-52-1", "Interruptor",
            "Schneider", "SE-449021", "2015",
            null, 23.0, 1250.0, 45.0
        )

        insertarEquipo(
            db, "Planta Molienda", "REL-50/51-A",
            "Relé de protección",
            "SEL", "SEL-751-30219", "2020",
            null, null, 5.0
        )

        insertarEquipo(
            db, "Site Huechuraba", "CAB-MT-02", "Cable de poder",
            "Nexans", "NX-2201-B", "2019",
            null, 15.0, null
        )

        insertarEquipo(
            db, "Faena Til Til", "TR-SS-01", "Transformador",
            "ABB", "ABB-1180442", "2011",
            5000.0, 23.0, 0.0
        )
    }

    private fun insertarEquipo(
        db: SQLiteDatabase,
        instalacion: String,
        tag: String,
        tipo: String,
        marca: String,
        modelo: String,
        numeroSerie: String,
        potenciaKva: Double?,
        tensiones: Double?,
        corriente: Double?,
        maxContactos: Double? = null
    ) {

        db.execSQL(
            SQL_INSERTAR_EQUIPO,
            arrayOf(
                tag,
                marca,
                modelo,
                numeroSerie,
                potenciaKva,
                tensiones,
                corriente,
                maxContactos,
                tipo,
                instalacion,
                tag
            )
        )
    }

    // =========================================================
    // TIPOS DE PRUEBA Y PARÁMETROS
    // =========================================================

    private fun precargarTiposPrueba(db: SQLiteDatabase) {

        val tipos = listOf(
            "Resistencia de Aislación",
            "Relación de Transformación (TTR)",
            "Resistencia de Devanados",
            "Resistencia de Contactos",
            "Operación de Relé de Protección"
        )

        tipos.forEach { nombre ->
            db.execSQL(
                """
                INSERT INTO $TABLA_TIPO_PRUEBA (nombre)
                SELECT ?
                WHERE NOT EXISTS (
                    SELECT 1 FROM $TABLA_TIPO_PRUEBA
                    WHERE nombre = ?
                )
                """.trimIndent(),
                arrayOf(nombre, nombre)
            )
        }

        val parametros = listOf(
            Triple(
                "Resistencia de Aislación",
                "T_PRUEBA",
                "Tensión de prueba|kV"
            ),
            Triple(
                "Resistencia de Aislación",
                "LEC_1MIN",
                "Lectura a 1 min|MΩ"
            ),
            Triple(
                "Resistencia de Aislación",
                "LEC_10MIN",
                "Lectura a 10 min|MΩ"
            ),

            Triple(
                "Relación de Transformación (TTR)",
                "POS_TAP",
                "Posición de TAP|posición"
            ),
            Triple(
                "Relación de Transformación (TTR)",
                "REL_TEORICA",
                "Relación teórica|ratio"
            ),
            Triple(
                "Relación de Transformación (TTR)",
                "REL_MEDIDA",
                "Relación medida|ratio"
            ),

            Triple(
                "Resistencia de Devanados",
                "AT_BT",
                "Resistencia AT-BT|mΩ"
            ),
            Triple(
                "Resistencia de Devanados",
                "AT_T",
                "Resistencia AT-T|mΩ"
            ),
            Triple(
                "Resistencia de Devanados",
                "BT_T",
                "Resistencia BT-T|mΩ"
            ),

            Triple(
                "Resistencia de Contactos",
                "RES_CONTACTO",
                "Resistencia de contactos|µΩ"
            ),

            Triple(
                "Operación de Relé de Protección",
                "CORRIENTE_AJUSTE",
                "Corriente de ajuste|A"
            ),
            Triple(
                "Operación de Relé de Protección",
                "CORRIENTE_MEDIDA",
                "Corriente medida|A"
            ),
            Triple(
                "Operación de Relé de Protección",
                "TIEMPO_ESPERADO",
                "Tiempo esperado|ms"
            ),
            Triple(
                "Operación de Relé de Protección",
                "TIEMPO_MEDIDO",
                "Tiempo medido|ms"
            )
        )

        parametros.forEach { (tipo, codigo, detalle) ->

            val partes = detalle.split("|", limit = 2)
            val nombre = partes[0]
            val unidad = partes[1]

            db.execSQL(
                """
                INSERT INTO $TABLA_PARAMETRO (
                    id_tipo_prueba, codigo, nombre, unidad
                )
                SELECT tp.id_tipo_prueba, ?, ?, ?
                FROM $TABLA_TIPO_PRUEBA tp
                WHERE tp.nombre = ?
                  AND NOT EXISTS (
                      SELECT 1
                      FROM $TABLA_PARAMETRO p
                      WHERE p.id_tipo_prueba = tp.id_tipo_prueba
                        AND p.codigo = ?
                  )
                """.trimIndent(),
                arrayOf(codigo, nombre, unidad, tipo, codigo)
            )
        }
    }

    private fun precargarCompatibilidadPruebas(db: SQLiteDatabase) {

        val compatibilidades = listOf(
            "Transformador" to "Resistencia de Aislación",
            "Transformador" to "Relación de Transformación (TTR)",
            "Transformador" to "Resistencia de Devanados",
            "Celda MT" to "Resistencia de Aislación",
            "Celda MT" to "Resistencia de Contactos",
            "Interruptor" to "Resistencia de Aislación",
            "Interruptor" to "Resistencia de Contactos",
            "Relé de protección" to "Operación de Relé de Protección",
            "Cable de poder" to "Resistencia de Aislación"
        )

        compatibilidades.forEach { (equipo, prueba) ->

            db.execSQL(
                """
                INSERT OR IGNORE INTO $TABLA_TIPO_PRUEBA_EQUIPO (
                    id_tipo_equipo, id_tipo_prueba
                )
                SELECT te.id_tipo_equipo, tp.id_tipo_prueba
                FROM $TABLA_TIPO_EQUIPO te
                CROSS JOIN $TABLA_TIPO_PRUEBA tp
                WHERE te.nombre = ?
                  AND tp.nombre = ?
                """.trimIndent(),
                arrayOf(equipo, prueba)
            )
        }
    }

    // =========================================================
    // HISTORIAL DE PROTOCOLOS
    // =========================================================

    private fun precargarProtocolos(db: SQLiteDatabase) {

        insertarProtocolo(
            db,
            numero = "PT-2024-0312",
            equipo = "TR-01",
            instalacion = "Planta Lampa",
            rutTecnico = "17.284.519-3",
            fecha = "2024-09-14 10:30:00",
            temperatura = 18.0,
            humedad = 55.0,
            observaciones = "Protocolo histórico de transformador.",
            estado = "FORMALIZADO"
        )

        insertarProtocolo(
            db,
            numero = "PT-2025-0288",
            equipo = "TR-01",
            instalacion = "Planta Lampa",
            rutTecnico = "19.412.087-7",
            fecha = "2025-09-13 10:30:00",
            temperatura = 21.0,
            humedad = 48.0,
            observaciones = "Protocolo histórico de transformador.",
            estado = "FORMALIZADO"
        )

        // El número PT-2026-0401 ya existía en la semilla original.
        // Se conserva su identidad y se actualizan sus datos de demo
        // para coincidir con el historial que acordamos.
        insertarProtocolo(
            db,
            numero = "PT-2026-0401",
            equipo = "TR-01",
            instalacion = "Planta Lampa",
            rutTecnico = "17.284.519-3",
            fecha = "2026-09-26 10:30:00",
            temperatura = 19.0,
            humedad = 60.0,
            observaciones = "Lecturas de aislación con resultado observado.",
            estado = "REGISTRADO_TERRENO"
        )

        db.execSQL(
            """
            UPDATE $TABLA_PROTOCOLO
            SET fecha_hora_prueba = ?,
                temperatura_c = ?,
                humedad_pct = ?,
                observaciones = ?,
                id_estado = (
                    SELECT id_estado
                    FROM $TABLA_ESTADO_PROTOCOLO
                    WHERE codigo = ?
                )
            WHERE numero = ?
            """.trimIndent(),
            arrayOf(
                "2026-09-26 10:30:00",
                19.0,
                60.0,
                "Lecturas de aislación con resultado observado.",
                "REGISTRADO_TERRENO",
                "PT-2026-0401"
            )
        )

        insertarProtocolo(
            db,
            numero = "PT-2026-0402",
            equipo = "TR-02",
            instalacion = "Planta Lampa",
            rutTecnico = "17.284.519-3",
            fecha = "2026-09-26 11:30:00",
            temperatura = 19.0,
            humedad = 60.0,
            observaciones = "Protocolo de transformador.",
            estado = "REGISTRADO_TERRENO"
        )

        insertarProtocolo(
            db,
            numero = "PT-2026-0389",
            equipo = "CEL-MT-03",
            instalacion = "Edificio Clínico",
            rutTecnico = "26.104.583-4",
            fecha = "2026-09-20 09:00:00",
            temperatura = 22.0,
            humedad = 45.0,
            observaciones = "Resultado observado.",
            estado = "OBSERVADO"
        )

        insertarProtocolo(
            db,
            numero = "PT-2026-0377",
            equipo = "REL-50/51-A",
            instalacion = "Planta Molienda",
            rutTecnico = "20.137.648-3",
            fecha = "2026-09-15 10:00:00",
            temperatura = 24.0,
            humedad = 35.0,
            observaciones = "Protocolo formalizado.",
            estado = "FORMALIZADO"
        )

        insertarProtocolo(
            db,
            numero = "PT-2026-0360",
            equipo = "INT-52-1",
            instalacion = "Faena Til Til",
            rutTecnico = "19.412.087-7",
            fecha = "2026-09-08 08:00:00",
            temperatura = 27.0,
            humedad = 20.0,
            observaciones = "Resultado rechazado.",
            estado = "FORMALIZADO"
        )
    }

    private fun insertarProtocolo(
        db: SQLiteDatabase,
        numero: String,
        equipo: String,
        instalacion: String,
        rutTecnico: String,
        fecha: String,
        temperatura: Double,
        humedad: Double,
        observaciones: String,
        estado: String
    ) {

        db.execSQL(
            SQL_INSERTAR_PROTOCOLO,
            arrayOf(
                numero,
                fecha,
                temperatura,
                humedad,
                observaciones,
                rutTecnico,
                estado,
                equipo,
                instalacion,
                numero
            )
        )
    }

    // =========================================================
    // PRUEBAS Y MEDICIONES DE AISLACIÓN
    // =========================================================

    private fun precargarPruebasYMediciones(db: SQLiteDatabase) {

        insertarPruebaAislacion(
            db,
            "PT-2024-0312",
            1,
            12500.0,
            31250.0
        )

        insertarPruebaAislacion(
            db,
            "PT-2025-0288",
            1,
            9800.0,
            21560.0
        )

        insertarPruebaAislacion(
            db,
            "PT-2026-0401",
            2,
            6100.0,
            10980.0
        )

        insertarPruebaSimple(
            db, "PT-2026-0402",
            "Resistencia de Aislación", RESULTADO_APROBADO
        )

        insertarPruebaSimple(
            db, "PT-2026-0389",
            "Resistencia de Contactos", RESULTADO_OBSERVADO
        )

        insertarPruebaSimple(
            db, "PT-2026-0377",
            "Operación de Relé de Protección", RESULTADO_APROBADO
        )

        insertarPruebaSimple(
            db, "PT-2026-0360",
            "Resistencia de Contactos", RESULTADO_RECHAZADO
        )
    }

    private fun insertarPruebaAislacion(
        db: SQLiteDatabase,
        numeroProtocolo: String,
        resultado: Int,
        lectura1Min: Double,
        lectura10Min: Double
    ) {

        db.execSQL(
            """
            INSERT INTO $TABLA_PRUEBA (
                id_protocolo, id_tipo_prueba, elemento,
                momento, realizada, id_resultado
            )
            SELECT
                p.id_protocolo, tp.id_tipo_prueba,
                'Devanado AT-BT', 'UNICA', 1, ?
            FROM $TABLA_PROTOCOLO p
            CROSS JOIN $TABLA_TIPO_PRUEBA tp
            WHERE p.numero = ?
              AND tp.nombre = 'Resistencia de Aislación'
              AND NOT EXISTS (
                  SELECT 1
                  FROM $TABLA_PRUEBA pr
                  WHERE pr.id_protocolo = p.id_protocolo
                    AND pr.id_tipo_prueba = tp.id_tipo_prueba
                    AND pr.momento = 'UNICA'
              )
            """.trimIndent(),
            arrayOf(resultado, numeroProtocolo)
        )

        // Las mediciones se insertan solo si la prueba existe
        // y todavía no tiene ese parámetro registrado.
        insertarMedicion(
            db, numeroProtocolo, "T_PRUEBA", 5.0
        )

        insertarMedicion(
            db, numeroProtocolo, "LEC_1MIN", lectura1Min
        )

        insertarMedicion(
            db, numeroProtocolo, "LEC_10MIN", lectura10Min
        )
    }

    private fun insertarMedicion(
        db: SQLiteDatabase,
        numeroProtocolo: String,
        codigoParametro: String,
        valor: Double
    ) {

        db.execSQL(
            """
            INSERT INTO $TABLA_MEDICION (
                id_prueba, id_parametro, valor
            )
            SELECT pr.id_prueba, pa.id_parametro, ?
            FROM $TABLA_PROTOCOLO p
            INNER JOIN $TABLA_PRUEBA pr
                ON pr.id_protocolo = p.id_protocolo
            INNER JOIN $TABLA_TIPO_PRUEBA tp
                ON tp.id_tipo_prueba = pr.id_tipo_prueba
            INNER JOIN $TABLA_PARAMETRO pa
                ON pa.id_tipo_prueba = tp.id_tipo_prueba
            WHERE p.numero = ?
              AND tp.nombre = 'Resistencia de Aislación'
              AND pa.codigo = ?
              AND NOT EXISTS (
                  SELECT 1
                  FROM $TABLA_MEDICION m
                  WHERE m.id_prueba = pr.id_prueba
                    AND m.id_parametro = pa.id_parametro
              )
            """.trimIndent(),
            arrayOf(valor, numeroProtocolo, codigoParametro)
        )
    }

    private fun insertarPruebaSimple(
        db: SQLiteDatabase,
        numeroProtocolo: String,
        nombrePrueba: String,
        resultado: Int
    ) {

        db.execSQL(
            """
            INSERT INTO $TABLA_PRUEBA (
                id_protocolo, id_tipo_prueba, elemento,
                momento, realizada, id_resultado
            )
            SELECT
                p.id_protocolo, tp.id_tipo_prueba,
                NULL, 'UNICA', 1, ?
            FROM $TABLA_PROTOCOLO p
            INNER JOIN $TABLA_TIPO_PRUEBA tp
                ON tp.nombre = ?
            WHERE p.numero = ?
              AND NOT EXISTS (
                  SELECT 1
                  FROM $TABLA_PRUEBA pr
                  WHERE pr.id_protocolo = p.id_protocolo
                    AND pr.id_tipo_prueba = tp.id_tipo_prueba
                    AND pr.momento = 'UNICA'
              )
            """.trimIndent(),
            arrayOf(resultado, nombrePrueba, numeroProtocolo)
        )
    }

    // =========================================================
    // MÉTODOS ORIGINALES Y CONSULTAS REUTILIZABLES
    // =========================================================

    /**
     * Método original conservado para los ViewModels existentes.
     * El llamador sigue siendo responsable de cerrar el Cursor.
     */
    fun buscarUsuarioPorCorreo(correo: String): Cursor {
        return readableDatabase.rawQuery(
            "SELECT * FROM $TABLA_USUARIO WHERE correo = ?",
            arrayOf(correo)
        )
    }

    /**
     * Método original conservado por compatibilidad.
     * Convierte los bytes de la imagen seleccionada en un BLOB.
     */
    fun actualizarAvatarUsuario(
        idUsuario: Long,
        avatarUri: String
    ): Int {

        val imagen = try {
            contexto.contentResolver.openInputStream(
                Uri.parse(avatarUri)
            )?.use { it.readBytes() }
        } catch (_: Exception) {
            null
        } ?: return 0

        return actualizarAvatarBlob(idUsuario, imagen)
    }

    /**
     * Guarda el avatar como bytes en la columna avatar BLOB.
     * Una imagen null elimina el avatar guardado.
     */
    fun actualizarAvatarBlob(
        idUsuario: Long,
        imagen: ByteArray?
    ): Int {

        val valores = ContentValues().apply {
            put("avatar", imagen)
        }

        return writableDatabase.update(
            TABLA_USUARIO,
            valores,
            "id_usuario = ?",
            arrayOf(idUsuario.toString())
        )
    }

    /**
     * Método original conservado para los ViewModels existentes.
     */
    fun actualizarPassword(
        correo: String,
        nuevoHash: String
    ): Int {

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

    // NUEVO
    /**
     * Busca un usuario por su correo y devuelve sus datos como Usuario.
     */
    fun obtenerUsuarioPorCorreo(correo: String): Usuario? {
        val consulta = """
        SELECT u.*, r.codigo AS rol_codigo
        FROM $TABLA_USUARIO u
        INNER JOIN $TABLA_ROL r
            ON r.id_rol = u.id_rol
        WHERE u.correo = ?
    """.trimIndent()

        return readableDatabase.rawQuery(
            consulta,
            arrayOf(correo)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                leerUsuario(cursor)
            } else {
                null
            }
        }
    }

    /**
     * Busca un usuario por su identificador.
     */
    fun obtenerUsuarioPorId(idUsuario: Long): Usuario? {
        val consulta = """
        SELECT u.*, r.codigo AS rol_codigo
        FROM $TABLA_USUARIO u
        INNER JOIN $TABLA_ROL r
            ON r.id_rol = u.id_rol
        WHERE u.id_usuario = ?
    """.trimIndent()

        return readableDatabase.rawQuery(
            consulta,
            arrayOf(idUsuario.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                leerUsuario(cursor)
            } else {
                null
            }
        }
    }

    /**
     * Convierte una fila de usuario en el modelo Usuario.
     * Es privada porque solo la necesita este DBHelper.
     */
    private fun leerUsuario(cursor: Cursor): Usuario? {
        val codigoRol = cursor.getString(
            cursor.getColumnIndexOrThrow("rol_codigo")
        )

        val rol = when (codigoRol.uppercase()) {
            "TECNICO" -> RolUsuario.TECNICO_INGENIERO
            "CLIENTE" -> RolUsuario.CLIENTE
            else -> return null
        }

        val indiceAvatar = cursor.getColumnIndexOrThrow("avatar")
        val avatar = if (cursor.isNull(indiceAvatar)) {
            null
        } else {
            cursor.getBlob(indiceAvatar)
        }

        val indiceGiro = cursor.getColumnIndexOrThrow("giro")
        val indiceContactoNombre =
            cursor.getColumnIndexOrThrow("contacto_nombre")
        val indiceContactoCargo =
            cursor.getColumnIndexOrThrow("contacto_cargo")

        return Usuario(
            idUsuario = cursor.getLong(
                cursor.getColumnIndexOrThrow("id_usuario")
            ),
            rol = rol,
            rut = cursor.getString(
                cursor.getColumnIndexOrThrow("rut")
            ),
            nombre = cursor.getString(
                cursor.getColumnIndexOrThrow("nombre")
            ),
            correo = cursor.getString(
                cursor.getColumnIndexOrThrow("correo")
            ),
            telefono = cursor.getString(
                cursor.getColumnIndexOrThrow("telefono")
            ) ?: "",
            passwordHash = cursor.getString(
                cursor.getColumnIndexOrThrow("password_hash")
            ),
            activo = cursor.getInt(
                cursor.getColumnIndexOrThrow("activo")
            ) == 1,
            giro = if (cursor.isNull(indiceGiro)) {
                null
            } else {
                cursor.getString(indiceGiro)
            },
            contactoNombre = if (cursor.isNull(indiceContactoNombre)) {
                null
            } else {
                cursor.getString(indiceContactoNombre)
            },
            contactoCargo = if (cursor.isNull(indiceContactoCargo)) {
                null
            } else {
                cursor.getString(indiceContactoCargo)
            },
            avatar = avatar
        )
    }

    // NUEVO

    /**
     * Obtiene el ID interno de un rol usando su código.
     * Así no dependemos de que los roles tengan IDs específicos.
     */
    fun obtenerIdRolPorCodigo(codigo: String): Long? {
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT id_rol FROM $TABLA_ROL WHERE codigo = ?",
            arrayOf(codigo.trim().uppercase())
        )

        return cursor.use {
            if (it.moveToFirst()) it.getLong(0) else null
        }
    }

    /**
     * Comprueba si ya existe un usuario con el RUT indicado.
     */
    fun existeUsuarioPorRut(rut: String): Boolean {
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT 1 FROM $TABLA_USUARIO WHERE rut = ? LIMIT 1",
            arrayOf(rut)
        )

        return cursor.use { it.moveToFirst() }
    }

    /**
     * Comprueba si ya existe un usuario con el correo indicado.
     */
    fun existeUsuarioPorCorreo(correo: String): Boolean {
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT 1 FROM $TABLA_USUARIO WHERE correo = ? LIMIT 1",
            arrayOf(correo)
        )

        return cursor.use { it.moveToFirst() }
    }

    /**
     * Inserta un usuario en SQLite y devuelve su ID.
     * Devuelve -1 si SQLite no consigue completar la inserción.
     */
    fun insertarUsuario(valores: ContentValues): Long {
        val db = writableDatabase

        return db.insert(
            TABLA_USUARIO,
            null,
            valores
        )
    }


    // =========================================================
    // HU-04: CONSULTAS PARA EL REGISTRO DE PROTOCOLOS
    // =========================================================

    /**
     * Obtiene los clientes activos registrados en SQLite.
     * No devuelve usuarios con el rol de técnico.
     */
    fun obtenerClientesProtocolo(): List<ClienteProtocolo> {
        val consulta = """
            SELECT
                u.id_usuario,
                u.rut,
                u.nombre,
                u.correo
            FROM $TABLA_USUARIO u
            INNER JOIN $TABLA_ROL r
                ON r.id_rol = u.id_rol
            WHERE r.codigo = ?
              AND u.activo = 1
            ORDER BY u.nombre COLLATE NOCASE
        """.trimIndent()

        return readableDatabase.rawQuery(
            consulta,
            arrayOf("CLIENTE")
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        ClienteProtocolo(
                            idUsuario = cursor.getLong(
                                cursor.getColumnIndexOrThrow("id_usuario")
                            ),
                            rut = cursor.getString(
                                cursor.getColumnIndexOrThrow("rut")
                            ),
                            nombre = cursor.getString(
                                cursor.getColumnIndexOrThrow("nombre")
                            ),
                            correo = cursor.getString(
                                cursor.getColumnIndexOrThrow("correo")
                            )
                        )
                    )
                }
            }
        }
    }

    /**
     * Obtiene las instalaciones asociadas a un cliente.
     */
    fun obtenerInstalacionesPorCliente(
        idUsuarioCliente: Long
    ): List<Instalacion> {
        val consulta = """
            SELECT
                i.id_instalacion,
                i.id_usuario_cliente,
                i.nombre,
                i.ubicacion,
                i.id_comuna,
                c.nombre AS nombre_comuna,
                i.observaciones_acceso
            FROM $TABLA_INSTALACION i
            INNER JOIN $TABLA_COMUNA c
                ON c.id_comuna = i.id_comuna
            WHERE i.id_usuario_cliente = ?
            ORDER BY i.nombre COLLATE NOCASE
        """.trimIndent()

        return readableDatabase.rawQuery(
            consulta,
            arrayOf(idUsuarioCliente.toString())
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    val indiceUbicacion =
                        cursor.getColumnIndexOrThrow("ubicacion")

                    val indiceObservaciones =
                        cursor.getColumnIndexOrThrow(
                            "observaciones_acceso"
                        )

                    add(
                        Instalacion(
                            idInstalacion = cursor.getLong(
                                cursor.getColumnIndexOrThrow(
                                    "id_instalacion"
                                )
                            ),
                            idUsuarioCliente = cursor.getLong(
                                cursor.getColumnIndexOrThrow(
                                    "id_usuario_cliente"
                                )
                            ),
                            nombre = cursor.getString(
                                cursor.getColumnIndexOrThrow("nombre")
                            ),
                            ubicacion = if (
                                cursor.isNull(indiceUbicacion)
                            ) {
                                null
                            } else {
                                cursor.getString(indiceUbicacion)
                            },
                            idComuna = cursor.getLong(
                                cursor.getColumnIndexOrThrow("id_comuna")
                            ),
                            nombreComuna = cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                    "nombre_comuna"
                                )
                            ),
                            observacionesAcceso =
                                if (cursor.isNull(indiceObservaciones)) {
                                    null
                                } else {
                                    cursor.getString(indiceObservaciones)
                                }
                        )
                    )
                }
            }
        }
    }

    /**
     * Obtiene los equipos pertenecientes a una instalación.
     * Incluye el nombre y la clasificación del tipo de equipo.
     */
    fun obtenerEquiposPorInstalacion(
        idInstalacion: Long
    ): List<Equipo> {
        val consulta = """
            SELECT
                e.id_equipo,
                e.id_instalacion,
                e.tag,
                e.id_tipo_equipo,
                te.nombre AS nombre_tipo_equipo,
                te.clasificacion AS clasificacion_tipo,
                e.marca,
                e.modelo,
                e.numero_serie,
                e.potencia_kva,
                e.tensiones,
                e.corriente,
                e.max_contactos_uohm
            FROM $TABLA_EQUIPO e
            INNER JOIN $TABLA_TIPO_EQUIPO te
                ON te.id_tipo_equipo = e.id_tipo_equipo
            WHERE e.id_instalacion = ?
            ORDER BY e.tag COLLATE NOCASE
        """.trimIndent()

        return readableDatabase.rawQuery(
            consulta,
            arrayOf(idInstalacion.toString())
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    fun leerDoubleOpcional(columna: String): Double? {
                        val indice =
                            cursor.getColumnIndexOrThrow(columna)

                        return if (cursor.isNull(indice)) {
                            null
                        } else {
                            cursor.getDouble(indice)
                        }
                    }

                    fun leerTextoOpcional(columna: String): String? {
                        val indice =
                            cursor.getColumnIndexOrThrow(columna)

                        return if (cursor.isNull(indice)) {
                            null
                        } else {
                            cursor.getString(indice)
                        }
                    }

                    add(
                        Equipo(
                            idEquipo = cursor.getLong(
                                cursor.getColumnIndexOrThrow("id_equipo")
                            ),
                            idInstalacion = cursor.getLong(
                                cursor.getColumnIndexOrThrow(
                                    "id_instalacion"
                                )
                            ),
                            tag = cursor.getString(
                                cursor.getColumnIndexOrThrow("tag")
                            ),
                            idTipoEquipo = cursor.getLong(
                                cursor.getColumnIndexOrThrow(
                                    "id_tipo_equipo"
                                )
                            ),
                            nombreTipoEquipo = cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                    "nombre_tipo_equipo"
                                )
                            ),
                            clasificacionTipo = cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                    "clasificacion_tipo"
                                )
                            ),
                            marca = leerTextoOpcional("marca"),
                            modelo = leerTextoOpcional("modelo"),
                            numeroSerie = leerTextoOpcional("numero_serie"),
                            potenciaKva = leerDoubleOpcional("potencia_kva"),
                            tensiones = leerDoubleOpcional("tensiones"),
                            corriente = leerDoubleOpcional("corriente"),
                            maxContactosUohm =
                                leerDoubleOpcional("max_contactos_uohm")
                        )
                    )
                }
            }
        }
    }

    /**
     * Obtiene las comunas disponibles, indicando su región.
     */
    fun obtenerComunas(): List<Comuna> {
        val consulta = """
            SELECT
                c.id_comuna,
                c.id_region,
                c.nombre AS nombre_comuna,
                r.nombre AS nombre_region
            FROM $TABLA_COMUNA c
            INNER JOIN $TABLA_REGION r
                ON r.id_region = c.id_region
            ORDER BY r.nombre COLLATE NOCASE,
                     c.nombre COLLATE NOCASE
        """.trimIndent()

        return readableDatabase.rawQuery(
            consulta,
            null
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        Comuna(
                            idComuna = cursor.getLong(
                                cursor.getColumnIndexOrThrow("id_comuna")
                            ),
                            idRegion = cursor.getLong(
                                cursor.getColumnIndexOrThrow("id_region")
                            ),
                            nombre = cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                    "nombre_comuna"
                                )
                            ),
                            nombreRegion = cursor.getString(
                                cursor.getColumnIndexOrThrow("nombre_region")
                            )
                        )
                    )
                }
            }
        }
    }

    /**
     * Obtiene el catálogo de tipos de equipo.
     */
    fun obtenerTiposEquipo(): List<TipoEquipo> {
        val consulta = """
            SELECT id_tipo_equipo, nombre, clasificacion
            FROM $TABLA_TIPO_EQUIPO
            ORDER BY nombre COLLATE NOCASE
        """.trimIndent()

        return readableDatabase.rawQuery(
            consulta,
            null
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        TipoEquipo(
                            idTipoEquipo = cursor.getLong(
                                cursor.getColumnIndexOrThrow(
                                    "id_tipo_equipo"
                                )
                            ),
                            nombre = cursor.getString(
                                cursor.getColumnIndexOrThrow("nombre")
                            ),
                            clasificacion = cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                    "clasificacion"
                                )
                            )
                        )
                    )
                }
            }
        }
    }

    /**
     * Obtiene únicamente las pruebas compatibles con el tipo de equipo.
     */
    fun obtenerPruebasCompatibles(
        idTipoEquipo: Long
    ): List<TipoPrueba> {
        val consulta = """
            SELECT
                tp.id_tipo_prueba,
                tp.nombre
            FROM $TABLA_TIPO_PRUEBA_EQUIPO c
            INNER JOIN $TABLA_TIPO_PRUEBA tp
                ON tp.id_tipo_prueba = c.id_tipo_prueba
            WHERE c.id_tipo_equipo = ?
            ORDER BY tp.nombre COLLATE NOCASE
        """.trimIndent()

        return readableDatabase.rawQuery(
            consulta,
            arrayOf(idTipoEquipo.toString())
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        TipoPrueba(
                            idTipoPrueba = cursor.getLong(
                                cursor.getColumnIndexOrThrow(
                                    "id_tipo_prueba"
                                )
                            ),
                            nombre = cursor.getString(
                                cursor.getColumnIndexOrThrow("nombre")
                            )
                        )
                    )
                }
            }
        }
    }


    // =========================================================
    // HU-04: CREACIÓN DE INSTALACIONES Y EQUIPOS
    // =========================================================

    /**
     * Comprueba si un TAG ya está registrado en una instalación.
     * El mismo TAG puede existir en instalaciones diferentes.
     */
    fun existeTagEnInstalacion(
        idInstalacion: Long,
        tag: String
    ): Boolean {
        return readableDatabase.rawQuery(
            """
                SELECT 1
                FROM $TABLA_EQUIPO
                WHERE id_instalacion = ?
                  AND UPPER(TRIM(tag)) = UPPER(TRIM(?))
                LIMIT 1
            """.trimIndent(),
            arrayOf(idInstalacion.toString(), tag.trim())
        ).use { cursor ->
            cursor.moveToFirst()
        }
    }

    /**
     * Crea una instalación para el cliente seleccionado.
     * Devuelve su ID si se inserta correctamente o -1 si falla.
     */
    fun insertarInstalacionProtocolo(
        idUsuarioCliente: Long,
        nombre: String,
        ubicacion: String?,
        idComuna: Long,
        observacionesAcceso: String?
    ): Long {
        val valores = ContentValues().apply {
            put("id_usuario_cliente", idUsuarioCliente)
            put("nombre", nombre.trim())

            if (ubicacion.isNullOrBlank()) {
                putNull("ubicacion")
            } else {
                put("ubicacion", ubicacion.trim())
            }

            put("id_comuna", idComuna)

            if (observacionesAcceso.isNullOrBlank()) {
                putNull("observaciones_acceso")
            } else {
                put(
                    "observaciones_acceso",
                    observacionesAcceso.trim()
                )
            }
        }

        return try {
            writableDatabase.insert(
                TABLA_INSTALACION,
                null,
                valores
            )
        } catch (_: SQLiteConstraintException) {
            -1L
        }
    }

    /**
     * Crea un equipo en la instalación seleccionada.
     * El TAG debe ser único dentro de esa instalación.
     *
     * Los datos de placa pueden quedar nulos cuando no se
     * hayan proporcionado.
     */
    fun insertarEquipoProtocolo(
        idInstalacion: Long,
        tag: String,
        idTipoEquipo: Long,
        marca: String?,
        modelo: String?,
        numeroSerie: String?,
        potenciaKva: Double?,
        tensiones: Double?,
        corriente: Double?,
        maxContactosUohm: Double?
    ): Long {

        val tagLimpio = tag.trim()

        // Comprobación previa para poder mostrar un mensaje claro.
        if (existeTagEnInstalacion(idInstalacion, tagLimpio)) {
            return -1L
        }

        val valores = ContentValues().apply {
            put("id_instalacion", idInstalacion)
            put("tag", tagLimpio)
            put("id_tipo_equipo", idTipoEquipo)

            if (marca.isNullOrBlank()) {
                putNull("marca")
            } else {
                put("marca", marca.trim())
            }

            if (modelo.isNullOrBlank()) {
                putNull("modelo")
            } else {
                put("modelo", modelo.trim())
            }

            if (numeroSerie.isNullOrBlank()) {
                putNull("numero_serie")
            } else {
                put("numero_serie", numeroSerie.trim())
            }

            if (potenciaKva == null) {
                putNull("potencia_kva")
            } else {
                put("potencia_kva", potenciaKva)
            }

            if (tensiones == null) {
                putNull("tensiones")
            } else {
                put("tensiones", tensiones)
            }

            if (corriente == null) {
                putNull("corriente")
            } else {
                put("corriente", corriente)
            }

            if (maxContactosUohm == null) {
                putNull("max_contactos_uohm")
            } else {
                put("max_contactos_uohm", maxContactosUohm)
            }
        }

        return try {
            writableDatabase.insert(
                TABLA_EQUIPO,
                null,
                valores
            )
        } catch (_: SQLiteConstraintException) {
            // La restricción UNIQUE también protege ante
            // un TAG duplicado entre la comprobación y el INSERT.
            -1L
        }
    }



}

