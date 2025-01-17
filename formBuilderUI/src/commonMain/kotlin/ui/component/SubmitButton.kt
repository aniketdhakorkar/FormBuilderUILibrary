package ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SubmitButton(showProgressIndicator: Boolean, onClick: () -> Unit, isEnabled: Boolean) {
    Button(
        modifier = Modifier.padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        enabled = isEnabled
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.padding(8.dp).padding(horizontal = 16.dp),
                text = "Submit",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            if (showProgressIndicator) {
                Spacer(modifier = Modifier.width(16.dp))
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        }
    }
}