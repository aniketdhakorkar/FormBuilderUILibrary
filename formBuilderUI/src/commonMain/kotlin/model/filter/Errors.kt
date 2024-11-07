package model.filter

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Errors(
    @SerialName("code")
    val code: String? = null,
    @SerialName("detail")
    val detail: String? = null,
    @SerialName("messages")
    val messages: List<Message>? = null
)
