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
            isSubmitButtonVisible = true,
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoiYWNjZXNzIiwiZXhwIjoxNzM0MDIzMTY1LCJpYXQiOjE3MzQwMTk1NjUsImp0aSI6ImFjMjc5NDdmNTVkYzRmYzY5NjZkNzg5NzY1YjQwZDkwIiwidXNlcl9pZCI6NjZ9.H7Mxy25JdIYwvPv54W3QpLp-u6z3JPclakbB0nX9LJo"
        )
    }
}