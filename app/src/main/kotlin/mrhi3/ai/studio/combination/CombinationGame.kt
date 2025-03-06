package mrhi3.ai.studio.combination

import android.content.Intent
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import mrhi3.ai.studio.GenerativeViewModelFactory
import mrhi3.ai.studio.MainActivity
import mrhi3.ai.studio.feature.text.SummarizeUiState
import mrhi3.ai.studio.feature.text.SummarizeViewModel
import mrhi3.ai.studio.firebase.saveCombination
import mrhi3.ai.studio.firebase.showLoading
import mrhi3.ai.studio.readData

data class CombinationData (
    val q: String = "윷 + ? -> 윷놀이",
    val k: String = "말",
    val choices: List<String> = listOf("의자", "연필", "말"),
    val hints: List<String> = listOf("4개 존재", "놀이 도구", "움직임")
)

@Composable
fun CombinationGame(mode: String, gameSource: CombinationData = CombinationData()) {

    BackHandler {
        // 아무런 동작 수행하지 않음으로써 뒤로가기 기능 무시
    }
    var buttonColors by remember { mutableStateOf(listOf(Color(0xFF80CBC4), Color(0xFF80CBC4), Color(0xFF80CBC4))) }
    var comGameData by remember { mutableStateOf(gameSource) }

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
        val job = remember { prompt.getCombinationQize(comGameData) }
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
                    comGameData = gson.fromJson(cleanedJsonString, CombinationData::class.java)
                    Log.d("ParsedData", comGameData.toString())
                    buttonColors = (listOf(Color(0xFF80CBC4), Color(0xFF80CBC4), Color(0xFF80CBC4)))
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
            val newJob = prompt.getCombinationQize(comGameData)
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
                    comGameData = gson.fromJson(cleanedJsonString, CombinationData::class.java)
                    Log.d("ParsedData", comGameData.toString())
                    buttonColors = (listOf(Color(0xFF80CBC4), Color(0xFF80CBC4), Color(0xFF80CBC4)))
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
        saveCombination(context = context, comGameData = comGameData)
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

    fun onChoiceClicks(index: Int, choice: String, k: String) {
        if (choice == k) {
            Log.d("onChoiceClicks", "True")
            buttonColors = buttonColors.toMutableList().apply {
                this[index] = Color.Green
            }
        } else {
            Log.d("onChoiceClicks", "False")
            buttonColors = buttonColors.toMutableList().apply {
                this[index] = Color.Red
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
                text = "CombinationGame".uppercase(),
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
                    .background(
                        Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "주제 : 한국 전통문화",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp),
                                color = Color(0xFF424242)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = comGameData.q,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = Color.DarkGray)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "힌트",
                            fontSize = 20.sp,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        comGameData.hints.forEach { hint ->
                            Text(
                                text = "- $hint",
                                fontSize = 16.sp,
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    comGameData.choices.forEachIndexed { index, choice ->
                        ButtonCard(answer = "${index + 1}",
                            text = choice,
                            onClick = { onChoiceClicks(index, choice, comGameData.k) },
                            backgroundColor = buttonColors[index])
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

@Composable
fun ButtonCard(answer: String, text: String, onClick: () -> Unit, backgroundColor: Color) {
    Card(
        modifier = Modifier
            .height(50.dp)
            .shadow(4.dp)
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxSize()
                .background(color = backgroundColor)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = answer,
                    fontSize = 22.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = text,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview
@Composable
fun previewer() {
    CombinationGame(mode = "Main", CombinationData())
}