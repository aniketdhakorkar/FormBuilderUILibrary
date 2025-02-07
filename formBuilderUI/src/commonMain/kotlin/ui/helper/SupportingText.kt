package ui.helper

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import model.DependentValueCustomText

@Composable
fun SupportingText(
    dependentValueCustomText: DependentValueCustomText? = null  ,
    errorMessage: String
) {
    Column {
        if ((dependentValueCustomText?.isShow == true)) {
            Text(
                text = "${dependentValueCustomText.expression} ${dependentValueCustomText.value}",
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (errorMessage.isNotBlank()) {
            Text(
                text = errorMessage,
                fontStyle = FontStyle.Italic,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}