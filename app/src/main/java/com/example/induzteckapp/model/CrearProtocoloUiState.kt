package com.example.induzteckapp.model

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