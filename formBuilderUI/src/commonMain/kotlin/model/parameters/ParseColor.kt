package model.parameters

import androidx.compose.ui.graphics.Color

fun parseColor(colorString: String): Color {
    val colorLong = colorString.removePrefix("#").toLong(16)

    return if (colorString.length == 7) {
        Color(0xFF000000 or colorLong)
    } else {
        Color(colorLong)
    }
}
