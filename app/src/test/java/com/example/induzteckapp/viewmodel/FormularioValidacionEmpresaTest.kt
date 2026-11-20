package com.example.induzteckapp.viewmodel

import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class FormularioValidacionEmpresaTest {
    @Test
    fun datosEmpresaValidosConAcentosNoGeneranErrores() {
        val errores = FormularioViewModel.validarDatosEmpresa(
            "Servicios eléctricos",
            "María José Núñez",
            "Jefa de Operaciones"
        )
        assertTrue(errores.isEmpty())
    }

    @Test
    fun datosEmpresaOpcionalesPuedenQuedarVacios() {
        val errores = FormularioViewModel.validarDatosEmpresa("", "", "")
        assertTrue(errores.isEmpty())
    }

    @Test
    fun datosEmpresaConCaracteresNoPermitidosSonRechazados() {
        val errores = FormularioViewModel.validarDatosEmpresa(
            "Servicios 123",
            "12345",
            "Jefe_Operaciones"
        )
        assertEquals(3, errores.size)
    }
}
