package model

import kotlinx.serialization.Serializable

@Serializable
data class DropdownOption(
    val optionId: Int,
    val optionName: String,
    val pValue: Int,
    val isChecked: Boolean = false,
    val dbParam: String = "",
    val prompts: String? = null
) {
    fun doesMatchSearchQuery(query: String): Boolean = optionName.contains(query, ignoreCase = true)
}