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
            activity = "10",
            form = "3",
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoiYWNjZXNzIiwiZXhwIjoxNzM0OTUyNTYyLCJpYXQiOjE3MzQ5NDg5NjIsImp0aSI6Ijc2YmIyYzAzN2NiMjQyMWY5MGFmOTlmNjYyNjIxZDIxIiwidXNlcl9pZCI6NjZ9.2tCFHhyLQLoQk373TQZ6CjYM91NYov0P4ImYj-ybFUI"
        )
    }
}