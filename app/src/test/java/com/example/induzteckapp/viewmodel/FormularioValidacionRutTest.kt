package com.example.induzteckapp.viewmodel

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FormularioValidacionRutTest {
    @Test
    fun validarRutAceptaDigitoVerificadorCorrectoConFormatoSimple() {
        assertTrue(FormularioViewModel.validarRut("12345678-5"))
    }

    @Test
    fun validarRutAceptaRutConPuntosYRechazaDigitoIncorrecto() {
        assertTrue(FormularioViewModel.validarRut("12.345.678-5"))
        assertFalse(FormularioViewModel.validarRut("12.345.678-9"))
    }
}
