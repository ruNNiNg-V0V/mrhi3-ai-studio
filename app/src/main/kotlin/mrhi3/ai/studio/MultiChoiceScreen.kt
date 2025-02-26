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
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.gson.JsonParser
import mrhi3.ai.studio.feature.text.SummarizeViewModel

data class CountryOptions(
    val country: String,
    val options: List<String>
)

@Composable
internal fun MultiChoiceRoute(
    summarizeViewModel: SummarizeViewModel = viewModel(factory = GenerativeViewModelFactory)
) {
    val summarizeUiState by summarizeViewModel.uiState.collectAsState()

    val result = summarizeViewModel.getMultiChoiceSource() // 결과가 문자열로 나옴

    // 우선 result를 jsonObject로 변환
    val jsonObject = JsonParser.parseString(result).asJsonObject

    // jsonObject로 변환된 변수를 Gson으로 -> CountryOptions 타입이 변경됨
    val countryOptions: CountryOptions = Gson().fromJson(jsonObject.toString(), object : TypeToken<CountryOptions>() {}.type)

    // 뷰 화면을 불러오는 컴포즈 함수
    QuizGame(countryOptions)
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

@Composable
fun QuizGame(countryOptions: CountryOptions) {
    var currentQuestion by remember { mutableStateOf(countryOptions.options.random()) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    BaseGameScreen(
        title = "사지선다 게임",
        onBackPressed = { /* 뒤로가기 처리 */ },
        onNewGameClick = { currentQuestion = countryOptions.options.random() },
        onSaveClick = { saveGame() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${countryOptions.country}의 수도는?",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                countryOptions.options.chunked(2).forEach { rowOptions ->
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
                                        color = getColorForOption(option, countryOptions.options),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .shadow(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(
                                    onClick = {
                                        selectedAnswer = option
                                        checkAnswer(option, context, countryOptions.options.last())
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
