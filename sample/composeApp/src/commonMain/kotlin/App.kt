import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import util.InputWrapper
import model.parameters.ChildrenX
import model.parameters.Parameters

@Composable
@Preview
fun App(
    parameterValueMap: MutableMap<Int, InputWrapper>,
    parameterMap: MutableMap<Int, ChildrenX>,
    visibilityMap: MutableMap<Int, Boolean>,
) {
    MaterialTheme {
        FormScreen(
            parameterValueMap = parameterValueMap,
            parameterMap = parameterMap,
            visibilityMap = visibilityMap,
            onClick = {}
        )
    }
}