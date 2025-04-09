package com.pratham.formbuilderui

import App
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import model.parameters.ChildrenX
import model.parameters.Parameters
import util.InputWrapper

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "FormBuilderUi",
    ) {
        /*val parametersFlow = MutableStateFlow<List<Parameters>>(emptyList())
        val parameterValueMapFlow = MutableStateFlow<Map<Int, InputWrapper>>(emptyMap())
        val parameterMapFlow = MutableStateFlow<Map<Int, ChildrenX>>(emptyMap())
        val visibilityMapFlow = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
        val enabledMapFlow = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
        val combinationValueMapFlow = MutableStateFlow<Map<String, List<String>>>(emptyMap())

        val httpClient = createAuthenticatedHttpClient()

        val parameterValueMap by parameterValueMapFlow.collectAsState()
        val parameterMap by parameterMapFlow.collectAsState()
        val visibilityMap by visibilityMapFlow.collectAsState()
        val enabledMap by enabledMapFlow.collectAsState()
        val combinationValueMap by combinationValueMapFlow.collectAsState()

        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            fetchParameters().onEach { parameters ->
                parametersFlow.value = parameters

                parameters
                    .flatMap { it.children }
                    .flatMap { it.children }
                    .forEach { child ->
                        if (child.elementType != "ElementLabel") {
                            parameterValueMapFlow.update { it + (child.elementId to InputWrapper(child.elementValue ?: "")) }
                        }

                        parameterMapFlow.update { it + (child.elementId to child) }
                        visibilityMapFlow.update { it + (child.elementId to !child.isDependent) }
                        enabledMapFlow.update { it + (child.elementId to true) }
                    }

                combinationValueMapFlow.value = mapOf("1" to List(4) { "1240" })
            }.launchIn(scope)
        }

        App(
            parameterValueMap = parameterValueMap,
            parameterMap = parameterMap,
            visibilityMap = visibilityMap,
            enabledStatusMap = enabledMap,
            httpClient = httpClient,
            combinationPValueList = combinationValueMap
        )*/
    }
}

/*fun createAuthenticatedHttpClient(): HttpClient = HttpClient {
    install(HttpTimeout) {
        socketTimeoutMillis = 60_000
        requestTimeoutMillis = 60_000
    }
    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                co.touchlab.kermit.Logger.d("KtorClient") { message }
            }
        }
        level = LogLevel.ALL
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
                BearerTokens(
                    accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                    refreshToken = null
                )
            }
        }
    }
}

fun fetchParameters(): Flow<List<Parameters>> = flow {
    try {
        val client = HttpClient {
            install(HttpTimeout) {
                socketTimeoutMillis = 60_000
                requestTimeoutMillis = 60_000
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        co.touchlab.kermit.Logger.d("KtorClient") { message }
                    }
                }
                level = LogLevel.ALL
            }
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }

        val response = client.post("https://testdataentry.prathamapps.com/parameters/get/") {
            setBody(
                FormDataContent(
                    parameters {
                        append("app_type", "1")
                        append("project_id", "6")
                        append("program_id", "10")
                        append("category_id", "3")
                    }
                )
            )
        }

        emit(response.body())
    } catch (e: Exception) {
        e.printStackTrace()
    }
}*/
