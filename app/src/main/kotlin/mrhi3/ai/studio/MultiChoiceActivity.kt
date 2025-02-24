package mrhi3.ai.studio

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.draw.shadow

class MultiChoiceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuizGame()
        }
    }
}

@Composable
fun BaseGameScreen(
    title: String,
    onBackPressed: () -> Unit,
    onNewGameClick: () -> Unit,
    onSaveClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        // Title Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp)
                )
                .shadow(4.dp)
        ) {
            IconButton(
                onClick = onBackPressed,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = title.uppercase(),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 48.dp)
            )
        }

        // Game Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 16.dp)
                .background(
                    color = Color(0xFFFFC107),
                    shape = RoundedCornerShape(8.dp)
                )
                .shadow(4.dp)
        ) {
            content()
        }

        // Button Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onNewGameClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text(
                    text = "새 게임",
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            Button(
                onClick = onSaveClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text(
                    text = "저장",
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun QuizGame() {
    var question by remember { mutableStateOf("대한민국의 수도는?") }
    val options = listOf("평양", "파리", "도쿄", "서울")
    var selectedAnswer by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    BaseGameScreen(
        title = "사지선다 게임",
        onBackPressed = { /* 뒤로가기 처리 */ },
        onNewGameClick = { startNewGame() },
        onSaveClick = { saveGame() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = question,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            options.forEach { option ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(
                            color = getColorForOption(option),
                            shape = RoundedCornerShape(4.dp)
                        )
                ) {
                    Button(
                        onClick = {
                            selectedAnswer = option
                            checkAnswer(option, context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(4.dp)
                    ) {
                        Text(text = option, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun getColorForOption(option: String): Color {
    return when (option) {
        "평양" -> Color(0xFFE57373)
        "파리" -> Color(
            0xFFFFF176)
        "도쿄" -> Color(0xFF81C784)
        "서울" -> Color(0xFF64B5F6)
        else -> Color.Gray
    }
}

fun checkAnswer(selectedAnswer: String, context: Context) {
    if (selectedAnswer == "서울") {
        Toast.makeText(context, "정답입니다!", Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(context, "틀렸습니다!", Toast.LENGTH_SHORT).show()
    }
}

fun startNewGame() {
    // 게임 초기화 로직 추가
}

fun saveGame() {
    // 게임 저장 로직 추가
}

@Preview(showBackground = true)
@Composable
fun BaseGameScreenPreview() {
    MaterialTheme {
        BaseGameScreen(
            title = "게임 타이틀",
            onBackPressed = { },
            onNewGameClick = { },
            onSaveClick = { }
        ) {
            // 게임 콘텐츠
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("게임 콘텐츠")
            }
        }
    }
}