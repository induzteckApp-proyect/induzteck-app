package com.example.induzteckapp.viewmodel

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginViewModelTest {
    @Test
    fun validarContrasenaAceptaMayusculaMinusculaNumeroYSimbolo() {
        assertTrue(LoginViewModel.validarContrasena("Abc1234@"))
    }

    @Test
    fun validarContrasenaReconoceLaEnyeMayusculaComoMayuscula() {
        assertTrue(LoginViewModel.validarContrasena("Ñandú123@"))
    }

    @Test
    fun validarContrasenaNoConsideraLaEnyeComoSimbolo() {
        assertFalse(LoginViewModel.validarContrasena("Ñandú1234"))
    }

    @Test
    fun validarContrasenaRechazaUnaClaveSinMayuscula() {
        assertFalse(LoginViewModel.validarContrasena("abc1234@"))
    }

    @Test
    fun validarContrasenaRechazaEspacios() {
        assertFalse(LoginViewModel.validarContrasena("Abc 1234@"))
    }
}
