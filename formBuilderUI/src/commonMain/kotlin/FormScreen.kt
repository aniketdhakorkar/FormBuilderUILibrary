import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.crossfade
import coil3.util.DebugLogger
import kotlinx.coroutines.CoroutineScope
import ui.component.CreateDropdown
import ui.component.CreateLabel
import ui.component.CreateTextField
import ui.component.SubmitButton
import ui.theme.AppTheme
import util.DependentValueCustomText
import util.InputWrapper
import model.parameters.ChildrenX
import model.parameters.toDropdown
import okio.FileSystem
import ui.component.CreateCamera
import util.SnackbarController

@Composable
fun FormScreen(
    parameterValueMap: Map<Int, InputWrapper>,
    parameterMap: Map<Int, ChildrenX>,
    visibilityMap: Map<Int, Boolean>,
    onClick: (Map<Int, InputWrapper>) -> Unit,
    enabledStatusMap: Map<Int, Boolean>,
    isSubmitButtonVisible: Boolean,
    token: String
) {

    setSingletonImageLoaderFactory { context ->
        getAsyncImageLoader(context)
    }
    val scope = rememberCoroutineScope()
    val snackbarController = SnackbarController(scope = scope)
    val viewModel = viewModel { FormScreenViewModel() }
    val localParameterValueMap by viewModel.localParameterValueMap.collectAsState()
    val localParameterMap by viewModel.localParameterMap.collectAsState()
    val localVisibilityStatusMap by viewModel.localVisibilityStatusMap.collectAsState()
    val localEnabledStatusMap by viewModel.localEnabledStatusMap.collectAsState()
    val dependentValueMap by viewModel.dependentValueMap.collectAsState()
    val onlineDropdownOptionMap by viewModel.onlineDropdownOptionMap.collectAsState()
    val focusManager = LocalFocusManager.current
    val showProgressIndicator by viewModel.showProgressIndicator.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(parameterMap) {
        if (parameterMap.isNotEmpty())
            viewModel.initData(
                parameterValueMap = parameterValueMap,
                parameterMap = parameterMap,
                visibilityMap = visibilityMap,
                enabledStatusMap = enabledStatusMap,
                token = token
            )
    }

    LaunchedEffect(true) {
        viewModel.uiEvent.collect { event ->
            snackbarController.showSnackbar(snackbarHostState = snackbarHostState, message = event)
        }
    }


    AppTheme {
        Scaffold(snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 64.dp)
                    .background(color = MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(localParameterMap.toList()) { parameter ->

                        val parameterValue =
                            localParameterValueMap[parameter.first] ?: InputWrapper("")
                        val isVisible = localVisibilityStatusMap[parameter.first] ?: true
                        val isEnabled = localEnabledStatusMap[parameter.first] ?: true
                        val question = parameter.second.elementLabel.en ?: ""
                        val description = parameter.second.elementTooltip.en ?: ""
                        val isMandatory = parameter.second.isRequired
                        val style = parameter.second.style
                        val isOnlineDropdownOption =
                            onlineDropdownOptionMap[parameter.first]?.isNotEmpty() ?: false

                        when (parameter.second.elementType) {
                            "ElementLabel" -> CreateLabel(
                                question = question,
                                style = style,
                                isVisible = isVisible
                            )

                            "ElementText" -> {

                                CreateTextField(
                                    question = question,
                                    description = description,
                                    textFieldValue = parameterValue,
                                    onValueChange = { value ->
                                        viewModel.onEvent(
                                            FormScreenEvent.OnTextFieldValueChanged(
                                                elementId = parameter.second.elementId,
                                                value = value
                                            )
                                        )
                                    },
                                    isMandatory = parameter.second.isRequired,
                                    style = parameter.second.style,
                                    inputType = parameter.second.inputType,
                                    isVisible = isVisible,
                                    isEnable = isEnabled,
                                    focusManager = focusManager,
                                    onFocusChange = {
                                        if (parameter.second.inputType == "number") {
                                            viewModel.onEvent(
                                                FormScreenEvent.OnTextFieldFocusChanged(
                                                    elementId = parameter.second.elementId,
                                                    isFocused = it
                                                )
                                            )
                                        }
                                    },
                                    dependentValueCustomText = dependentValueMap[parameter.second.elementId]
                                        ?: DependentValueCustomText()
                                )
                            }

                            "ElementDropDown" -> {

                                CreateDropdown(
                                    question = question,
                                    description = description,
                                    isMandatory = isMandatory,
                                    style = style,
                                    optionList = if (isOnlineDropdownOption)
                                        onlineDropdownOptionMap[parameter.first] ?: emptyList()
                                    else
                                        parameter.second.elementData.options.map { it.toDropdown() },
                                    dropdownValue = parameterValue,
                                    onValueChanged = { option ->
                                        viewModel.onEvent(
                                            FormScreenEvent.OnDropdownValueChanged(
                                                elementId = parameter.second.elementId,
                                                option = option
                                            )
                                        )
                                    },
                                    isVisible = isVisible,
                                    isEnabled = isEnabled,
                                    focusManager = focusManager
                                )
                            }

                            "ElementImageUpload" -> {
                                CreateCamera(
                                    question = question,
                                    description = description,
                                    isMandatory = isMandatory,
                                    style = style,
                                    imageList = parameterValue.value.split("&").toList(),
                                    onCameraButtonClicked = {
                                        viewModel.onEvent(
                                            FormScreenEvent.OnCameraButtonClicked(
                                                elementId = parameter.second.elementId,
                                                data = it
                                            )
                                        )
                                    },
                                    onPhotoDeleteButtonClicked = {
                                        viewModel.onEvent(
                                            FormScreenEvent.OnPhotoDeleteButtonClicked(
                                                elementId = parameter.second.elementId,
                                                index = it
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                if (isSubmitButtonVisible)
                    SubmitButton(showProgressIndicator = showProgressIndicator, onClick = {
                        viewModel.onEvent(FormScreenEvent.OnSubmitButtonClicked(onClick = onClick))
                    })
            }
        }
    }
}

fun getAsyncImageLoader(context: PlatformContext) =
    ImageLoader.Builder(context).memoryCachePolicy(CachePolicy.ENABLED).memoryCache {
        MemoryCache.Builder().maxSizePercent(context, 0.3).strongReferencesEnabled(true).build()
    }.diskCachePolicy(CachePolicy.ENABLED).networkCachePolicy(CachePolicy.ENABLED).diskCache {
        newDiskCache()
    }.crossfade(true).logger(DebugLogger()).build()

fun newDiskCache(): DiskCache {
    return DiskCache.Builder().directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "image_cache")
        .maxSizeBytes(1024L * 1024 * 1024) // 512MB
        .build()
}