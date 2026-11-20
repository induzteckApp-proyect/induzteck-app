package com.example.induzteckapp.viewmodel

import com.example.induzteckapp.model.ClienteConsulta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ListaClientesViewModelTest {
    private val clientes = listOf(
        ClienteConsulta(1, "Sanitaria Valle Central", "96731540-0", correo = "contacto@valle.cl"),
        ClienteConsulta(2, "Minera Cerro Alto", "77105362-9", correo = "administracion@cerroalto.cl")
    )

    @Test
    fun filtrarClientesEncuentraCoincidenciasPorNombreRutOCorreo() {
        val porNombre = ListaClientesViewModel.filtrarClientes(clientes, "sanitaria")
        val porRut = ListaClientesViewModel.filtrarClientes(clientes, "77105362-9")
        val porCorreo = ListaClientesViewModel.filtrarClientes(clientes, "cerroalto.cl")

        assertEquals(1, porNombre.size)
        assertEquals(1, porRut.size)
        assertEquals(1, porCorreo.size)
        assertEquals(2, porCorreo.first().idUsuario)
    }

    @Test
    fun alternarExpansionSoloModificaAlClienteSeleccionado() {
        val resultado = ListaClientesViewModel.alternarExpansionCliente(clientes, 2)
        assertFalse(resultado[0].isExpanded)
        assertTrue(resultado[1].isExpanded)
    }
}
