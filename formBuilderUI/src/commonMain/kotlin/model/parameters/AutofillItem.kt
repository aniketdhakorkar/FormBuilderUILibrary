package model.parameters

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AutofillItem(
    @SerialName("activity_id")
    val activityId: Int,
    @SerialName("form_id")
    val formId: Int,
    @SerialName("database_param")
    val databaseParam: String,
    @SerialName("targeted_table")
    val targetedTable: String,
    @SerialName("is_immediate_dependent")
    val isImmediateDependent: Boolean
)
