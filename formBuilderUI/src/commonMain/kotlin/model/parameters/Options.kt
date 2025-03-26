package model.parameters


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.DropdownOption

@Serializable
data class Options(
    @SerialName("option_id")
    val optionId: Int,
    @SerialName("option_name")
    val optionName: OptionName,
    @SerialName("pvalue")
    val pValue: Int,
    @SerialName("prompts")
    val prompts: String? = null
)

fun Options.toDropdown() =
    DropdownOption(optionId = optionId, pValue = pValue, optionName = optionName.en ?: "", prompts = prompts)