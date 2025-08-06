import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.parameters
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import model.filter.FilterResponseDto
import model.DependentValueCustomText
import util.InputWrapper
import util.SendUiEvent
import model.parameters.ChildrenX
import model.DropdownOption
import model.ImageModel
import model.filter.toOption
import model.image.ImagePreSignedUrlResponseDto
import model.image.SaveImageResponseDto
import model.parameters.toDropdown
import validation.calculateRemainingValuesForFocusChange
import validation.calculateRemainingValuesForValueChange
import validation.calculateSumForElement
import validation.checkMobileNoValidation
import validation.expressionValidation
import validation.hideAndShowValidation
import validation.validateInputInRange

@OptIn(FlowPreview::class)
class FormScreenViewModel : ViewModel() {
    private lateinit var httpClient: HttpClient
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()
    private var elementId: Int = 0
    private val _localParameterMap = MutableStateFlow<Map<Int, ChildrenX>>(emptyMap())
    val localParameterMap = searchText
        .debounce(1000L)
        .combine(_localParameterMap) { text, parameterMap ->
            if (text.isBlank()) {
                parameterMap
            } else {
                parameterMap.mapValues { (id, childrenX) ->
                    if (id == elementId) {
                        val filteredOptions = childrenX.elementData.options.filter {
                            it.toDropdown().doesMatchSearchQuery(text)
                        }.toList()

                        childrenX.copy(
                            elementData = childrenX.elementData.copy(
                                options = filteredOptions
                            )
                        )
                    } else {
                        childrenX
                    }
                }
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            _localParameterMap.value
        )
    private val _localParameterValueMap =
        MutableStateFlow<Map<Int, InputWrapper>>(emptyMap())
    val localParameterValueMap = _localParameterValueMap.asStateFlow()
    private val _localVisibilityStatusMap = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val localVisibilityStatusMap = _localVisibilityStatusMap.asStateFlow()
    private val _localEnabledStatusMap = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val localEnabledStatusMap = _localEnabledStatusMap.asStateFlow()
    private val _dependentOperatorMap = MutableStateFlow<Map<List<Int>, List<Int>>>(emptyMap())
    private val _dependentValueMap =
        MutableStateFlow<Map<Int, DependentValueCustomText>>(emptyMap())
    val dependentValueMap = _dependentValueMap.asStateFlow()
    private val _operatorMap = MutableStateFlow<Map<List<Int>, List<Int>>>(emptyMap())
    private val _operatorValueMap =
        MutableStateFlow<Map<Int, DependentValueCustomText>>(emptyMap())
    private val _combinationPValueList = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    private val _showProgressIndicator = MutableStateFlow(false)
    val showProgressIndicator = _showProgressIndicator.asStateFlow()
    private val _isSubmitButtonEnabled = MutableStateFlow(true)
    val isSubmitButtonEnabled = _isSubmitButtonEnabled.asStateFlow()
    private val _imageList = MutableStateFlow<Map<Int, List<ImageModel>>>(emptyMap())
    val imageList = _imageList.asStateFlow()
    private val _isViewCamera = MutableStateFlow(false)
    val isViewCamera = _isViewCamera.asStateFlow()
    private var _activity = ""
    private var _form = ""
    private var _action = ""
    private val _hasFocusChangedOnce = MutableStateFlow(false)
    private val _uiEvent = Channel<String>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onEvent(event: FormScreenEvent) {
        when (event) {
            is FormScreenEvent.OnDropdownValueChanged -> {

                try {
                    _localParameterValueMap.value =
                        _localParameterValueMap.value.toMutableMap().apply {
                            val newValue = if (_action == "filter") {
                                Json.encodeToString(event.option)
                            } else {
                                event.option.pValue.toString()
                            }

                            put(event.elementId, InputWrapper(value = newValue, errorMessage = ""))
                        }

                    if (_action != "filter" && _combinationPValueList.value.keys.contains(event.elementId.toString()))
                        checkCombinationOfPValue()
                } catch (e: Exception) {
                    e.printStackTrace()
                    SendUiEvent.send(
                        viewModelScope = viewModelScope,
                        _uiEvent = _uiEvent,
                        event = e.message
                            ?: "An unknown error occurred. Please try again."
                    )
                }

                val visibilityMap = hideAndShowValidation(
                    elementId = event.elementId,
                    parameterMap = _localParameterMap.value,
                    parameterValueMap = _localParameterValueMap.value,
                    selectedOptionIds = listOf(event.option.optionId)
                )

                visibilityMap.filterValues { !it }.forEach { (key, _) ->
                    _localParameterValueMap.value =
                        _localParameterValueMap.value.toMutableMap().apply {
                            this[key] = InputWrapper(
                                value = _localParameterMap.value[key]?.elementValue ?: "",
                                errorMessage = ""
                            )
                        }
                }

                _localVisibilityStatusMap.value =
                    _localVisibilityStatusMap.value.toMutableMap().apply {
                        putAll(visibilityMap)
                    }


                _localParameterMap.value[event.elementId]?.elementData?.let { elementData ->

                    if (elementData.dataUrl != null) {
                        viewModelScope.launch {
                            remoteApi(
                                url = elementData.dataUrl,
                                elementId = event.elementId,
                            )
                        }
                    }
                    elementData.dependentApi?.forEach { api ->
                        viewModelScope.launch {
                            try {
                                val filterMap = api.parameter.mapValues { (_, value) ->
                                    val paramValue =
                                        _localParameterValueMap.value[value]?.value.orEmpty()
                                    if (_action == "filter") {
                                        Json.decodeFromString<DropdownOption>(paramValue).pValue.toString()
                                    } else {
                                        paramValue.ifEmpty { "0" }
                                    }
                                }

                                remoteApi(
                                    url = api.url,
                                    filterMap = filterMap,
                                    elementId = api.dependent
                                )

                                _localParameterValueMap.value =
                                    _localParameterValueMap.value.toMutableMap().apply {
                                        put(
                                            api.dependent,
                                            InputWrapper(value = "", errorMessage = "")
                                        )
                                    }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                SendUiEvent.send(
                                    viewModelScope = viewModelScope,
                                    _uiEvent = _uiEvent,
                                    event = e.message
                                        ?: "An unknown error occurred. Please try again."
                                )
                            }
                        }
                    }
                }
            }

            is FormScreenEvent.OnTextFieldFocusChanged -> {
                if (event.isFocused) {
                    _hasFocusChangedOnce.value = true
                    elementId = event.elementId
                    val remainingValue =
                        calculateRemainingValuesForFocusChange(
                            elementId = event.elementId,
                            dependentOperatorMap = _dependentOperatorMap.value,
                            localParameterValueMap = localParameterValueMap.value
                        )

                    _dependentValueMap.value = _dependentValueMap.value.mapValues { (key, value) ->
                        value.copy(
                            isShow = event.elementId == key && remainingValue != 0,
                            value = "$remainingValue"
                        )
                    }.toMutableMap()

                } else if (!event.isFocused && _hasFocusChangedOnce.value && elementId == event.elementId) {
                    val isFieldEmpty =
                        _localParameterMap.value[event.elementId]?.isRequired == "true" &&
                                _localParameterValueMap.value[event.elementId]?.value?.isEmpty() == true

                    if (isFieldEmpty) {
                        Logger.d("elementId") {
                            event.elementId.toString()
                        }
                        _localParameterValueMap.value =
                            _localParameterValueMap.value.toMutableMap().apply {
                                put(
                                    event.elementId,
                                    InputWrapper(
                                        value = localParameterValueMap.value[event.elementId]?.value
                                            ?: "",
                                        errorMessage = "Field should not be empty",
                                        isFocus = false
                                    )
                                )
                            }
                    }

                    val (remainingValue, parentValue, childValue, expression, dependentValue)
                            = calculateRemainingValuesForValueChange(
                        elementId = event.elementId,
                        newValue = _localParameterValueMap.value[event.elementId]?.value
                            ?: "",
                        dependentOperatorMap = _dependentOperatorMap.value,
                        dependentValueMap = _dependentValueMap.value,
                        localParameterValueMap = _localParameterValueMap.value,
                        isSkipEqualConditions = true
                    )

                    val errorMessage = expressionValidation(
                        expression = expression,
                        remainingValue = remainingValue,
                        parentValue = parentValue,
                        childValue = childValue,
                        dependentValue = dependentValue,
                    )

                    if (errorMessage.isNotBlank()) {
                        _localParameterValueMap.value =
                            _localParameterValueMap.value.toMutableMap().apply {
                                put(
                                    event.elementId,
                                    InputWrapper(
                                        value = localParameterValueMap.value[event.elementId]?.value
                                            ?: "",
                                        errorMessage = errorMessage,
                                        isFocus = false
                                    )
                                )
                            }
                    }
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
                                dependentOperatorMap = _dependentOperatorMap.value,
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
                                                value = if (event.value.isNotBlank() && (localParameterValueMap.value[event.elementId]?.value
                                                        ?: "").length > event.value.length
                                                ) {
                                                    localParameterValueMap.value[event.elementId]?.value
                                                        ?: ""
                                                } else "",
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

                            if (_localParameterMap.value[event.elementId]?.autoCalculate?.isNotEmpty() == true) {
                                val sumMap = calculateSumForElement(
                                    elementId = event.elementId,
                                    operatorMap = _operatorMap.value,
                                    operatorValueMap = _operatorValueMap.value,
                                    localParameterValueMap = localParameterValueMap.value
                                )

                                _operatorMap.value.forEach { (key, value) ->
                                    if (key.contains(event.elementId)) {
                                        value.forEach { dependentId ->
                                            val sum =
                                                sumMap.entries.find { (idList, _) -> dependentId in idList }?.value

                                            _localParameterValueMap.value =
                                                _localParameterValueMap.value.toMutableMap().apply {
                                                    put(
                                                        dependentId,
                                                        InputWrapper(
                                                            value = sum?.toString() ?: "",
                                                            errorMessage = ""
                                                        )
                                                    )
                                                }
                                        }

                                    }
                                }
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
                try {
                    _showProgressIndicator.value = true
                    _isSubmitButtonEnabled.value = false

                    _localParameterMap.value.forEach { (elementId, childrenX) ->
                        if (_localVisibilityStatusMap.value[elementId] == true) {
                            if (childrenX.inputType == "number") {
                                val (remainingValue, parentValue, childValue, expression, dependentValue) = calculateRemainingValuesForValueChange(
                                    elementId = elementId,
                                    newValue = _localParameterValueMap.value[elementId]?.value
                                        ?: "",
                                    dependentOperatorMap = _dependentOperatorMap.value,
                                    dependentValueMap = _dependentValueMap.value,
                                    localParameterValueMap = _localParameterValueMap.value,
                                    isSkipEqualConditions = true
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
                                                elementId,
                                                InputWrapper(
                                                    value = localParameterValueMap.value[elementId]?.value
                                                        ?: "",
                                                    errorMessage = errorMessage,
                                                    isFocus = true
                                                )
                                            )
                                        }
                                }
                            }
                        }
                    }

                    val isFieldEmpty = _localParameterValueMap.value.run {
                        isEmpty() || any { (key, inputWrapper) ->
                            val parameter = _localParameterMap.value[key]
                            val isVisible = _localVisibilityStatusMap.value[key] == true
                            val isRequired = parameter?.isRequired == "true"
                            parameter?.elementType != "ElementLabel" && isVisible && isRequired && inputWrapper.value.isEmpty()
                        }
                    }

                    if (_action != "filter")
                        checkCombinationOfPValue()

                    val firstError =
                        _localParameterValueMap.value.values.firstOrNull { it.errorMessage.isNotBlank() }

                    when {
                        isFieldEmpty -> SendUiEvent.send(
                            viewModelScope,
                            _uiEvent,
                            "Field should not be empty"
                        )

                        firstError != null -> SendUiEvent.send(
                            viewModelScope,
                            _uiEvent,
                            firstError.errorMessage
                        )

                        else -> event.onClick(
                            _localParameterValueMap.value,
                            _localVisibilityStatusMap.value
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    SendUiEvent.send(viewModelScope, _uiEvent, "An error occurred. Try again.")
                } finally {
                    _showProgressIndicator.value = false
                    _isSubmitButtonEnabled.value = true
                }
            }

            is FormScreenEvent.OnPhotoTaken -> {
                viewModelScope.launch {
                    val currentImages =
                        _imageList.value[event.elementId]?.toMutableList() ?: mutableListOf()
                    currentImages.add(event.image)

                    updateImageList(event.elementId, currentImages)

                    val resourcePath = try {
                        saveImageApi(image = event.image)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        handleImageUploadError(event.elementId, currentImages)
                        return@launch
                    }

                    updateResourcePath(event.elementId, resourcePath)
                    updateLocalParameterValue(event.elementId)
                }
            }

            is FormScreenEvent.OnPhotoDeleteButtonClicked -> {
                val updatedImageList = _imageList.value.toMutableMap().apply {
                    this[event.elementId]?.let { imageList ->
                        val updatedList = imageList.toMutableList()
                        updatedList.removeAt(event.index)
                        this[event.elementId] = updatedList
                    }
                }
                _imageList.value = updatedImageList

                _localParameterValueMap.value =
                    _localParameterValueMap.value.toMutableMap().apply {
                        this[event.elementId] = InputWrapper(
                            value = updatedImageList[event.elementId]?.joinToString(",") ?: "",
                            errorMessage = ""
                        )
                    }
            }

            is FormScreenEvent.OnSearchValueChanged -> {
                elementId = event.elementId
                _searchText.value = event.searchText
            }

            is FormScreenEvent.OnImageViewButtonClicked -> {
                if (event.image.preSignedUrl.isEmpty() && event.image.byteImage == null) {
                    viewModelScope.launch {
                        try {
                            val url = getImagePreSignedUrl(urls = event.image.resourcePath)

                            _imageList.value = _imageList.value.mapValues { (key, imageList) ->
                                if (key == event.elementId) {
                                    imageList.map { imageModel ->
                                        if (imageModel.resourcePath == event.image.resourcePath) {
                                            imageModel.copy(
                                                isLoading = false,
                                                preSignedUrl = url
                                            )
                                        } else
                                            imageModel
                                    }
                                } else imageList
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                _isViewCamera.value = true
            }

            is FormScreenEvent.OnCheckboxValueChanged -> {

                if (event.option.prompts != null && event.option.isChecked) {
                    SendUiEvent.send(
                        viewModelScope = viewModelScope,
                        _uiEvent = _uiEvent,
                        event = event.option.prompts
                    )
                }
                val tempList = _localParameterValueMap.value[event.elementId]?.value
                    ?.takeIf { it.isNotEmpty() }
                    ?.split(",")
                    ?.toMutableList()
                    ?: mutableListOf()

                val optionValue = event.option.pValue.toString()

                if (!tempList.remove(optionValue)) {
                    tempList.add(optionValue)
                }

                _localParameterValueMap.value =
                    _localParameterValueMap.value.toMutableMap().apply {
                        put(
                            event.elementId,
                            InputWrapper(
                                value = tempList.joinToString(","),
                                errorMessage = ""
                            )
                        )
                    }

                val selectedOptionIds = tempList.mapNotNull { pValue ->
                    _localParameterMap.value[event.elementId]?.elementData?.options
                        ?.firstOrNull { it.pValue == pValue.toIntOrNull() }
                        ?.optionId
                }.ifEmpty { listOf(0) }

                val visibilityMap = hideAndShowValidation(
                    elementId = event.elementId,
                    parameterMap = _localParameterMap.value,
                    parameterValueMap = _localParameterValueMap.value,
                    selectedOptionIds = selectedOptionIds
                )

                visibilityMap.filterValues { !it }.forEach { (key, _) ->
                    _localParameterValueMap.value =
                        _localParameterValueMap.value.toMutableMap().apply {
                            this[key] = InputWrapper(
                                value = _localParameterMap.value[key]?.elementValue ?: "",
                                errorMessage = ""
                            )
                        }
                }

                _localVisibilityStatusMap.value =
                    _localVisibilityStatusMap.value.toMutableMap().apply {
                        putAll(visibilityMap)
                    }
            }

            FormScreenEvent.OnImagePreviewDialogDismiss -> {
                _isViewCamera.value = false
            }

            is FormScreenEvent.OnDateValueChanged -> {
                _localParameterValueMap.value = _localParameterValueMap.value
                    .toMutableMap()
                    .apply {
                        put(
                            event.elementId,
                            InputWrapper(value = event.value, errorMessage = "")
                        )
                    }
            }

            is FormScreenEvent.OnTimeValueChanged -> {
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

    fun initData(
        parameterValueMap: Map<Int, InputWrapper>,
        parameterMap: Map<Int, ChildrenX>,
        visibilityMap: Map<Int, Boolean>,
        enabledStatusMap: Map<Int, Boolean>,
        combinationPValueList: Map<String, List<String>>,
        activity: String,
        form: String,
        action: String,
        httpClient: HttpClient
    ) {
        _localParameterValueMap.value = parameterValueMap
        _localParameterMap.value = parameterMap
        _localVisibilityStatusMap.value = visibilityMap
        _localEnabledStatusMap.value = enabledStatusMap
        _combinationPValueList.value = combinationPValueList
        _action = action
        _activity = activity
        _form = form
        _isSubmitButtonEnabled.value = true
        _imageList.value = _imageList.value.toMutableMap().apply {
            clear()
        }
        _isViewCamera.value = false
        this.httpClient = httpClient
        val tempDependentValueMap = _dependentValueMap.value.toMutableMap()
        val tempDependentOperatorMap = _dependentOperatorMap.value.toMutableMap()
        val tempOperatorMap = _operatorMap.value.toMutableMap()
        val tempOperatorValueMap = _operatorValueMap.value.toMutableMap()

        try {
            _localParameterMap.value.forEach { (_, element) ->

                if (element.elementType == "ElementImageUpload" && !_localParameterValueMap.value[element.elementId]?.value.isNullOrEmpty()) {
                    _imageList.value = _imageList.value.toMutableMap().apply {
                        this[element.elementId] =
                            _localParameterValueMap.value[element.elementId]?.value?.split(",")
                                ?.map { imagePath ->
                                    ImageModel(
                                        byteImage = null,
                                        resourcePath = imagePath,
                                        isLoading = false
                                    )
                                } ?: emptyList()
                    }
                }
                val tempPValue = when (_action) {
                    "filter" -> {
                        _localParameterValueMap.value[element.elementId]?.value
                            ?.let {
                                if (it.isNotEmpty() && it.contains("{"))
                                    Json.decodeFromString<DropdownOption>(it).pValue
                                else
                                    _localParameterValueMap.value[element.elementId]?.value?.toIntOrNull()
                                        ?: 0
                            }
                            ?: 0
                    }

                    else -> _localParameterValueMap.value[element.elementId]?.value?.toIntOrNull()
                        ?: 0
                }

                if (!element.elementOptionDependent.isNullOrEmpty()) {
                    val tempVisibilityMap = hideAndShowValidation(
                        elementId = element.elementId,
                        parameterMap = _localParameterMap.value,
                        parameterValueMap = _localParameterValueMap.value,
                        selectedOptionIds = listOf(
                            (element.elementData.options
                                .firstOrNull { it.pValue == tempPValue }
                                ?.optionId) ?: 0)
                    )

                    tempVisibilityMap.filterValues { !it }.forEach { (key, _) ->
                        _localParameterValueMap.value =
                            _localParameterValueMap.value.toMutableMap().apply {
                                this[key] = InputWrapper(
                                    value = _localParameterMap.value[key]?.elementValue ?: "",
                                    errorMessage = ""
                                )
                            }
                    }

                    _localVisibilityStatusMap.value =
                        _localVisibilityStatusMap.value.toMutableMap().apply {
                            putAll(tempVisibilityMap)
                        }
                }

                element.validation.forEach { validation ->
                    validation.values.forEach { value ->
                        val operatorKeys = value.dependentOperator.split(",").map(String::toInt)
                        val resultValues = value.dependentResult.split(",").map(String::toInt)

                        tempDependentOperatorMap[operatorKeys] = resultValues

                        operatorKeys.forEach { key ->
                            tempDependentValueMap[key] = DependentValueCustomText(
                                isShow = false,
                                expression = value.dependant,
                                value = _localParameterMap.value[key]?.elementValue ?: ""
                            )
                        }
                    }
                }

                element.autoCalculate.forEach {
                    val operatorKeys = it.operator.split(",").map(String::toInt)
                    val resultValues = it.operatorResult.split(",").map(String::toInt)

                    tempOperatorMap[operatorKeys] = resultValues

                    operatorKeys.forEach { key ->
                        tempOperatorValueMap[key] = DependentValueCustomText(
                            isShow = false,
                            expression = it.operation,
                            value = _localParameterMap.value[key]?.elementValue ?: ""
                        )
                    }
                }

                if (!element.elementData.dataUrl.isNullOrBlank()) {
                    viewModelScope.launch {
                        try {
                            remoteApi(
                                url = element.elementData.dataUrl,
                                filterMap = emptyMap(),
                                elementId = element.elementId
                            )

                            element.elementData.dependentApi?.forEach { api ->
                                val filterMap = api.parameter.mapValues { (_, value) ->
                                    val paramValue =
                                        _localParameterValueMap.value[value]?.value.orEmpty()
                                    if (_action == "filter") {
                                        Json.decodeFromString<DropdownOption>(paramValue).pValue.toString()
                                    } else {
                                        paramValue.ifEmpty { "0" }
                                    }
                                }
                                val finalFilterMap =
                                    if (filterMap.values.contains("0")) emptyMap() else filterMap
                                remoteApi(
                                    url = api.url,
                                    filterMap = finalFilterMap,
                                    elementId = api.dependent
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            SendUiEvent.send(
                                viewModelScope = viewModelScope,
                                _uiEvent = _uiEvent,
                                event = e.message
                                    ?: "An unknown error occurred. Please try again."
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        _dependentOperatorMap.value = tempDependentOperatorMap
        _dependentValueMap.value = tempDependentValueMap
        _operatorMap.value = tempOperatorMap
        _operatorValueMap.value = tempOperatorValueMap
    }

    private fun checkCombinationOfPValue() {
        _combinationPValueList.value.let { map ->
            val elementIds = map.keys.firstOrNull() ?: return@let
            val validCombinations = map[elementIds]?.map { combination ->
                combination.split(", ").sorted().joinToString(", ")
            } ?: emptyList()

            val relevantKeys = elementIds.split(",").map { it.trim().toInt() }.toSet()

            val selectedValues = _localParameterValueMap.value
                .filterKeys { it in relevantKeys }
                .values
                .mapNotNull { it.value.takeIf { it1 -> it1.isNotBlank() } }
                .sorted()
                .joinToString(", ")

            if (selectedValues in validCombinations) {
                val conflictingParams = relevantKeys.mapNotNull { key ->
                    val param = _localParameterMap.value[key]
                    val parameterName = param?.elementLabel?.en.orEmpty()
                    val optionName = param?.elementData?.options
                        ?.firstOrNull { it.pValue.toString() == _localParameterValueMap.value[key]?.value }
                        ?.optionName?.en.orEmpty()

                    parameterName.takeIf { it.isNotBlank() }
                        ?.let { "$it ($optionName)" }
                }

                if (conflictingParams.isNotEmpty()) {
                    val joinedParams = conflictingParams.joinToString(", ")
                    val joinedNames =
                        conflictingParams.joinToString(", ") { it.substringBefore(" (") }
                    val errorMessage =
                        "The selected values for $joinedParams already exist. Please choose different values for $joinedNames."

                    SendUiEvent.send(viewModelScope, _uiEvent, errorMessage)

                    _localParameterValueMap.value =
                        _localParameterValueMap.value.mapValues { (key, wrapper) ->
                            if (key in relevantKeys) wrapper.copy(errorMessage = errorMessage) else wrapper
                        }
                }
            } else {
                _localParameterValueMap.value =
                    _localParameterValueMap.value.mapValues { (key, wrapper) ->
                        if (key in relevantKeys) wrapper.copy(errorMessage = "") else wrapper
                    }
            }
        }
    }

    private fun updateImageList(elementId: Int, images: List<ImageModel>) {
        _imageList.value = _imageList.value.toMutableMap().apply {
            this[elementId] = images
        }
    }

    private fun handleImageUploadError(elementId: Int, currentImages: MutableList<ImageModel>) {
        val updatedImages = if (currentImages.isNotEmpty()) {
            currentImages.toMutableList().apply { removeAt(lastIndex) }
        } else {
            currentImages.toMutableList()
        }

        updateImageList(elementId, updatedImages)

        SendUiEvent.send(
            viewModelScope = viewModelScope,
            _uiEvent = _uiEvent,
            event = "An error occurred while uploading your image.\nPlease check your internet connection and try again."
        )
    }

    private fun updateResourcePath(elementId: Int, resourcePath: String) {
        _imageList.value = _imageList.value.mapValues { (key, imageList) ->
            if (key == elementId) {
                imageList.map { imageModel ->
                    if (imageModel.resourcePath.isEmpty()) {
                        imageModel.copy(isLoading = false, resourcePath = resourcePath)
                    } else {
                        imageModel
                    }
                }
            } else {
                imageList
            }
        }
    }

    private fun updateLocalParameterValue(elementId: Int) {
        val resourcePathList =
            _imageList.value[elementId]?.map { it.resourcePath } ?: emptyList()
        _localParameterValueMap.value = _localParameterValueMap.value.toMutableMap().apply {
            this[elementId] = InputWrapper(
                value = resourcePathList.joinToString(","),
                errorMessage = ""
            )
        }
    }

    private suspend fun remoteApi(
        url: String,
        filterMap: Map<String, String> = emptyMap(),
        elementId: Int
    ) {
        val result = try {
            httpClient.get(url) {
                url {
                    parameters.append("activity", _activity)
                    parameters.append("form", _form)
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
            _localParameterMap.value = _localParameterMap.value.toMutableMap().apply {
                this[elementId]?.elementData?.options =
                    result.body<FilterResponseDto>().data?.map { it.toOption() } ?: emptyList()
            }
            _searchText.value = "-"
            delay(1000)
            _searchText.value = ""
        } catch (e: Exception) {
            throw Exception("Failed to parse the response. Please try again later.", e)
        }
    }

    private suspend fun saveImageApi(image: ImageModel): String {
        val saveImageResponseDto: SaveImageResponseDto?

        val result = try {
            httpClient.post("https://enable.prathamapps.com/api/resource/upload/") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("activity", _activity)
                            append("form", _form)
                            append(
                                key = "resource",
                                value = image.byteImage
                                    ?: throw IllegalArgumentException("Image data cannot be null or empty"),
                                headers = Headers.build {
                                    append(
                                        HttpHeaders.ContentType,
                                        ContentType.Image.PNG.toString()
                                    )
                                    append(
                                        HttpHeaders.ContentDisposition,
                                        "filename=\"image.png\""
                                    )
                                })
                        }
                    )
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
            throw Exception("Network error occurred. Please check your internet connection and try again.")
        }

        when (result.status.value) {
            in 200..299 -> Unit
            500 -> throw Exception("Internal server error. Please try again later.")
            in 400..499 -> {
                throw Exception("Client error: ${result.body<SaveImageResponseDto>().errors?.detail}")
            }

            else -> throw Exception("An unknown error occurred. Please try again.")
        }

        try {
            saveImageResponseDto = result.body<SaveImageResponseDto>()
        } catch (e: Exception) {
            throw Exception("Failed to parse the response. Please try again later.", e)
        }
        return saveImageResponseDto.resourcePaths
    }

    private suspend fun getImagePreSignedUrl(urls: String): String {
        val imageUrl: String

        val result = try {
            httpClient.post("https://enable.prathamapps.com/api/resource/presignedurl/") {
                setBody(
                    FormDataContent(
                        parameters {
                            append("file_paths", urls)
                        }
                    )
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
            throw Exception("Network error occurred. Please check your internet connection and try again.")
        }

        when (result.status.value) {
            in 200..299 -> Unit
            500 -> throw Exception("Internal server error. Please try again later.")
            in 400..499 -> {
                throw Exception("Client error: ${result.body<ImagePreSignedUrlResponseDto>().message}")
            }

            else -> throw Exception("An unknown error occurred. Please try again.")
        }

        try {
            imageUrl = result.body<ImagePreSignedUrlResponseDto>().urls?.joinToString(",") ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Failed to parse the response. Please try again later.")
        }
        return imageUrl
    }
}