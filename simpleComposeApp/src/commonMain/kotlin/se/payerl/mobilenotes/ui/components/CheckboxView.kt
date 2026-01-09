import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun CheckboxView(
    checked: Boolean,
    onItemCheckedChange: (Boolean) -> Unit,
    frontColor: Color = Color(0xFF27AD2D),
    backColor: Color = Color(0xFF9FC7A3),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(backColor),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconButton(
            onClick = { onItemCheckedChange(!checked) }
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Checked",
                    tint = frontColor,
                    modifier = Modifier.aspectRatio(1.0f)
                )
            }
        }
    }
}

@Preview
@Composable
fun CheckboxViewPreview() {
    Column(modifier = Modifier
        .height(60.dp)
        .width(40.dp)) {
        CheckboxView(
            checked = true,
            onItemCheckedChange = { _ -> }/*,
            modifier = Modifier
                .height(20.dp)*/
        )
    }
}