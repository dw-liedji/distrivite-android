import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalTime

@Composable
fun TiqTaqBlinking() {

    var currentTime by remember { mutableStateOf(LocalTime.now()) }

    // State to control whether to show "Tiq" or "Taq"
    var isTiq by remember { mutableStateOf(true) }

    // LaunchedEffect to update the time and switch Tiq/Taq every second
    LaunchedEffect(Unit) {
        while (true) {
            // Update time immediately on second changes
            currentTime = LocalTime.now()

            // Switch between "Tiq" and "Taq" every second
            isTiq = currentTime.second % 2 == 0

            // Wait for the next second before repeating
            delay(1000L - (System.currentTimeMillis() % 1000L))
        }
    }

    // Layout to display "Tiq" and "Taq" switching in the same place, with time to the right
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
        ) {
            // "Tiq" or "Taq" animation, synchronized with time
            BasicText(
                text = if (isTiq) "Class" else "AI",
                modifier = Modifier
                    .padding(8.dp),
                style = LocalTextStyle.current.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )

            // Display current time to the right of the animation
            BasicText(
                text = currentTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")),
                modifier = Modifier.padding(start = 4.dp),
                style = LocalTextStyle.current.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}
