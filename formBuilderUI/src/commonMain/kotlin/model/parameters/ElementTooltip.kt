package model.parameters


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElementTooltip(
    @SerialName("as")
    val asX: String?,
    @SerialName("bn")
    val bn: String?,
    @SerialName("en")
    val en: String?,
    @SerialName("gu")
    val gu: String?,
    @SerialName("hi")
    val hi: String?,
    @SerialName("kn")
    val kn: String?,
    @SerialName("mr")
    val mr: String?,
    @SerialName("or")
    val or: String?,
    @SerialName("pa")
    val pa: String?,
    @SerialName("ta")
    val ta: String?,
    @SerialName("te")
    val te: String?,
    @SerialName("ur")
    val ur: String?
)