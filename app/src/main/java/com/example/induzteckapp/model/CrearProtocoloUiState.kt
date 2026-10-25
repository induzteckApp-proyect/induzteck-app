package com.example.induzteckapp.model

data class CrearProtocoloUiState(
    // Navegación
    val pasoActual: Int = 1,

    // Catálogos de SQLite
    val clientes: List<ClienteProtocolo> = emptyList(),
    val instalaciones: List<Instalacion> = emptyList(),
    val equipos: List<Equipo> = emptyList(),
    val regiones: List<Region> = emptyList(),
    val comunas: List<Comuna> = emptyList(),
    val tiposEquipo: List<TipoEquipo> = emptyList(),
    val pruebasCompatibles: List<TipoPrueba> = emptyList(),
    val parametrosPrueba: List<ParametroPrueba> = emptyList(),

    // Selecciones
    val clienteSeleccionado: ClienteProtocolo? = null,
    val instalacionSeleccionada: Instalacion? = null,
    val equipoSeleccionado: Equipo? = null,
    val tipoEquipoSeleccionado: TipoEquipo? = null,
    val pruebaSeleccionada: TipoPrueba? = null,
    val regionSeleccionada: Region? = null,
    val comunaSeleccionada: Comuna? = null,

    // Formulario de nueva instalación
    val mostrarFormularioInstalacion: Boolean = false,
    val nombreInstalacion: String = "",
    val ubicacionInstalacion: String = "",
    val observacionesAcceso: String = "",

    // Formulario de nuevo equipo
    val mostrarFormularioEquipo: Boolean = false,
    val tagEquipo: String = "",
    val marcaEquipo: String = "",
    val modeloEquipo: String = "",
    val numeroSerieEquipo: String = "",
    val potenciaKva: String = "",
    val tensiones: String = "",
    val corriente: String = "",
    val maxContactosUohm: String = "",

    // Condiciones ambientales
    val temperaturaC: String = "",
    val humedadPct: String = "",

    // Mediciones, identificadas por el código del parámetro
    val mediciones: Map<String, String> = emptyMap(),
    val unidadAislacion: String = "MΩ",

    // Estado de la interfaz
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val mensajeError: String? = null,
    val mensajeExito: String? = null
)



/*
data class CrearProtocoloUiState(
    // Navegación interna del asistente
    val pasoActual: Int = 1,

    // Datos que se cargan desde SQLite
    val clientes: List<ClienteProtocolo> = emptyList(),
    val instalaciones: List<Instalacion> = emptyList(),
    val equipos: List<Equipo> = emptyList(),
    val comunas: List<Comuna> = emptyList(),
    val tiposEquipo: List<TipoEquipo> = emptyList(),
    val pruebasCompatibles: List<TipoPrueba> = emptyList(),

    // Selecciones del paso 1
    val clienteSeleccionado: ClienteProtocolo? = null,
    val instalacionSeleccionada: Instalacion? = null,
    val equipoSeleccionado: Equipo? = null,
    val pruebaSeleccionada: TipoPrueba? = null,

    // Condiciones ambientales del paso 2
    val temperaturaC: String = "",
    val humedadPct: String = "",

    // Campos del paso 3.
    // Se guardan como texto para validar el formato antes de convertirlos.
    val mediciones: Map<String, String> = emptyMap(),

    // Estado de la interfaz
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val mensajeError: String? = null,
    val mensajeExito: String? = null
)

*/