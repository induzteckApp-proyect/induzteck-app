package com.example.induzteckapp.data

import android.content.Context
import android.util.Base64
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.core.content.edit
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Guarda el JWT cifrado con una clave AES protegida por Android Keystore.
 * La clave privada del dispositivo no se almacena en SharedPreferences.
 */
class TokenSeguroStorage(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(
        "induztek_token_seguro", Context.MODE_PRIVATE
    )

    fun guardarToken(token: String) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, obtenerClave())
        val cifrado = cipher.doFinal(token.toByteArray(StandardCharsets.UTF_8))
        prefs.edit {
            putString("iv", Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            putString("datos", Base64.encodeToString(cifrado, Base64.NO_WRAP))
        }
    }

    fun leerToken(): String? {
        val ivTexto = prefs.getString("iv", null) ?: return null
        val datosTexto = prefs.getString("datos", null) ?: return null
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = Base64.decode(ivTexto, Base64.NO_WRAP)
            val datos = Base64.decode(datosTexto, Base64.NO_WRAP)
            cipher.init(Cipher.DECRYPT_MODE, obtenerClave(), GCMParameterSpec(128, iv))
            String(cipher.doFinal(datos), StandardCharsets.UTF_8)
        } catch (_: Exception) {
            borrarToken()
            null
        }
    }

    fun borrarToken() {
        prefs.edit { remove("iv"); remove("datos") }
    }

    private fun obtenerClave(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existente = runCatching { keyStore.getKey(ALIAS, null) as? SecretKey }.getOrNull()
        if (existente != null) return existente
        runCatching { keyStore.deleteEntry(ALIAS) }

        val generador = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
        )
        val parametros = KeyGenParameterSpec.Builder(
            ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()
        generador.init(parametros)
        return generador.generateKey()
    }

    private companion object {
        const val ALIAS = "induztek_jwt_keystore"
    }
}
