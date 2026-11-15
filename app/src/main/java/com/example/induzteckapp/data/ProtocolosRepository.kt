package com.example.induzteckapp.data

import android.content.Context
import com.example.induzteckapp.data.remote.*
import com.example.induzteckapp.model.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class ProtocolosRepository(context: Context) {
    private val appContext = context.applicationContext
    private val api = RetrofitClient.protocolosApi(appContext)
    private val activos = ActivosRepository(appContext)
    private val preferencias = InduztekPreferencias(appContext)

    suspend fun obtenerPruebasCompatibles(idTipoEquipo: Long): List<TipoPrueba> = ejecutarApi {
        api.tiposPrueba(idTipoEquipo)
    }.map { TipoPrueba(it.id, it.nombre) }

    suspend fun obtenerParametrosPorTipoPrueba(idTipoPrueba: Long): List<ParametroPrueba> = ejecutarApi {
        api.parametros(idTipoPrueba)
    }.map { ParametroPrueba(it.id, it.idTipoPrueba, it.codigo, it.nombre, it.unidad) }

    suspend fun guardarCabeceraBorrador(
        idProtocoloExistente: Long?, idEquipo: Long, idUsuarioRegistra: Long,
        fechaHora: String, temperaturaC: Double, humedadPct: Double
    ): Long {
        validarUsuarioSesion(idUsuarioRegistra)
        val request = BorradorRequestDto(
            idEquipo = idEquipo,
            fechaHoraPrueba = fechaIso(fechaHora),
            temperaturaC = temperaturaC,
            humedadPct = humedadPct,
            idsTiposPrueba = emptyList()
        )
        val respuesta = if (idProtocoloExistente != null && idProtocoloExistente > 0L) {
            ejecutarApi { api.actualizarCabecera(idProtocoloExistente, request) }
        } else {
            ejecutarApi { api.crearBorrador(request) }
        }
        return respuesta.id
    }

    suspend fun guardarPruebasSeleccionadasBorrador(idProtocolo: Long, idsTiposPrueba: List<Long>) {
        ejecutarApi {
            api.guardarPruebasSeleccionadas(idProtocolo, GuardarPruebasSeleccionadasRequestDto(idsTiposPrueba.distinct()))
        }
    }

    suspend fun guardarPruebaEvaluada(
        idProtocolo: Long, idTipoPrueba: Long, elemento: String?,
        mediciones: Map<String, Double>, idResultado: Int
    ): Long {
        val parametros = obtenerParametrosPorTipoPrueba(idTipoPrueba).associateBy { it.codigo }
        val medicionesRequest = mediciones.map { (codigo, valor) ->
            val parametro = parametros[codigo]
                ?: throw IllegalArgumentException("No existe el parámetro $codigo para esta prueba.")
            MedicionRequestDto(parametro.idParametro, valor)
        }
        val response = ejecutarApi {
            api.guardarPrueba(idProtocolo, GuardarPruebaRequestDto(
                idPrueba = null,
                idTipoPrueba = idTipoPrueba,
                elemento = elemento?.trim()?.ifBlank { null },
                momento = "UNICA",
                realizada = true,
                idResultado = idResultado,
                mediciones = medicionesRequest
            ))
        }
        return response.pruebas.lastOrNull { it.idTipoPrueba == idTipoPrueba && it.elemento == elemento }?.id
            ?: response.pruebas.lastOrNull { it.idTipoPrueba == idTipoPrueba }?.id
            ?: -1L
    }

    suspend fun finalizarBorradorProtocolo(idProtocolo: Long) {
        ejecutarApi { api.cambiarEstado(idProtocolo, CambioEstadoRequestDto("REGISTRADO_TERRENO")) }
    }

    suspend fun cambiarEstado(idProtocolo: Long, nuevoEstado: String, motivo: String = "") {
        ejecutarApi {
            api.cambiarEstado(idProtocolo, CambioEstadoRequestDto(nuevoEstado, motivo.trim().ifBlank { null }))
        }
    }

    suspend fun prepararProtocoloParaEdicion(idProtocolo: Long) {
        ejecutarApi { api.prepararEdicion(idProtocolo) }
    }

    suspend fun obtenerDetalleBorrador(idProtocolo: Long, idUsuarioRegistra: Long): BorradorProtocoloDetalle {
        val protocolo = ejecutarApi { api.detalle(idProtocolo) }
        if (protocolo.idUsuarioRegistra != idUsuarioRegistra || protocolo.estadoCodigo != "BORRADOR") {
            throw IllegalArgumentException("No se encontró el borrador del técnico autenticado.")
        }
        val equipo = activos.obtenerEquipo(protocolo.idEquipo)
        val instalacion = activos.obtenerInstalacion(equipo.idInstalacion)
        return BorradorProtocoloDetalle(
            idProtocolo = protocolo.id,
            idUsuarioCliente = instalacion.idUsuarioCliente,
            idInstalacion = instalacion.idInstalacion,
            idEquipo = equipo.idEquipo,
            temperaturaC = protocolo.temperaturaC,
            humedadPct = protocolo.humedadPct
        )
    }

    suspend fun obtenerTiposPruebaCompletados(idProtocolo: Long): Set<Long> =
        ejecutarApi { api.detalle(idProtocolo) }.pruebas.filter { it.realizada }.map { it.idTipoPrueba }.toSet()

    suspend fun obtenerElementosPruebaCompletados(idProtocolo: Long, idTipoPrueba: Long): Set<String> =
        ejecutarApi { api.detalle(idProtocolo) }.pruebas
            .filter { it.realizada && it.idTipoPrueba == idTipoPrueba }
            .mapNotNull { it.elemento?.trim()?.lowercase(Locale.ROOT) }.toSet()

    suspend fun obtenerTiposPruebaSeleccionadosBorrador(idProtocolo: Long): List<Long> =
        ejecutarApi { api.detalle(idProtocolo) }.pruebas.map { it.idTipoPrueba }.distinct()

    suspend fun obtenerResultadoGlobalProtocolo(idProtocolo: Long): Int? = when (
        ejecutarApi { api.detalle(idProtocolo) }.resultadoGlobal
    ) {
        "Aprobado" -> 1
        "Observado" -> 2
        "Rechazado" -> 3
        else -> null
    }

    suspend fun obtenerResumenPruebasProtocolo(idProtocolo: Long): List<String> =
        ejecutarApi { api.detalle(idProtocolo) }.pruebas.map { prueba ->
            val elemento = prueba.elemento?.takeIf { it.isNotBlank() }?.let { " ($it)" }.orEmpty()
            "${prueba.tipoPrueba}$elemento: ${if (prueba.realizada) prueba.resultado else "Pendiente"}"
        }

    suspend fun obtenerBorradoresProtocolo(idUsuario: Long): List<ProtocoloBorrador> {
        validarUsuarioSesion(idUsuario)
        return ejecutarApi { api.borradores() }.map { protocolo ->
            ProtocoloBorrador(
                idProtocolo = protocolo.id,
                numero = protocolo.numero,
                tagEquipo = protocolo.tagEquipo,
                tipoEquipo = protocolo.tipoEquipo,
                fechaHora = protocolo.fechaHoraPrueba,
                temperaturaC = protocolo.temperaturaC,
                humedadPct = protocolo.humedadPct
            )
        }
    }

    suspend fun obtenerHistorialProtocolos(): List<ProtocoloHistorial> = ejecutarApi { api.historial() }
        .map { it.aHistorial() }

    suspend fun obtenerHistorialEquipo(idEquipo: Int): List<ProtocoloResumen> = ejecutarApi {
        api.protocolosEquipo(idEquipo.toLong())
    }.map { it.aResumen() }

    suspend fun obtenerHistorialEquipoCliente(idEquipo: Int): List<ProtocoloResumen> = obtenerHistorialEquipo(idEquipo)

    suspend fun obtenerProtocolosCliente(): List<ProtocoloHistorial> = ejecutarApi { api.protocolosCliente() }
        .map { it.aHistorial() }

    suspend fun obtenerDetalleProtocolo(idProtocolo: Int): ProtocoloDetalle = ejecutarApi {
        api.detalle(idProtocolo.toLong())
    }.aDetalle()

    suspend fun reemplazarProtocolo(idAnterior: Long, request: BorradorRequestDto): Long =
        ejecutarApi { api.reemplazar(idAnterior, request) }.id

    private fun ProtocoloDto.aResumen() = ProtocoloResumen(
        idProtocolo = id.toInt(),
        numeroProtocolo = numero,
        fechaHora = fechaHoraPrueba,
        estado = if (estadoCodigo == "REGISTRADO_TERRENO") "Pendiente de formalización" else estadoNombre,
        resultadoGlobal = resultadoGlobal,
        tecnicoResponsable = tecnicoNombre
    )

    private fun ProtocoloDto.aHistorial() = ProtocoloHistorial(
        idProtocolo = id,
        numero = numero,
        cliente = clienteNombre,
        instalacion = instalacionNombre,
        ubicacion = ubicacion.orEmpty(),
        tagEquipo = tagEquipo,
        tipoEquipo = tipoEquipo,
        marca = marca.orEmpty(),
        modelo = modelo.orEmpty(),
        tecnico = tecnicoNombre,
        fechaHora = fechaHoraPrueba,
        temperaturaC = temperaturaC,
        humedadPct = humedadPct,
        estadoCodigo = estadoCodigo,
        estadoNombre = estadoNombre,
        observaciones = observaciones.orEmpty(),
        pruebas = pruebas.map { prueba ->
            PruebaHistorial(
                idPrueba = prueba.id.toLong(),
                nombre = prueba.tipoPrueba,
                elemento = prueba.elemento.orEmpty(),
                realizada = prueba.realizada,
                idResultado = prueba.idResultado,
                mediciones = prueba.mediciones.map { MedicionHistorial(it.parametro, it.valor, it.unidad) }
            )
        }
    )

    private fun ProtocoloDto.aDetalle() = ProtocoloDetalle(
        idProtocolo = id.toInt(),
        numeroProtocolo = numero,
        fechaHora = fechaHoraPrueba,
        estadoCodigo = estadoCodigo,
        estadoEtiqueta = if (estadoCodigo == "REGISTRADO_TERRENO") "Pendiente de formalización" else estadoNombre,
        resultadoGlobal = resultadoGlobal,
        tecnicoResponsable = tecnicoNombre,
        clienteNombre = clienteNombre,
        instalacionNombre = instalacionNombre,
        equipoTag = tagEquipo,
        tipoEquipo = tipoEquipo,
        temperatura = "${temperaturaC} °C",
        humedad = "${humedadPct} %",
        observaciones = observaciones.orEmpty(),
        pruebas = pruebas.map { prueba ->
            PruebaDetalle(
                idPrueba = prueba.id.toInt(),
                tipoPruebaNombre = prueba.tipoPrueba,
                elemento = prueba.elemento.orEmpty(),
                momento = prueba.momento,
                valorMedido = prueba.mediciones.joinToString("\n") { "${it.parametro}: ${it.valor} ${it.unidad}" }
                    .ifBlank { if (prueba.realizada) "Sin mediciones" else "Prueba pendiente" },
                unidadMedida = "",
                resultado = if (prueba.realizada) prueba.resultado else "Pendiente"
            )
        }
    )

    private fun fechaIso(valor: String): String = runCatching {
        LocalDateTime.parse(valor, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }.getOrElse { valor }

    private fun validarUsuarioSesion(idUsuario: Long) {
        val actual = preferencias.leerSesion()
        if (actual > 0L && actual != idUsuario) {
            throw IllegalStateException("La sesión cambió. Vuelve a iniciar sesión antes de continuar.")
        }
    }
}
