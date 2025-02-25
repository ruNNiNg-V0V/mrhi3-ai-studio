package mrhi3.ai.studio

import android.content.Context
import android.widget.Toast
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
import androidx.lifecycle.viewmodel.compose.viewModel
import mrhi3.ai.studio.feature.text.SummarizeViewModel

data class CountryOptions(
    val country:String,
    val capital:List<String>
)

@Composable
internal fun MultiChoiceRoute(
    summarizeViewModel: SummarizeViewModel = viewModel(factory = GenerativeViewModelFactory)
) {
    val summarizeUiState by summarizeViewModel.uiState.collectAsState()
    // 특정 나라와 그 나라의 수도를 포함하는 수도 4를 json으로 받기
    summarizeViewModel.getMultiChoiceSource(" ")
    // 뷰 화면을 불러오는 컴포즈 함수
    QuizGame()
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
            .padding(0.dp)
    ) {

        // Title Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary
                )
                .shadow(2.dp)
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
                    color = Color(0xFFFFFF),
                    shape = RoundedCornerShape(8.dp)
                )
                .shadow(2.dp)
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

data class Question(val question: String, val options: List<String>, val answer: String)

val questions = listOf(
    Question("대한민국의 수도는?", listOf("평양", "파리", "도쿄", "서울"), "서울"),
    Question("미국의 수도는?", listOf("뉴욕", "워싱턴 D.C.", "시카고", "로스앤젤레스"), "워싱턴 D.C."),
    Question("프랑스의 수도는?", listOf("런던", "베를린", "마드리드", "파리"), "파리")
)

@Composable
fun QuizGame() {
    var currentQuestion by remember { mutableStateOf(questions.random()) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    BaseGameScreen(
        title = "사지선다 게임",
        onBackPressed = { /* 뒤로가기 처리 */ },
        onNewGameClick = { currentQuestion = questions.random() },
        onSaveClick = { saveGame() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentQuestion.question,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                currentQuestion.options.chunked(2).forEach { rowOptions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowOptions.forEach { option ->
                            Box(
                                modifier = Modifier
                                    .size(150.dp)
                                    .padding(vertical = 4.dp)
                                    .background(
                                        color = getColorForOption(option),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .shadow(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(
                                    onClick = {
                                        selectedAnswer = option
                                        checkAnswer(option, context, currentQuestion.answer)
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
}



@Composable
fun getColorForOption(option: String): Color {
    return when (option) {
        "평양", "뉴욕", "런던" -> Color(0xFFE57373)
        "파리", "워싱턴 D.C." -> Color(
            0xFFFFF176)
        "도쿄", "시카고", "마드리드" -> Color(0xFF81C784)
        "서울", "로스앤젤레스", "베를린" -> Color(0xFF64B5F6)
        else -> Color.Gray
    }
}

fun checkAnswer(selectedAnswer: String, context: Context, correctAnswer: String) {
    if (selectedAnswer == correctAnswer) {
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