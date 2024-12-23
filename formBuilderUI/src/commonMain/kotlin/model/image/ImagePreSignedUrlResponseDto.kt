package model.image


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ImagePreSignedUrlResponseDto(
    @SerialName("status")
    val status: String,
    @SerialName("urls")
    val urls: List<String>?,
    @SerialName("message")
    val message: String? = null
)