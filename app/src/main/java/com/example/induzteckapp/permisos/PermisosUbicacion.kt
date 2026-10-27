package com.example.induzteckapp.permisos

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermisosUbicacion {

    const val UBICACION_APROXIMADA = Manifest.permission.ACCESS_COARSE_LOCATION
    const val UBICACION_PRECISA = Manifest.permission.ACCESS_FINE_LOCATION

    val PERMISOS_UBICACION = arrayOf(
        UBICACION_APROXIMADA,
        UBICACION_PRECISA
    )

    fun tienePermiso(context: Context): Boolean {
        val aproximada = ContextCompat.checkSelfPermission(
            context,
            UBICACION_APROXIMADA
        ) == PackageManager.PERMISSION_GRANTED

        val precisa = ContextCompat.checkSelfPermission(
            context,
            UBICACION_PRECISA
        ) == PackageManager.PERMISSION_GRANTED

        return aproximada || precisa
    }

    fun abrirAjustesAplicacion(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(intent)
    }

    fun abrirAjustesUbicacion(context: Context) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(intent)
    }
}
