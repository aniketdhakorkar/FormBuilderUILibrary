package com.pratham.formbuilderui

import App
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.Json
import util.InputWrapper
import model.parameters.ChildrenX
import model.parameters.Parameters

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val _parameters = MutableStateFlow<List<Parameters>>(emptyList())
        val _parameterValueMap = MutableStateFlow<Map<Int, InputWrapper>>(mutableMapOf())
        val _parameterMap = MutableStateFlow<Map<Int, ChildrenX>>(mutableMapOf())
        val _visibilityMap = MutableStateFlow<Map<Int, Boolean>>(mutableMapOf())
        val _enabledMap = MutableStateFlow<Map<Int, Boolean>>(mutableMapOf())

        val httpClient = provideHttpClient()

        val paramFlow = flow<List<Parameters>> {
            try {
                val httpClientWithoutAuth = HttpClient {
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
                }

                val result =
                    httpClientWithoutAuth.post("https://testdataentry.prathamapps.com/parameters/get/") {
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
                emit(result.body())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        setContent {

            val parameterValueMap by _parameterValueMap.asStateFlow().collectAsState()
            val parameterMap by _parameterMap.asStateFlow().collectAsState()
            val visibilityMap by _visibilityMap.asStateFlow().collectAsState()
            val enabledMap by _enabledMap.asStateFlow().collectAsState()
            val scope = rememberCoroutineScope()

            LaunchedEffect(key1 = true) {
                paramFlow.onEach {
                    _parameters.value = it
                    it.flatMap { it.children }
                        .flatMap { it.children }
                        .forEach { childX ->
                            _parameterValueMap.value =
                                _parameterValueMap.value.toMutableMap().apply {
                                    if (childX.elementType != "ElementLabel" && childX.elementType != "ElementHidden")
                                        put(
                                            childX.elementId,
                                            InputWrapper(value = "")
                                        )
                                }

                            _parameterMap.value = _parameterMap.value.toMutableMap().apply {
                                put(
                                    childX.elementId,
                                    childX
                                )
                            }

                            _visibilityMap.value = _visibilityMap.value.toMutableMap().apply {
                                put(childX.elementId, !childX.isDependent)
                            }

                            _enabledMap.value = _enabledMap.value.toMutableMap().apply {
                                put(childX.elementId, true)
                            }
                        }
                }.launchIn(scope = scope)
            }

            App(
                parameterValueMap = parameterValueMap,
                parameterMap = parameterMap,
                visibilityMap = visibilityMap,
                enabledStatusMap = enabledMap,
                httpClient = httpClient
            )
        }
    }

    private fun provideHttpClient(): HttpClient = HttpClient {
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
                    BearerTokens(
                        accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoiYWNjZXNzIiwiZXhwIjoxNzM3NzAxOTczLCJpYXQiOjE3Mzc2OTgzNzMsImp0aSI6IjgzMTFlYmY1YzkwNTQ2ZWY5ZjFlMWNhZTM0YThmNWYwIiwidXNlcl9pZCI6MTAzfQ.NNQzUQfZgMEQ6gqVqDBxzpgKsehbrnIKD0Fno6hF-OA",
                        refreshToken = null
                    )
                }
            }
        }
    }
}