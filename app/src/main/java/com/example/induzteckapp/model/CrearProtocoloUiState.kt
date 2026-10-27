package com.example.induzteckapp.model

data class CrearProtocoloUiState(
    
    val pasoActual: Int = 1,

    
    val clientes: List<ClienteProtocolo> = emptyList(),
    val instalaciones: List<Instalacion> = emptyList(),
    val equipos: List<Equipo> = emptyList(),
    val regiones: List<Region> = emptyList(),
    val comunas: List<Comuna> = emptyList(),
    val tiposEquipo: List<TipoEquipo> = emptyList(),
    val pruebasCompatibles: List<TipoPrueba> = emptyList(),
    val parametrosPrueba: List<ParametroPrueba> = emptyList(),

    
    val clienteSeleccionado: ClienteProtocolo? = null,
    val instalacionSeleccionada: Instalacion? = null,
    val equipoSeleccionado: Equipo? = null,
    val tipoEquipoSeleccionado: TipoEquipo? = null,
    val pruebaSeleccionada: TipoPrueba? = null,
    val pruebasSeleccionadas: List<TipoPrueba> = emptyList(),
    val indicePruebaActual: Int = 0,
    val pruebasCompletadas: Set<Long> = emptySet(),
    val elementosCompletados: Set<String> = emptySet(),
    val idProtocoloBorrador: Long? = null,
    val modoEdicion: Boolean = false,
    val protocoloFinalizado: Boolean = false,
    val resumenResultados: List<String> = emptyList(),
    val elementoPrueba: String = "",
    val releOpero: Boolean = true,
    val resultadoManual: Int = 1,
    val borradores: List<ProtocoloBorrador> = emptyList(),
    val regionSeleccionada: Region? = null,
    val comunaSeleccionada: Comuna? = null,

    
    val mostrarFormularioInstalacion: Boolean = false,
    val nombreInstalacion: String = "",
    val ubicacionInstalacion: String = "",
    val observacionesAcceso: String = "",

    
    val mostrarFormularioEquipo: Boolean = false,
    val tagEquipo: String = "",
    val marcaEquipo: String = "",
    val modeloEquipo: String = "",
    val numeroSerieEquipo: String = "",
    val potenciaKva: String = "",
    val tensiones: String = "",
    val corriente: String = "",
    val maxContactosUohm: String = "",

    
    val temperaturaC: String = "",
    val humedadPct: String = "",

    
    val mediciones: Map<String, String> = emptyMap(),
    val unidadAislacion: String = "MΩ",

    
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val mensajeError: String? = null,
    val mensajeExito: String? = null,
    
    val erroresCampos: Map<String, String> = emptyMap()
)


