package model.parameters


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChildrenX(
    @SerialName("autocalculate")
    val autoCalculate: List<String>,
    @SerialName("database_param")
    val databaseParam: String,
    @SerialName("dependant")
    val dependant: String,
    @SerialName("dependent_operator")
    val dependentOperator: String,
    @SerialName("dependent_result")
    val dependentResult: String,
    @SerialName("element_data")
    val elementData: ElementData,
    @SerialName("element_id")
    val elementId: Int,
    @SerialName("elementLabel")
    val elementLabel: ElementLabel,
    @SerialName("style")
    val style: Style?,
    @SerialName("element_size")
    val elementSize: Int,
    @SerialName("elementTooltip")
    val elementTooltip: ElementTooltip,
    @SerialName("element_type")
    val elementType: String,
    @SerialName("element_value")
    val elementValue: String?,
    @SerialName("expression")
    val expression: String?,
    @SerialName("input_type")
    val inputType: String,
    @SerialName("is_dependent")
    val isDependent: Boolean,
    @SerialName("is_disable")
    val isDisable: String,
    @SerialName("is_editable")
    val isEditable: String,
    @SerialName("is_required")
    val isRequired: String,
    @SerialName("is_selection")
    val isSelection: String,
    @SerialName("is_visible")
    val isVisible: String?,
    @SerialName("max_val")
    val maxVal: Double?,
    @SerialName("min_val")
    val minVal: Double?,
    @SerialName("operation")
    val operation: String,
    @SerialName("operator")
    val operator: String,
    @SerialName("operator_result")
    val operatorResult: String,
    @SerialName("parameter")
    val parameter: Int,
    @SerialName("validation")
    val validation: List<Validation>,
    @SerialName("elementOptionDependent")
    val elementOptionDependent: Map<String, String>?
)