package com.example.induzteckapp.ui.theme.components

import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.induzteckapp.R
import com.example.induzteckapp.ui.theme.theme.Dimens
import com.example.induzteckapp.ui.theme.theme.MiRegistroTheme

@Composable
fun AvatarCircular(
    avatarBytes: ByteArray? = null,
    @DrawableRes defaultImagen: Int = R.drawable.avatar_usuario,
    descripcion: String = "Avatar de usuario",
    modifier: Modifier = Modifier
) {
    val painterResourceDefault = painterResource(defaultImagen)
    val painter: Painter = remember(avatarBytes, defaultImagen) {
        if (avatarBytes != null && avatarBytes.isNotEmpty()) {
            try {
                val bitmap = BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.size)
                if (bitmap != null) {
                    BitmapPainter(bitmap.asImageBitmap())
                } else null
            } catch (e: Exception) {
                null
            }
        } else null
    } ?: painterResourceDefault

    Image(
        painter = painter,
        contentDescription = descripcion,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(Dimens.tamanoAvatar)
            .shadow(elevation = 8.dp, CircleShape)
            .clip(CircleShape)
            .border(
                Dimens.bordeAvatar,
                MaterialTheme.colorScheme.surface,
                CircleShape
            )
    )
}

@Composable
fun AvatarCircular(
    @DrawableRes imagen: Int,
    descripcion: String,
    modifier: Modifier = Modifier
) {
    AvatarCircular(
        avatarBytes = null,
        defaultImagen = imagen,
        descripcion = descripcion,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun AvatarCircularPreview() {
    MiRegistroTheme {
        AvatarCircular(
            imagen = R.drawable.avatar_usuario,
            descripcion = "Vista previa avatar",
            modifier = Modifier.padding(16.dp)
        )
    }
}
