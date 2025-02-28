package mrhi3.ai.studio.multiChoice

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CountryOptions(
    val q: String = "대한민국",
    val k: String = "서울",
    val choices: List<String> = listOf("평양", "파리", "도쿄", "서울")
)

@Composable
fun getColorForOption(option: String, options: List<String>): Color {
    val colors = listOf(
        Color(0xFFE57373),  //첫 번째 옵션 색상
        Color(0xFFFFF176),  //두 번째 옵션 색상
        Color(0xFF81C784),  //세 번째 옵션 색상
        Color(0xFF64B5F6)   //네 번째 옵션 색상
    )

    // 옵션 인덱스에 따라 색상 반환
    return if (option in options) {
        colors[options.indexOf(option) % colors.size]
    } else {
        Color.Gray
    }
}

fun checkAnswer(context: Context,selectedAnswer: String) {
    val correctAnswer = CountryOptions().k
    if (selectedAnswer == correctAnswer) {
        Toast.makeText(context, "정답입니다!", Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(context, "틀렸습니다!", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun MultiChoiceGame() {
    val question  = CountryOptions().q
    val options = CountryOptions().choices
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = question + "의 수도는 ?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            options.chunked(2).forEach { rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    rowOptions.forEach { option ->
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .padding(vertical = 8.dp)
                                .background(
                                    color = getColorForOption(option, options),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .shadow(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = {
                                    selectedAnswer = option
                                    checkAnswer(context,option)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                Text(text = option, color = Color.Black, fontSize = 20.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
