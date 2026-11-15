package com.example.induzteckapp.ui.theme.styles

// com.example.induzteckapp/styles/EstilosBoton.kt

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.induzteckapp.ui.theme.theme.Blanco
import com.example.induzteckapp.ui.theme.theme.GrisDeshabilitado
import com.example.induzteckapp.ui.theme.theme.Primario
import com.example.induzteckapp.ui.theme.theme.Dimens

object EstilosBoton {
    val forma: Shape = RoundedCornerShape(Dimens.radioBoton) //border-radius
    val relleno = PaddingValues(horizontal = 20.dp, vertical = 12.dp) //padding
    //boton principal: fondo morado y texto blanco
    @Composable
    fun coloresPrincipal(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = Primario, //background-color
        contentColor = Blanco, //color
        disabledContainerColor = GrisDeshabilitado,
        disabledContentColor = Blanco
    )
    //botones secundarios: transparente, texto color del tema del tlf
    @Composable
    fun coloresSecundario(): ButtonColors = ButtonDefaults.outlinedButtonColors(
        contentColor = MaterialTheme.colorScheme.primary
    )
    //borde secundario
    @Composable
    fun bordeSecundario(): BorderStroke = BorderStroke(
        Dimens.bordeBoton,
        MaterialTheme.colorScheme.primary
    )
    //boton principal (animacion = presione el boton se hunda)
    @Composable
    fun elevacion(): ButtonElevation = ButtonDefaults.buttonElevation(
        defaultElevation = Dimens.elevacionBoton,
        pressedElevation = 1.dp
    )
}
fun Modifier.estiloAltoBoton(): Modifier = this.height(Dimens.alturaBoton)