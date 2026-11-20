package com.example.induzteckapp.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CrearProtocoloViewModelTest {
    @Test
    fun condicionesAmbientalesAceptanLosLimitesPermitidos() {
        val errores = CrearProtocoloViewModel.validarCondicionesAmbientales(-10.0, 100.0)
        assertTrue(errores.isEmpty())
    }

    @Test
    fun temperaturaFueraDeRangoGeneraErrorEnElCampoCorrespondiente() {
        val errores = CrearProtocoloViewModel.validarCondicionesAmbientales(60.1, 45.0)
        assertEquals(1, errores.size)
        assertTrue(errores.containsKey("temperaturaC"))
    }

    @Test
    fun humedadFueraDeRangoGeneraErrorEnElCampoCorrespondiente() {
        val errores = CrearProtocoloViewModel.validarCondicionesAmbientales(20.0, -0.1)
        assertEquals(1, errores.size)
        assertTrue(errores.containsKey("humedadPct"))
    }
}
