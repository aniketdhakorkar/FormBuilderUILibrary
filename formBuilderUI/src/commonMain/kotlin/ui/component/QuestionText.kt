package ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import model.parameters.Style
import model.parameters.parseColor

@Composable
fun GenerateText(
    question: String,
    style: Style?,
    isMandatory: String,
    parameterDescription: String
) {

    val fontStyle =
        if ((style?.textStyle ?: "").contains("italic")) FontStyle.Italic else FontStyle.Normal
    val fontWeight =
        if ((style?.textStyle ?: "").contains("bold")) FontWeight.Bold else FontWeight.Normal
    val fontColor =
        if (style == null) MaterialTheme.colorScheme.onBackground else parseColor(style.fontColor)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if (style == null) Color.Transparent
                    else parseColor(style.backgroundColor)
                ),
            text = buildAnnotatedString {
                append(question)
                if (isMandatory == "true")
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Red
                        )
                    ) {
                        append("  *")
                    }
            },
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontSize = 18.sp,
            color = fontColor
        )

        if (parameterDescription.isNotBlank())
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                text = "($parameterDescription)",
                fontStyle = FontStyle.Italic,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = .5f)
            )
    }
}