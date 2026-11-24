package com.example.induzteckapp.viewmodel

import com.example.induzteckapp.model.Instalacion
import org.junit.Assert.assertEquals
import org.junit.Test

class ListaInstalacionesViewModelTest {
    private val instalaciones = listOf(
        Instalacion(1, 10, "Planta Lampa", "Sector industrial", 1, "Lampa", null),
        Instalacion(2, 11, "Edificio Clínico", "Zona urbana", 2, "La Florida", null),
        Instalacion(3, 12, "Site Huechuraba", "Parque empresarial", 3, "Huechuraba", null)
    )

    @Test
    fun filtrarInstalacionesBuscaPorNombreDeComunaSinDistinguirMayusculas() {
        val resultado = ListaInstalacionesViewModel.filtrarInstalaciones(instalaciones, "la florida")
        assertEquals(1, resultado.size)
        assertEquals("Edificio Clínico", resultado.first().nombre)
    }

    @Test
    fun filtrarInstalacionesConTextoSinCoincidenciasDevuelveListaVacia() {
        val resultado = ListaInstalacionesViewModel.filtrarInstalaciones(instalaciones, "Antofagasta")
        assertEquals(0, resultado.size)
    }
}
