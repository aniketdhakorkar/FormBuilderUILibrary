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
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.filter.FilterResponseDto
import util.DependentValueCustomText
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
import validation.checkMobileNoValidation
import validation.expressionValidation
import validation.hideAndShowValidation
import validation.validateInputInRange

@OptIn(FlowPreview::class)
class FormScreenViewModel : ViewModel() {

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
    private val dependentOperatorMap = _dependentOperatorMap.asStateFlow()
    private val _dependentValueMap =
        MutableStateFlow<Map<Int, DependentValueCustomText>>(emptyMap())
    val dependentValueMap = _dependentValueMap.asStateFlow()
    private var _token: String = ""
    private val _showProgressIndicator = MutableStateFlow(false)
    val showProgressIndicator = _showProgressIndicator.asStateFlow()
    private val _imageList = MutableStateFlow<Map<Int, List<ImageModel>>>(emptyMap())
    val imageList = _imageList.asStateFlow()
    private var _activity = ""
    private var _form = ""
    private var _action = ""
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
                        InputWrapper(
                            value = if (_action == "filter")
                                Json.encodeToString<DropdownOption>(event.option)
                            else event.option.pValue.toString(),
                            errorMessage = ""
                        )
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
                                        val pValue = if (_action == "filter")
                                            Json.decodeFromString<DropdownOption>(
                                                _localParameterValueMap.value[value]?.value ?: ""
                                            ).pValue.toString()
                                        else
                                            _localParameterValueMap.value[value]?.value ?: "0"
                                        put(
                                            key = key,
                                            value = pValue
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

            is FormScreenEvent.OnPhotoTaken -> {
                viewModelScope.launch {
                    val currentImages =
                        _imageList.value[event.elementId]?.toMutableList() ?: mutableListOf()
                    currentImages.add(event.image)

                    _imageList.value = _imageList.value.toMutableMap().apply {
                        this[event.elementId] = currentImages
                    }

                    val resourcePath: String?

                    try {
                        resourcePath = saveImageApi(image = event.image, token = _token)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _imageList.value = _imageList.value.toMutableMap().apply {
                            this[event.elementId] = currentImages.apply {
                                if (isNotEmpty()) removeLast()
                            }
                        }

                        SendUiEvent.send(
                            viewModelScope = viewModelScope,
                            _uiEvent = _uiEvent,
                            event = "An error occurred while uploading your image.\nPlease check your internet connection and try again."
                        )
                        return@launch
                    }

                    if (resourcePath != null) {
                        _imageList.value = _imageList.value.mapValues { (key, imageList) ->
                            if (key == event.elementId) {
                                imageList.map { imageModel ->
                                    imageModel.copy(
                                        isLoading = false,
                                        resourcePath = resourcePath
                                    )
                                }
                            } else imageList
                        }

                        _localParameterValueMap.value =
                            _localParameterValueMap.value.toMutableMap().apply {
                                this[event.elementId] = InputWrapper(
                                    value = _imageList.value[event.elementId]?.joinToString(",")
                                        ?: "",
                                    errorMessage = ""
                                )
                            }
                    }
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

                _localParameterValueMap.value = _localParameterValueMap.value.toMutableMap().apply {
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
                viewModelScope.launch {
                    try {
                        val url =
                            getImagePreSignedUrl(urls = event.image.resourcePath, token = _token)

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
        }
    }

    fun initData(
        parameterValueMap: Map<Int, InputWrapper>,
        parameterMap: Map<Int, ChildrenX>,
        visibilityMap: Map<Int, Boolean>,
        enabledStatusMap: Map<Int, Boolean>,
        activity: String,
        form: String,
        token: String,
        action: String
    ) {
        _localParameterValueMap.value = parameterValueMap
        _localParameterMap.value = parameterMap
        _localVisibilityStatusMap.value = visibilityMap
        _localEnabledStatusMap.value = enabledStatusMap
        _token = token
        _action = action
        _activity = activity
        _form = form
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
            _localParameterMap.value = _localParameterMap.value.toMutableMap().apply {
                this[elementId]?.elementData?.options =
                    result.body<FilterResponseDto>().data?.map { it.toOption() } ?: emptyList()
            }
            _searchText.value = "-"
            delay(1000)
            _searchText.value = ""
        } catch (e: Exception) {
            throw Exception("Failed to parse the response. Please try again later.", e)
        } finally {
            httpClient.close()
        }
    }

    private suspend fun saveImageApi(
        image: ImageModel,
        token: String
    ): String? {

        val httpClient = provideHttpClient(token = token)
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
                                value = image.byteImage ?: byteArrayOf(),
                                headers = Headers.build {
                                    append(HttpHeaders.ContentType, "image/png")
                                    append(
                                        HttpHeaders.ContentDisposition,
                                        "filename=\"image.png\""
                                    )
                                })
                        },
                        boundary = "WebAppBoundary"
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
        } finally {
            httpClient.close()
        }
        return saveImageResponseDto?.resourcePaths
    }

    private suspend fun getImagePreSignedUrl(urls: String, token: String): String {
        val httpClient = provideHttpClient(token = token)
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
        } finally {
            httpClient.close()
        }
        return imageUrl
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