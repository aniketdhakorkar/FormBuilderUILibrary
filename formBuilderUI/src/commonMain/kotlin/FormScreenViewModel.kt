import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import util.DependentValueCustomText
import util.InputWrapper
import util.SendUiEvent
import model.parameters.ChildrenX
import validation.calculateRemainingValuesForFocusChange
import validation.calculateRemainingValuesForValueChange
import validation.expressionValidation
import validation.hideAndShowValidation
import validation.validateInputInRange

class FormScreenViewModel : ViewModel() {

    private val _localParameterMap = MutableStateFlow<Map<Int, ChildrenX>>(emptyMap())
    val localParameterMap = _localParameterMap.asStateFlow()
    private val _localParameterValueMap =
        MutableStateFlow<Map<Int, InputWrapper>>(emptyMap())
    val localParameterValueMap = _localParameterValueMap.asStateFlow()
    private val _localVisibilityMap = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val localVisibilityMap = _localVisibilityMap.asStateFlow()
    private val _dependentOperatorMap = MutableStateFlow<Map<List<Int>, List<Int>>>(emptyMap())
    private val dependentOperatorMap = _dependentOperatorMap.asStateFlow()
    private val _dependentValueMap =
        MutableStateFlow<Map<Int, DependentValueCustomText>>(emptyMap())
    val dependentValueMap = _dependentValueMap.asStateFlow()
    private val _showProgressIndicator = MutableStateFlow(false)
    val showProgressIndicator = _showProgressIndicator.asStateFlow()
    private val _uiEvent = Channel<String>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onEvent(event: FormScreenEvent) {
        when (event) {
            is FormScreenEvent.OnDropdownValueChanged -> {

                if (_localParameterMap.value[event.elementId]?.elementOptionDependent != null) {
                    val visibilityMap = hideAndShowValidation(
                        elementOptionDependent = _localParameterMap.value[event.elementId]?.elementOptionDependent,
                        optionId = event.option.optionId,
                    )
                    _localVisibilityMap.value = _localVisibilityMap.value.toMutableMap().apply {
                        visibilityMap.forEach { (key, value) ->
                            put(key, value)
                        }
                    }
                }

                _localParameterValueMap.value = _localParameterValueMap.value.toMutableMap().apply {
                    put(
                        event.elementId,
                        InputWrapper(value = event.option.optionId.toString(), errorMessage = "")
                    )
                }
            }

            is FormScreenEvent.OnTextFieldValueFocusChanged -> {
                if (event.isFocused) {

                    val remainingValue =
                        calculateRemainingValuesForFocusChange(
                            elementId = event.elementId,
                            dependentOperatorMap = dependentOperatorMap.value,
                            localParameterValueMap = localParameterValueMap.value
                        )

                    _dependentValueMap.value = _dependentValueMap.value.mapValues { (key, value) ->
                        value.copy(
                            isShow = event.elementId == key && remainingValue != 0,
                            value = "$remainingValue"
                        )
                    }.toMutableMap()
                }
            }

            is FormScreenEvent.OnTextFieldValueChanged -> {
                if ((_localParameterMap.value[event.elementId]?.inputType ?: "") == "number") {
                    val minValue = _localParameterMap.value[event.elementId]?.minVal
                    val maxValue = _localParameterMap.value[event.elementId]?.maxVal

                    if (minValue != null && maxValue != null) {
                        val result = validateInputInRange(
                            newValue = event.value,
                            minValue = minValue,
                            maxValue = maxValue
                        )
                        if (result.isNotEmpty()) {
                            SendUiEvent.send(
                                viewModelScope = viewModelScope,
                                _uiEvent = _uiEvent,
                                event = result
                            )
                            _localParameterValueMap.value =
                                _localParameterValueMap.value.toMutableMap().apply {
                                    put(
                                        event.elementId,
                                        InputWrapper(
                                            value = event.value,
                                            errorMessage = result,
                                            isFocus = true
                                        )
                                    )
                                }
                            return
                        }
                    }

                    val (remainingValue, parentValue, childValue, expression, dependentValue) = calculateRemainingValuesForValueChange(
                        elementId = event.elementId,
                        newValue = event.value,
                        dependentOperatorMap = dependentOperatorMap.value,
                        dependentValueMap = dependentValueMap.value,
                        localParameterValueMap = localParameterValueMap.value
                    )

                    val errorMessage = expressionValidation(
                        expression = expression,
                        remainingValue = remainingValue,
                        parentValue = parentValue,
                        childValue = childValue,
                        dependentValue = dependentValue,
                    )

                    if (errorMessage.isNotBlank()) {
                        SendUiEvent.send(
                            viewModelScope = viewModelScope,
                            _uiEvent = _uiEvent,
                            event = errorMessage
                        )
                        _localParameterValueMap.value =
                            _localParameterValueMap.value.toMutableMap().apply {
                                put(
                                    event.elementId,
                                    InputWrapper(
                                        value = localParameterValueMap.value[event.elementId]?.value
                                            ?: "",
                                        errorMessage = errorMessage,
                                        isFocus = true
                                    )
                                )
                            }
                        return
                    }
                }

                _localParameterValueMap.value = _localParameterValueMap.value.toMutableMap().apply {
                    put(event.elementId, InputWrapper(value = event.value, errorMessage = ""))
                }
            }

            FormScreenEvent.OnSubmitButtonClicked -> {
                _showProgressIndicator.value = true
                if (_localParameterValueMap.value.any { it.value.value.isEmpty() }) {
                    SendUiEvent.send(
                        viewModelScope = viewModelScope,
                        _uiEvent = _uiEvent,
                        event = "Field should not be empty"
                    )
                }
                _showProgressIndicator.value = false
            }
        }
    }

    fun initData(
        parameterValueMap: MutableMap<Int, InputWrapper>,
        parameterMap: MutableMap<Int, ChildrenX>,
        visibilityMap: MutableMap<Int, Boolean>
    ) {
        _localParameterValueMap.value = parameterValueMap
        _localParameterMap.value = parameterMap
        _localVisibilityMap.value = visibilityMap
        val tempDependentValueMap = _dependentValueMap.value.toMutableMap()
        val tempDependentOperatorMap = _dependentOperatorMap.value.toMutableMap()

        _localParameterMap.value.forEach { (_, element) ->
            element.validation.forEach { validation ->
                validation.values.forEach { value ->
                    val operatorKeys = value.dependentOperator.split(",").map(String::toInt)
                    val resultValues = value.dependentResult.split(",").map(String::toInt)

                    tempDependentOperatorMap[operatorKeys] = resultValues

                    operatorKeys.forEach { key ->
                        tempDependentValueMap[key] = DependentValueCustomText(
                            isShow = false,
                            expression = value.dependant,
                            value = ""
                        )
                    }
                }
            }
        }

        _dependentOperatorMap.value = tempDependentOperatorMap
        _dependentValueMap.value = tempDependentValueMap
    }
}