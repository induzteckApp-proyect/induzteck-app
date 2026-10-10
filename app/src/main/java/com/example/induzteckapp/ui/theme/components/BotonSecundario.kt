package com.example.induzteckapp.ui.theme.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.induzteckapp.ui.theme.styles.EstilosBoton
import com.example.induzteckapp.ui.theme.styles.estiloAltoBoton
import com.example.induzteckapp.ui.theme.theme.MiRegistroTheme

// =====================================================================
//  BotonSecundario.kt  —  Acción de menor importancia (Limpiar, Cancelar).
//  Parte de OutlinedButton (solo borde) y aplica NUESTROS estilos.
// =====================================================================

@Composable
fun BotonSecundario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.estiloAltoBoton(),
        shape = EstilosBoton.forma,
        colors = EstilosBoton.coloresSecundario(),
        border = EstilosBoton.bordeSecundario(),       // border: 2px solid
        contentPadding = EstilosBoton.relleno
    ) {
        Text(text = texto)
    }
}

@Preview(showBackground = true)
@Composable
private fun BotonSecundarioPreview() {
    MiRegistroTheme {
        BotonSecundario(texto = "Limpiar", onClick = {}, modifier = Modifier.padding(16.dp))
    }
}
