package model

import kotlinx.serialization.Serializable

@Serializable
data class DropdownOption(
    val optionId: Int,
    val optionName: String,
    val pValue: Int,
    val isChecked: Boolean = false
) {
    fun doesMatchSearchQuery(query: String): Boolean = optionName.contains(query, ignoreCase = true)
}