package ui.helper

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
fun bringIntoView(
    coroutineScope: CoroutineScope,
    bringIntoViewRequester: BringIntoViewRequester
) {
    coroutineScope.launch {
        bringIntoViewRequester.bringIntoView()
    }
}