
package com.example.induzteckapp.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri

import com.example.induzteckapp.model.RolUsuario
import com.example.induzteckapp.model.Usuario
import com.example.induzteckapp.model.ClienteConsulta
import com.example.induzteckapp.model.EquipoConsulta
import com.example.induzteckapp.model.InstalacionConsulta
import com.example.induzteckapp.model.ProtocoloDetalle
import com.example.induzteckapp.model.ProtocoloResumen
import com.example.induzteckapp.model.PruebaDetalle

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
    // nuevo (25/10/2026)
// 1. Obtener clientes con instalaciones y equipos usando tus constantes (CA-2.1)
    fun obtenerClientesConActivos(filtro: String = ""): List<ClienteConsulta> {
        val clientesMap = mutableMapOf<Int, ClienteConsulta>()
        val db = readableDatabase

        val query = """
        SELECT DISTINCT 
            u.id_usuario, u.nombre AS cliente_nombre, u.rut,
            i.id_instalacion, i.nombre AS instalacion_nombre,
            e.id_equipo, e.tag, te.nombre AS tipo_equipo
        FROM $TABLA_USUARIO u
        JOIN $TABLA_INSTALACION i ON u.id_usuario = i.id_usuario_cliente
        JOIN $TABLA_EQUIPO e ON i.id_instalacion = e.id_instalacion
        JOIN $TABLA_TIPO_EQUIPO te ON e.id_tipo_equipo = te.id_tipo_equipo
        WHERE u.id_rol = (SELECT id_rol FROM $TABLA_ROL WHERE codigo = 'CLIENTE')
          AND (? = '' OR u.nombre LIKE '%' || ? || '%' OR e.tag LIKE '%' || ? || '%' OR i.nombre LIKE '%' || ? || '%')
        ORDER BY u.nombre ASC, i.nombre ASC, e.tag ASC
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(filtro, filtro, filtro, filtro))

        if (cursor.moveToFirst()) {
            do {
                val idUsuario = cursor.getInt(cursor.getColumnIndexOrThrow("id_usuario"))
                val clienteNombre = cursor.getString(cursor.getColumnIndexOrThrow("cliente_nombre"))
                val rut = cursor.getString(cursor.getColumnIndexOrThrow("rut"))
                val idInstalacion = cursor.getInt(cursor.getColumnIndexOrThrow("id_instalacion"))
                val instalacionNombre = cursor.getString(cursor.getColumnIndexOrThrow("instalacion_nombre"))
                val idEquipo = cursor.getInt(cursor.getColumnIndexOrThrow("id_equipo"))
                val tag = cursor.getString(cursor.getColumnIndexOrThrow("tag"))
                val tipoEquipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo_equipo"))

                val cliente = clientesMap.getOrPut(idUsuario) {
                    ClienteConsulta(
                        idUsuario = idUsuario,
                        nombreRazonSocial = clienteNombre,
                        rut = rut,
                        instalaciones = mutableListOf()
                    )
                }

                val instalacionesList = cliente.instalaciones as MutableList<InstalacionConsulta>
                var instalacion = instalacionesList.find { it.idInstalacion == idInstalacion }
                if (instalacion == null) {
                    instalacion = InstalacionConsulta(
                        idInstalacion = idInstalacion,
                        nombre = instalacionNombre,
                        equipos = mutableListOf()
                    )
                    instalacionesList.add(instalacion)
                }

                val equiposList = instalacion.equipos as MutableList<EquipoConsulta>
                if (equiposList.none { it.idEquipo == idEquipo }) {
                    equiposList.add(
                        EquipoConsulta(
                            idEquipo = idEquipo,
                            tag = tag,
                            tipoEquipo = tipoEquipo,
                            instalacionNombre = instalacionNombre // Etiqueta contextual (CA-2.2)
                        )
                    )
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        return clientesMap.values.toList()
    }

    // 2. Obtener historial de protocolos por equipo usando tus constantes de resultados (CA-2.3)
    fun obtenerHistorialEquipo(idEquipo: Int): List<ProtocoloResumen> {
        val historial = mutableListOf<ProtocoloResumen>()
        val db = readableDatabase

        val query = """
        SELECT 
            p.id_protocolo, p.numero, p.fecha_hora_prueba,
            ep.nombre AS estado,
            u.nombre AS tecnico_nombre,
            COALESCE((
                SELECT CASE
                    WHEN SUM(CASE WHEN pr.id_resultado = $RESULTADO_RECHAZADO THEN 1 ELSE 0 END) > 0 THEN 'Rechazado'
                    WHEN SUM(CASE WHEN pr.id_resultado = $RESULTADO_OBSERVADO THEN 1 ELSE 0 END) > 0 THEN 'Observado'
                    ELSE 'Aprobado'
                END
                FROM $TABLA_PRUEBA pr WHERE pr.id_protocolo = p.id_protocolo
            ), 'Aprobado') AS resultado_global
        FROM $TABLA_PROTOCOLO p
        JOIN $TABLA_ESTADO_PROTOCOLO ep ON p.id_estado = ep.id_estado
        JOIN $TABLA_USUARIO u ON p.id_usuario_registra = u.id_usuario
        WHERE p.id_equipo = ?
        ORDER BY p.fecha_hora_prueba DESC
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(idEquipo.toString()))
        if (cursor.moveToFirst()) {
            do {
                historial.add(
                    ProtocoloResumen(
                        idProtocolo = cursor.getInt(cursor.getColumnIndexOrThrow("id_protocolo")),
                        numeroProtocolo = cursor.getString(cursor.getColumnIndexOrThrow("numero")),
                        fechaHora = cursor.getString(cursor.getColumnIndexOrThrow("fecha_hora_prueba")),
                        estado = cursor.getString(cursor.getColumnIndexOrThrow("estado")),
                        resultadoGlobal = cursor.getString(cursor.getColumnIndexOrThrow("resultado_global")),
                        tecnicoResponsable = cursor.getString(cursor.getColumnIndexOrThrow("tecnico_nombre"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return historial
    }

    // 3. Obtener instalaciones de un cliente específico con sus equipos (Vista Cliente HU-03)
    fun obtenerInstalacionesCliente(idUsuarioCliente: Long, filtro: String = ""): List<InstalacionConsulta> {
        val instalacionesMap = mutableMapOf<Int, InstalacionConsulta>()
        val db = readableDatabase

        val query = """
            SELECT
                i.id_instalacion, i.nombre AS instalacion_nombre,
                e.id_equipo, e.tag, te.nombre AS tipo_equipo
            FROM $TABLA_INSTALACION i
            LEFT JOIN $TABLA_EQUIPO e ON i.id_instalacion = e.id_instalacion
            LEFT JOIN $TABLA_TIPO_EQUIPO te ON e.id_tipo_equipo = te.id_tipo_equipo
            WHERE i.id_usuario_cliente = ?
              AND (? = '' OR i.nombre LIKE '%' || ? || '%' OR e.tag LIKE '%' || ? || '%')
            ORDER BY i.nombre ASC, e.tag ASC
        """.trimIndent()

        val cursor = db.rawQuery(
            query,
            arrayOf(
                idUsuarioCliente.toString(),
                filtro,
                filtro,
                filtro
            )
        )

        if (cursor.moveToFirst()) {
            do {
                val idInstalacion = cursor.getInt(cursor.getColumnIndexOrThrow("id_instalacion"))
                val instalacionNombre = cursor.getString(cursor.getColumnIndexOrThrow("instalacion_nombre"))

                val instalacion = instalacionesMap.getOrPut(idInstalacion) {
                    InstalacionConsulta(
                        idInstalacion = idInstalacion,
                        nombre = instalacionNombre,
                        equipos = mutableListOf()
                    )
                }

                if (!cursor.isNull(cursor.getColumnIndexOrThrow("id_equipo"))) {
                    val idEquipo = cursor.getInt(cursor.getColumnIndexOrThrow("id_equipo"))
                    val tag = cursor.getString(cursor.getColumnIndexOrThrow("tag"))
                    val tipoEquipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo_equipo"))

                    val equiposList = instalacion.equipos as MutableList<EquipoConsulta>
                    if (equiposList.none { it.idEquipo == idEquipo }) {
                        equiposList.add(
                            EquipoConsulta(
                                idEquipo = idEquipo,
                                tag = tag,
                                tipoEquipo = tipoEquipo,
                                instalacionNombre = instalacionNombre
                            )
                        )
                    }
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        return instalacionesMap.values.toList()
    }

    // 4. Obtener historial de protocolos exclusivo para el Cliente (HU-03, RN-17 / CA-3.2)
    // Excluye BORRADOR y ANULADO. Mapea la etiqueta 'Pendiente de formalización' para 'Registrado en terreno' u 'Observado'.
    fun obtenerHistorialEquipoCliente(idEquipo: Int): List<ProtocoloResumen> {
        val historial = mutableListOf<ProtocoloResumen>()
        val db = readableDatabase

        val query = """
            SELECT
                p.id_protocolo, p.numero, p.fecha_hora_prueba,
                ep.codigo AS estado_codigo, ep.nombre AS estado_nombre,
                u.nombre AS tecnico_nombre,
                COALESCE((
                    SELECT CASE
                        WHEN SUM(CASE WHEN pr.id_resultado = $RESULTADO_RECHAZADO THEN 1 ELSE 0 END) > 0 THEN 'Rechazado'
                        WHEN SUM(CASE WHEN pr.id_resultado = $RESULTADO_OBSERVADO THEN 1 ELSE 0 END) > 0 THEN 'Observado'
                        ELSE 'Aprobado'
                    END
                    FROM $TABLA_PRUEBA pr WHERE pr.id_protocolo = p.id_protocolo
                ), 'Aprobado') AS resultado_global
            FROM $TABLA_PROTOCOLO p
            JOIN $TABLA_ESTADO_PROTOCOLO ep ON p.id_estado = ep.id_estado
            JOIN $TABLA_USUARIO u ON p.id_usuario_registra = u.id_usuario
            WHERE p.id_equipo = ?
              AND ep.codigo NOT IN ('BORRADOR', 'ANULADO')
            ORDER BY p.fecha_hora_prueba DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(idEquipo.toString()))
        if (cursor.moveToFirst()) {
            do {
                val estadoCodigo = cursor.getString(cursor.getColumnIndexOrThrow("estado_codigo"))
                val estadoEtiqueta = if (estadoCodigo == "FORMALIZADO") {
                    "Formalizado"
                } else {
                    "Pendiente de formalización"
                }

                historial.add(
                    ProtocoloResumen(
                        idProtocolo = cursor.getInt(cursor.getColumnIndexOrThrow("id_protocolo")),
                        numeroProtocolo = cursor.getString(cursor.getColumnIndexOrThrow("numero")),
                        fechaHora = cursor.getString(cursor.getColumnIndexOrThrow("fecha_hora_prueba")),
                        estado = estadoEtiqueta,
                        resultadoGlobal = cursor.getString(cursor.getColumnIndexOrThrow("resultado_global")),
                        tecnicoResponsable = cursor.getString(cursor.getColumnIndexOrThrow("tecnico_nombre"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return historial
    }

    // 5. Obtener detalle consolidado para la vista "Detalle del Respaldo Digital" (HU-03)
    fun obtenerDetalleProtocolo(idProtocolo: Int): ProtocoloDetalle? {
        val db = readableDatabase

        val queryCabecera = """
            SELECT
                p.id_protocolo, p.numero, p.fecha_hora_prueba, p.observaciones,
                p.temperatura_c, p.humedad_pct,
                ep.codigo AS estado_codigo, ep.nombre AS estado_nombre,
                u.nombre AS tecnico_nombre,
                cli.nombre AS cliente_nombre,
                i.nombre AS instalacion_nombre,
                e.tag AS equipo_tag, te.nombre AS tipo_equipo,
                COALESCE((
                    SELECT CASE
                        WHEN SUM(CASE WHEN pr.id_resultado = $RESULTADO_RECHAZADO THEN 1 ELSE 0 END) > 0 THEN 'Rechazado'
                        WHEN SUM(CASE WHEN pr.id_resultado = $RESULTADO_OBSERVADO THEN 1 ELSE 0 END) > 0 THEN 'Observado'
                        ELSE 'Aprobado'
                    END
                    FROM $TABLA_PRUEBA pr WHERE pr.id_protocolo = p.id_protocolo
                ), 'Aprobado') AS resultado_global
            FROM $TABLA_PROTOCOLO p
            JOIN $TABLA_ESTADO_PROTOCOLO ep ON p.id_estado = ep.id_estado
            JOIN $TABLA_USUARIO u ON p.id_usuario_registra = u.id_usuario
            JOIN $TABLA_EQUIPO e ON p.id_equipo = e.id_equipo
            JOIN $TABLA_TIPO_EQUIPO te ON e.id_tipo_equipo = te.id_tipo_equipo
            JOIN $TABLA_INSTALACION i ON e.id_instalacion = i.id_instalacion
            JOIN $TABLA_USUARIO cli ON i.id_usuario_cliente = cli.id_usuario
            WHERE p.id_protocolo = ?
        """.trimIndent()

        val cursor = db.rawQuery(queryCabecera, arrayOf(idProtocolo.toString()))
        if (!cursor.moveToFirst()) {
            cursor.close()
            return null
        }

        val numero = cursor.getString(cursor.getColumnIndexOrThrow("numero"))
        val fechaHora = cursor.getString(cursor.getColumnIndexOrThrow("fecha_hora_prueba"))
        val observaciones = cursor.getString(cursor.getColumnIndexOrThrow("observaciones")) ?: ""
        val temp = cursor.getString(cursor.getColumnIndexOrThrow("temperatura_c")) ?: "20"
        val hum = cursor.getString(cursor.getColumnIndexOrThrow("humedad_pct")) ?: "45"
        val estadoCodigo = cursor.getString(cursor.getColumnIndexOrThrow("estado_codigo"))
        val tecnicoNombre = cursor.getString(cursor.getColumnIndexOrThrow("tecnico_nombre"))
        val clienteNombre = cursor.getString(cursor.getColumnIndexOrThrow("cliente_nombre"))
        val instalacionNombre = cursor.getString(cursor.getColumnIndexOrThrow("instalacion_nombre"))
        val equipoTag = cursor.getString(cursor.getColumnIndexOrThrow("equipo_tag"))
        val tipoEquipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo_equipo"))
        val resultadoGlobal = cursor.getString(cursor.getColumnIndexOrThrow("resultado_global"))
        cursor.close()

        val estadoEtiqueta = if (estadoCodigo == "FORMALIZADO") {
            "Formalizado"
        } else {
            "Pendiente de formalización"
        }

        val pruebasList = mutableListOf<PruebaDetalle>()
        val queryPruebas = """
            SELECT
                pr.id_prueba, tp.nombre AS tipo_prueba, pr.elemento, pr.momento,
                COALESCE((
                    SELECT GROUP_CONCAT(m.valor, ', ')
                    FROM $TABLA_MEDICION m
                    WHERE m.id_prueba = pr.id_prueba
                ), '-') AS valor_medido,
                CASE pr.id_resultado
                    WHEN $RESULTADO_APROBADO THEN 'Aprobado'
                    WHEN $RESULTADO_OBSERVADO THEN 'Observado'
                    ELSE 'Rechazado'
                END AS resultado
            FROM $TABLA_PRUEBA pr
            JOIN $TABLA_TIPO_PRUEBA tp ON pr.id_tipo_prueba = tp.id_tipo_prueba
            WHERE pr.id_protocolo = ?
            ORDER BY pr.id_prueba ASC
        """.trimIndent()

        val cursorPruebas = db.rawQuery(queryPruebas, arrayOf(idProtocolo.toString()))
        if (cursorPruebas.moveToFirst()) {
            do {
                val valMedido = cursorPruebas.getString(cursorPruebas.getColumnIndexOrThrow("valor_medido")) ?: "-"
                pruebasList.add(
                    PruebaDetalle(
                        idPrueba = cursorPruebas.getInt(cursorPruebas.getColumnIndexOrThrow("id_prueba")),
                        tipoPruebaNombre = cursorPruebas.getString(cursorPruebas.getColumnIndexOrThrow("tipo_prueba")),
                        elemento = cursorPruebas.getString(cursorPruebas.getColumnIndexOrThrow("elemento")),
                        momento = cursorPruebas.getString(cursorPruebas.getColumnIndexOrThrow("momento")),
                        valorMedido = valMedido,
                        unidadMedida = "MΩ",
                        resultado = cursorPruebas.getString(cursorPruebas.getColumnIndexOrThrow("resultado"))
                    )
                )
            } while (cursorPruebas.moveToNext())
        }
        cursorPruebas.close()

        return ProtocoloDetalle(
            idProtocolo = idProtocolo,
            numeroProtocolo = numero,
            fechaHora = fechaHora,
            estadoCodigo = estadoCodigo,
            estadoEtiqueta = estadoEtiqueta,
            resultadoGlobal = resultadoGlobal,
            tecnicoResponsable = tecnicoNombre,
            clienteNombre = clienteNombre,
            instalacionNombre = instalacionNombre,
            equipoTag = equipoTag,
            tipoEquipo = tipoEquipo,
            temperatura = "$temp °C",
            humedad = "$hum %",
            observaciones = observaciones,
            pruebas = pruebasList
        )
    }

}


/*
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
        db.execSQL("INSERT INTO $TABLA_USUARIO (id_rol, rut, nombre, correo, telefono, password_hash, activo) VALUES (1, '17.284.519-3', 'Matías Godoy Herrera', 'tecnico@induztek.cl', '56900000101', '$hashPrueba', 1)")

        // 6. Datos Semilla: Cliente e Instalación (Anexo 9)
        db.execSQL("INSERT INTO $TABLA_USUARIO (id_rol, rut, nombre, correo, telefono, password_hash, activo, giro, contacto_nombre, contacto_cargo) VALUES (2, '76.482.915-8', 'Envasadora del Pacífico S.A.', 'cliente@ejemplo.cl', '56900000101', '$hashPrueba', 1, 'Agroindustria', 'Marcela Fuentes Rojas', 'Jefe de Planta')")
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

*/