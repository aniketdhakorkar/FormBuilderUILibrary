import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import model.filter.FilterResponseDto
import model.filter.toDropdown
import util.DependentValueCustomText
import util.InputWrapper
import util.SendUiEvent
import model.parameters.ChildrenX
import model.DropdownOption
import validation.calculateRemainingValuesForFocusChange
import validation.calculateRemainingValuesForValueChange
import validation.checkMobileNoValidation
import validation.expressionValidation
import validation.hideAndShowValidation
import validation.validateInputInRange

class FormScreenViewModel : ViewModel() {

    private val _localParameterMap = MutableStateFlow<Map<Int, ChildrenX>>(emptyMap())
    val localParameterMap = _localParameterMap.asStateFlow()
    private val _localParameterValueMap =
        MutableStateFlow<Map<Int, InputWrapper>>(emptyMap())
    val localParameterValueMap = _localParameterValueMap.asStateFlow()
    private val _localVisibilityStatusMap = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val localVisibilityStatusMap = _localVisibilityStatusMap.asStateFlow()
    private val _localEnabledStatusMap = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val localEnabledStatusMap = _localEnabledStatusMap.asStateFlow()
    private val _dependentOperatorMap = MutableStateFlow<Map<List<Int>, List<Int>>>(emptyMap())
    private val dependentOperatorMap = _dependentOperatorMap.asStateFlow()
    private val _dependentValueMap =
        MutableStateFlow<Map<Int, DependentValueCustomText>>(emptyMap())
    val dependentValueMap = _dependentValueMap.asStateFlow()
    private val _onlineDropdownOptionMap =
        MutableStateFlow<Map<Int, List<DropdownOption>>>(emptyMap())
    val onlineDropdownOptionMap = _onlineDropdownOptionMap.asStateFlow()
    private var _token: String = ""
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
                    _localVisibilityStatusMap.value =
                        _localVisibilityStatusMap.value.toMutableMap().apply {
                            visibilityMap.forEach { (key, value) ->
                                put(key, value)
                            }
                        }
                }

                _localParameterValueMap.value = _localParameterValueMap.value.toMutableMap().apply {
                    put(
                        event.elementId,
                        InputWrapper(value = event.option.pValue.toString(), errorMessage = "")
                    )
                }

                //for filter in home module
                val elementData = _localParameterMap.value[event.elementId]?.elementData
                if (elementData?.dataUrl != null) {
                    if (elementData.dependentApi != null) {
                        elementData.dependentApi.forEach {
                            viewModelScope.launch {
                                var filterMap: Map<String, String> = emptyMap()
                                it.parameter.forEach { (key, value) ->
                                    filterMap = filterMap.toMutableMap().apply {
                                        put(
                                            key = key,
                                            value = _localParameterValueMap.value[value]?.value
                                                ?: "0"
                                        )
                                    }

                                }
                                remoteApi(
                                    url = it.url,
                                    filterMap = filterMap,
                                    elementId = it.dependent,
                                    token = _token
                                )
                            }

                            _localParameterValueMap.value =
                                _localParameterValueMap.value.toMutableMap().apply {
                                    put(it.dependent, InputWrapper(value = "", errorMessage = ""))
                                }
                        }
                    }
                }
            }

            is FormScreenEvent.OnTextFieldFocusChanged -> {
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
                val valueType = (_localParameterMap.value[event.elementId]?.inputType ?: "")

                when (valueType) {
                    "number" -> {
                        if (event.value.all { it.isDigit() }) {
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

                            _localParameterValueMap.value = _localParameterValueMap.value
                                .toMutableMap()
                                .apply {
                                    put(
                                        event.elementId,
                                        InputWrapper(value = event.value, errorMessage = "")
                                    )
                                }
                        }
                    }

                    "text" -> {
                        _localParameterValueMap.value = _localParameterValueMap.value
                            .toMutableMap()
                            .apply {
                                put(
                                    event.elementId,
                                    InputWrapper(value = event.value, errorMessage = "")
                                )
                            }
                    }

                    "mobile" -> {
                        if (event.value.all { it.isDigit() }) {

                            val result = checkMobileNoValidation(mobileNo = event.value)

                            if (result.isNotBlank()) {
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
                            _localParameterValueMap.value = _localParameterValueMap.value
                                .toMutableMap()
                                .apply {
                                    put(
                                        event.elementId,
                                        InputWrapper(value = event.value, errorMessage = "")
                                    )
                                }
                        }
                    }
                }
            }

            is FormScreenEvent.OnSubmitButtonClicked -> {
                _showProgressIndicator.value = true

                val isFieldEmpty =
                    _localParameterValueMap.value.isEmpty() || _localParameterValueMap.value.any { (key, inputWrapper) ->
                        val elementType = _localParameterMap.value[key]?.elementType
                        val isVisible = _localVisibilityStatusMap.value[key] == true
                        val isRequired = _localParameterMap.value[key]?.isRequired == "true"
                        elementType != "ElementLabel" && isVisible && isRequired && inputWrapper.value.isEmpty()
                    }

                val firstError =
                    _localParameterValueMap.value.entries.firstOrNull { it.value.errorMessage.isNotBlank() }

                when {
                    isFieldEmpty -> {
                        SendUiEvent.send(
                            viewModelScope = viewModelScope,
                            _uiEvent = _uiEvent,
                            event = "Field should not be empty"
                        )
                    }

                    firstError != null -> {
                        SendUiEvent.send(
                            viewModelScope = viewModelScope,
                            _uiEvent = _uiEvent,
                            event = firstError.value.errorMessage
                        )
                    }

                    else -> {
                        event.onClick(_localParameterValueMap.value)
                    }
                }

                _showProgressIndicator.value = false
            }

            is FormScreenEvent.OnCameraButtonClicked -> {
                val currentData = _localParameterValueMap.value[event.elementId]?.value.orEmpty()
                val updatedData = if (currentData.isBlank()) {
                    event.data
                } else {
                    "$currentData&${event.data}"
                }

                _localParameterValueMap.value = _localParameterValueMap.value.toMutableMap().apply {
                    put(event.elementId, InputWrapper(value = updatedData, errorMessage = ""))
                }

            }

            is FormScreenEvent.OnPhotoDeleteButtonClicked -> {
                val currentData = _localParameterValueMap.value[event.elementId]?.value.orEmpty()
                val updatedData = currentData.split("&").toMutableList().apply {
                    if (event.index in indices) {
                        removeAt(event.index)
                    }
                }.joinToString(separator = "&")

                _localParameterValueMap.value = _localParameterValueMap.value.toMutableMap().apply {
                    put(event.elementId, InputWrapper(value = updatedData, errorMessage = ""))
                }
            }
        }
    }

    fun initData(
        parameterValueMap: Map<Int, InputWrapper>,
        parameterMap: Map<Int, ChildrenX>,
        visibilityMap: Map<Int, Boolean>,
        enabledStatusMap: Map<Int, Boolean>,
        token: String
    ) {
        _localParameterValueMap.value = parameterValueMap
        _localParameterMap.value = parameterMap
        _localVisibilityStatusMap.value = visibilityMap
        _localEnabledStatusMap.value = enabledStatusMap
        _token = token
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

            if (!element.elementData.dataUrl.isNullOrBlank()) {
                viewModelScope.launch {
                    try {
                        remoteApi(
                            url = element.elementData.dataUrl,
                            elementId = element.elementId,
                            filterMap = emptyMap(),
                            token = token
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        SendUiEvent.send(
                            viewModelScope = viewModelScope,
                            _uiEvent = _uiEvent,
                            event = e.message ?: "An unknown error occurred. Please try again."
                        )
                    }
                }
            }
        }

        _dependentOperatorMap.value = tempDependentOperatorMap
        _dependentValueMap.value = tempDependentValueMap
    }

    private suspend fun remoteApi(
        url: String,
        filterMap: Map<String, String>,
        elementId: Int,
        token: String
    ) {
        val httpClient = provideHttpClient(token = token)

        val result = try {
            httpClient.get(url) {
                url {
                    filterMap.forEach { (key, value) ->
                        parameters.append(key, value)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            throw Exception("Network error occurred. Please check your internet connection and try again.")
        }

        when (result.status.value) {
            in 200..299 -> Unit
            500 -> throw Exception("Internal server error. Please try again later.")
            in 400..499 -> {
                throw Exception("Client error: ${result.body<FilterResponseDto>().errors?.detail}")
            }

            else -> throw Exception("An unknown error occurred. Please try again.")
        }

        try {
            _onlineDropdownOptionMap.value = _onlineDropdownOptionMap.value.toMutableMap().apply {
                put(
                    elementId,
                    result.body<FilterResponseDto>().data?.map { it.toDropdown() } ?: emptyList()
                )
            }
            httpClient.close()
        } catch (e: Exception) {
            throw Exception("Failed to parse the response. Please try again later.", e)
        }
    }

    private fun provideHttpClient(token: String): HttpClient = HttpClient {
        install(HttpTimeout) {
            socketTimeoutMillis = 60_000
            requestTimeoutMillis = 60_000
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
            logger = object : Logger {
                override fun log(message: String) {
                    co.touchlab.kermit.Logger.d("KtorClient") {
                        message
                    }
                }
            }
        }
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(Auth) {
            bearer {
                loadTokens {
                    BearerTokens(accessToken = token, refreshToken = null)
                }
            }
        }
    }
}