package mrhi3.ai.studio.multiChoice

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.Navigation
import com.google.gson.Gson
import mrhi3.ai.studio.GenerativeViewModelFactory
import mrhi3.ai.studio.MainActivity
import mrhi3.ai.studio.feature.text.SummarizeUiState
import mrhi3.ai.studio.feature.text.SummarizeViewModel
import mrhi3.ai.studio.firebase.saveMultiChoice
import mrhi3.ai.studio.firebase.showLoading
import mrhi3.ai.studio.readData

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

fun checkAnswer(context: Context, selectedAnswer: String, correctAnswer: String) {
    if (selectedAnswer == correctAnswer) {
        Toast.makeText(context, "정답입니다!", Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(context, "틀렸습니다!", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun MultiChoiceGame(mode: String, gameSource: CountryOptions = CountryOptions()) {

    BackHandler {
        // 아무런 동작 수행하지 않음으로써 뒤로가기 기능 무시
    }

    var gameData by remember { mutableStateOf(gameSource) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // AI로 게임 소스 불러오기
    // 사용할 모델 선언
    val prompt: SummarizeViewModel = viewModel(factory = GenerativeViewModelFactory)
    // 모델의 상태 값의 변수
    val summarizeUiState by prompt.uiState.collectAsState()

    // 명령을 마친 후 작업을 관리할 변수
    var isLoading by remember { mutableStateOf(false) }

    if (isLoading) {
        showLoading(isLoading)
    }

    // 완료 확인
    if (mode == "Main") {
        // 작업 시작
        val job = remember { prompt.getCountryOptions(gameData) }
        isLoading = true
        LaunchedEffect(job) {
            job.join() // 작업이 완료될 때까지 기다림
            isLoading = !job.isCompleted
            val result = prompt.getResult()
            when (summarizeUiState) {
                is SummarizeUiState.Success -> {
                    Log.d("result", result)
                    /**
                     * TODO
                     * 문자열을 gameData로 사용할 수 있게 전처리 후 Gson 적용
                     */
                    var cleanedJsonString = result.trim()

                    //불필요한 문자 제거
                    cleanedJsonString = cleanedJsonString
                        .removePrefix("json")
                        .removePrefix("```json")
                        .removePrefix("```")
                        .trim()

                    cleanedJsonString = cleanedJsonString.removeSuffix("```").trim()
                    Log.d("CleanedJson", cleanedJsonString)

                    val gson = Gson()
                    gameData = gson.fromJson(cleanedJsonString, CountryOptions::class.java)
                    Log.d("ParsedData", gameData.toString())
                }

                else -> {
                    Log.e("error", "Failed to load data.")
                }
            }
        }
    }

    var haveToNewGame by remember { mutableStateOf(false) }

    if (haveToNewGame) {
        LaunchedEffect(haveToNewGame) {
            // 새 코루틴 작업 인스턴스를 생성합니다.
            val newJob = prompt.getCountryOptions(gameData)
            newJob.join() // 작업이 완료될 때까지 대기
            isLoading = !newJob.isCompleted

            val result = prompt.getResult()
            when (summarizeUiState) {
                is SummarizeUiState.Success -> {
                    Log.d("result", result)
                    /**
                     * TODO:
                     * 문자열을 gameData로 사용할 수 있게 전처리 후 Gson 적용
                     */
                    var cleanedJsonString = result.trim()
                        .removePrefix("json")
                        .removePrefix("```json")
                        .removePrefix("```")
                        .trim()
                        .removeSuffix("```")
                        .trim()

                    Log.d("CleanedJson", cleanedJsonString)

                    val gson = Gson()
                    gameData = gson.fromJson(cleanedJsonString, CountryOptions::class.java)
                    Log.d("ParsedData", gameData.toString())
                }

                else -> {
                    Log.e("error", "Failed to load data.")
                }
            }
            haveToNewGame = false  // 작업 완료 후 상태 리셋
            isLoading = false
        }
    }

    var haveToSave by remember { mutableStateOf(false) }

    if (haveToSave) {
        saveMultiChoice(context = context, gameData = gameData)
        haveToSave = false
        isLoading = false
    }

    var haveToBack by remember { mutableStateOf(false) }
    if (haveToBack) {
        when (mode) {
            "Main" -> {
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
            }

            else -> {
                readData()
            }
        }
    }

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
        ) {
            IconButton(
                onClick = { haveToBack = true },
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
                text = "MultiChoice".uppercase(),
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
                .padding(top = 16.dp, bottom = 8.dp)
                .background(
                    color = Color(0xFFFFFF),
                    shape = RoundedCornerShape(8.dp)
                )

        ) {
            // Game content Area
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = gameData.q + "의 수도는 ?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    gameData.choices.chunked(2).forEach { rowOptions ->
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
                                            color = getColorForOption(option, gameData.choices),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .shadow(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Button(
                                        onClick = {
                                            selectedAnswer = option
                                            checkAnswer(context, option, gameData.k)
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

        // Button Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween  // 두 버튼 간의 공간을 동일하게
        ) {
            Box(
                modifier = Modifier
                    .weight(1f),
                contentAlignment = Alignment.Center  // 중앙 정렬
            ) {
                Button(
                    onClick = {
                        haveToNewGame = true
                        isLoading = true
                    },
                    modifier = Modifier
                        .width(180.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(
                        text = "새 게임",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        haveToSave = true
                        isLoading = true
                    },
                    modifier = Modifier
                        .width(180.dp)
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

}
