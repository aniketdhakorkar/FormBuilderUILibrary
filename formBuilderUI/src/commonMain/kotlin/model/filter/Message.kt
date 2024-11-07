package model.filter


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    @SerialName("message")
    val message: String,
    @SerialName("token_class")
    val tokenClass: String,
    @SerialName("token_type")
    val tokenType: String
)