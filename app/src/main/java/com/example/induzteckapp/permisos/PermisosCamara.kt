package com.example.induzteckapp.permisos

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermisosCamara {

    const val CAMARA = Manifest.permission.CAMERA

    fun tienePermiso(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            CAMARA
        ) == PackageManager.PERMISSION_GRANTED
    }
}
