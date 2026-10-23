package com.example.induzteckapp.ui.theme.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectorDesplegable(
    etiqueta: String,
    opciones: List<T>,
    seleccionada: T?,
    alSeleccionar: (T) -> Unit,
    textoDe: (T) -> String,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    mensajeVacio: String = "No hay opciones disponibles"
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = {
            if (habilitado && opciones.isNotEmpty()) {
                expandido = !expandido
            }
        },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = seleccionada?.let(textoDe) ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = habilitado,
            label = { Text(etiqueta) },
            placeholder = {
                Text(
                    if (opciones.isEmpty()) mensajeVacio
                    else "Selecciona una opción"
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expandido
                )
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true
        )

        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = textoDe(opcion),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        alSeleccionar(opcion)
                        expandido = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}