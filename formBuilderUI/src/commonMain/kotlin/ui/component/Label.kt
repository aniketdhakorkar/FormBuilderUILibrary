package ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import model.parameters.Style
import model.parameters.parseColor

@Composable
fun CreateLabel(
    question: String,
    style: Style? = null,
    isVisible: Boolean
) {
    if (!isVisible) return

    val backgroundColor: Color =
        if (style?.backgroundColor == null) Color.Transparent else parseColor(style.backgroundColor)
    val fontStyle =
        if ((style?.textStyle ?: "").contains("italic")) FontStyle.Italic else FontStyle.Normal
    val fontWeight =
        if ((style?.textStyle ?: "").contains("bold")) FontWeight.Bold else FontWeight.Normal
    val fontColor =
        if (style?.fontColor == null) MaterialTheme.colorScheme.onBackground else parseColor(style.fontColor)

    Column(modifier = Modifier.padding(0.dp, 8.dp, 0.dp, 8.dp)) {
        Text(
            text = question,
            Modifier
                .fillMaxWidth()
                .background(color = backgroundColor)
                .padding(16.dp, 8.dp, 16.dp, 8.dp),
            fontSize = 16.sp,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            color = fontColor
        )
    }
}