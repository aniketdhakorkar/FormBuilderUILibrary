import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
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
) {
    MaterialTheme {
        FormScreen(
            parameterValueMap = parameterValueMap,
            parameterMap = parameterMap,
            visibilityMap = visibilityMap,
            enabledStatusMap = enabledStatusMap,
            onClick = {},
            action = "add",
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoiYWNjZXNzIiwiZXhwIjoxNzM0MzQyNjMxLCJpYXQiOjE3MzQzMzkwMzEsImp0aSI6IjU1OTZjYTMxNDc4YzRkMzViODAyY2I5OTRkZDczMjIxIiwidXNlcl9pZCI6NjZ9.wVuQmmaQ5SV7AsDXsYuiVg2yk8lkxT-C4s_e6ATs_gg"
        )
    }
}