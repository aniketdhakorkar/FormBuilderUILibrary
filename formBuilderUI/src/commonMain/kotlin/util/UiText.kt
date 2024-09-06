package util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

data class InputWrapper(
    var value: String,
    var errorMessage: String = "",
    var isFocus: Boolean = false
)

object SendUiEvent {

    fun send(
        viewModelScope: CoroutineScope,
        _uiEvent: Channel<String>,
        event: String
    ) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}