
package com.example.induzteckapp.ui.theme.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.induzteckapp.R
import com.example.induzteckapp.ui.theme.theme.Dimens
import com.example.induzteckapp.ui.theme.theme.MiRegistroTheme


@Composable
fun AvatarCircular(
    @DrawableRes imagen: Int,
    descripcion: String,
    modifier: Modifier = Modifier
){
    //etiqueta img
    Image(
        painter = painterResource(imagen),
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

@Preview(showBackground = true)
@Composable
private fun AvatarCircularPreview(){
    MiRegistroTheme {
        AvatarCircular(
            R.drawable.avatar_usuario,
            "soy una descripcion",
            Modifier.padding(16.dp)
        )
    }
}