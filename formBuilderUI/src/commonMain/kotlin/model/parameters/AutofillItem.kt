package model.parameters

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AutofillItem(
    @SerialName("activity_id")
    val activityId: Int? = null,
    @SerialName("form_id")
    val formId: Int? = null,
    @SerialName("database_param")
    val databaseParam: String,
    @SerialName("targeted_table")
    val targetedTable: String,
    @SerialName("is_immediate_dependent")
    val isImmediateDependent: Boolean? = null,
    @SerialName("module_id")
    val moduleId: Int? = null,
    @SerialName("elementOptionDependent")
    val elementOptionDependent: Map<String, String>? = null
)
