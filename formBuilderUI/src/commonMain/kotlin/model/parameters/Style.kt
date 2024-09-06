package model.parameters

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Style(
    @SerialName("text_style")
    val textStyle: String,
    @SerialName("background_color")
    val backgroundColor: String,
    @SerialName("font_color")
    val fontColor: String
)
