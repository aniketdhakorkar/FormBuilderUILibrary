import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import io.ktor.client.HttpClient
import org.jetbrains.compose.ui.tooling.preview.Preview
import util.InputWrapper
import model.parameters.ChildrenX

@Composable
@Preview
fun App(
    parameterValueMap: Map<Int, InputWrapper>,
    parameterMap: Map<Int, ChildrenX>,
    visibilityMap: Map<Int, Boolean>,
    enabledStatusMap: Map<Int, Boolean>,
    httpClient: HttpClient
) {
    MaterialTheme {
        FormScreen(
            parameterValueMap = parameterValueMap,
            parameterMap = parameterMap,
            visibilityMap = visibilityMap,
            enabledStatusMap = enabledStatusMap,
            onClick = { _, _ -> },
            action = "add",
            activity = "10",
            form = "3",
            httpClient = httpClient
        )
    }
}