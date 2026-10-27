

package com.example.induzteckapp


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.induzteckapp.navigation.AppNavegacion
import com.example.induzteckapp.ui.theme.theme.MiRegistroTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            MiRegistroTheme {
                AppNavegacion()
            }
        }
    }
}
