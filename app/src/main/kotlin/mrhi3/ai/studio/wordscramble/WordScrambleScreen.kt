package mrhi3.ai.studio.wordscramble

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import mrhi3.ai.studio.GenerativeViewModelFactory
import mrhi3.ai.studio.MainActivity
import mrhi3.ai.studio.feature.text.SummarizeUiState
import mrhi3.ai.studio.feature.text.SummarizeViewModel
import mrhi3.ai.studio.firebase.showLoading
import mrhi3.ai.studio.firebase.saveWordScramble
import mrhi3.ai.studio.readData
import android.view.ViewGroup
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch
import mrhi3.ai.studio.GameListActivity

// 게임 데이터를 담을 데이터 클래스
data class WordScrambleData(
    val SW: String,  // Scrambled Word
    val CW: String   // Correct Word
) {
    // 기본 생성자 추가
    constructor() : this("PPLEA", "APPLE")
}

@Composable
fun WordScrambleGame(
    mode: String,
    gameSource: WordScrambleData = WordScrambleData()
) {
    BackHandler {
        // 아무런 동작 수행하지 않음으로써 뒤로가기 기능 무시
    }

    var gameData by remember { mutableStateOf(gameSource) }
    var userInput by remember { mutableStateOf("") }
    var isCorrect by remember { mutableStateOf<Boolean?>(null) }
    val context = LocalContext.current

    // WordScrambleGame 함수 내에 추가해야 할 변수들
    val scope = rememberCoroutineScope()

// 텍스트 인식을 위한 객체 생성
    val textRecognition = remember { WordScrambleTextRecognition(context) }

// 그리기 캔버스 참조
    val drawCanvasRef = remember { mutableStateOf<DrawCanvas?>(null) }

// 인식된 텍스트 상태
    var recognizedText by remember { mutableStateOf("") }
    var isRecognizing by remember { mutableStateOf(false) }


    // AI로 게임 소스 불러오기
    // 사용할 모델 선언
    val prompt: SummarizeViewModel = viewModel(factory = GenerativeViewModelFactory)
    // 모델의 상태 값의 변수
    val summarizeUiState by prompt.uiState.collectAsState()

    // 명령을 마친 후 작업을 관리할 변수
    var isLoading by remember { mutableStateOf(true) }

    if (isLoading) {
        showLoading(isLoading)
    }


    // 작업 시작 (GameList에서 들어온 경우 실행하지 않음)
    val job = remember {
        if (mode != "GameList") {
            prompt.getWordScramble(gameData)
        } else null
    }

    // 완료 확인
    LaunchedEffect(job) {
        if (job != null) {
            job.join() // 작업이 완료될 때까지 기다림
            isLoading = !job.isCompleted

            val result = prompt.getResult()
            when (summarizeUiState) {
                is SummarizeUiState.Success -> {
                    Log.d("result", result)
                    var cleanedJsonString = result.trim()

                    // 불필요한 문자 제거
                    cleanedJsonString = cleanedJsonString
                        .removePrefix("json")
                        .removePrefix("```json")
                        .removePrefix("```")
                        .trim()
                        .removeSuffix("```")
                        .trim()

                    Log.d("CleanedJson", cleanedJsonString)

                    val gson = Gson()
                    try {
                        gameData = gson.fromJson(cleanedJsonString, WordScrambleData::class.java)
                        Log.d("ParsedData", gameData.toString())
                    } catch (e: Exception) {
                        Log.e("JsonParseError", e.message ?: "Unknown error")
                        gameData = WordScrambleData("PPLEA", "APPLE")
                    }
                }
                else -> {
                    Log.e("error", "Failed to load data.")
                }
            }
        } else {
            // GameList에서 들어온 경우는 이미 데이터가 있음
            isLoading = false
        }
    }

    // 새 게임 요청 상태
    var haveToNewGame by remember { mutableStateOf(false) }

    if (haveToNewGame) {
        LaunchedEffect(haveToNewGame) {
            // 새 코루틴 작업 인스턴스를 생성합니다.
            val newJob = prompt.getWordScramble(gameData)
            newJob.join() // 작업이 완료될 때까지 대기
            isLoading = !newJob.isCompleted

            val result = prompt.getResult()
            when (summarizeUiState) {
                is SummarizeUiState.Success -> {
                    Log.d("result", result)
                    val cleanedJsonString = result.trim()
                        .removePrefix("json")
                        .removePrefix("```json")
                        .removePrefix("```")
                        .trim()
                        .removeSuffix("```")
                        .trim()

                    Log.d("CleanedJson", cleanedJsonString)

                    val gson = Gson()
                    try {
                        gameData = gson.fromJson(cleanedJsonString, WordScrambleData::class.java)
                        Log.d("ParsedData", gameData.toString())
                    } catch (e: Exception) {
                        Log.e("JsonParseError", e.message ?: "Unknown error")
                        gameData = WordScrambleData("PPLEA", "APPLE")
                    }
                }
                else -> {
                    Log.e("error", "Failed to load data.")
                }
            }
            haveToNewGame = false  // 작업 완료 후 상태 리셋
            isLoading = false
            userInput = ""  // 새 게임 시 사용자 입력 초기화
            isCorrect = null // 결과 상태 초기화
        }
    }

    // 저장 요청 상태
    var haveToSave by remember { mutableStateOf(false) }

    if (haveToSave) {
        saveWordScramble(context = context, gameData = gameData)
        haveToSave = false
        isLoading = false
    }

    // 뒤로가기 요청 상태
    var haveToBack by remember { mutableStateOf(false) }
    if (haveToBack) {
        when (mode) {
            "Main" -> {
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
            }
            else -> {
                val intent = Intent(context, GameListActivity::class.java)
                context.startActivity(intent)
            }
        }
    }

    // 답변 체크 함수
    fun checkAnswer() {
        if (userInput.equals(gameData.CW, ignoreCase = true)) {
            isCorrect = true
            Toast.makeText(context, "정답입니다!", Toast.LENGTH_SHORT).show()
        } else {
            isCorrect = false
            Toast.makeText(context, "틀렸습니다. 다시 시도해보세요.", Toast.LENGTH_SHORT).show()
        }
    }
    // 손글씨 인식 함수 (checkAnswer 함수 아래에 추가)
    fun recognizeHandwrittenText() {
        drawCanvasRef.value?.let { canvas ->
            isRecognizing = true
            scope.launch {
                try {
                    val bitmap = canvas.getCurrentCanvas()
                    val text = textRecognition.recognizeText(bitmap)
                    recognizedText = text

                    if (text.isNotEmpty()) {
                        // 인식된 텍스트를 입력 필드에 반영
                        userInput = text
                        Toast.makeText(context, "텍스트 인식: $text", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "텍스트를 인식할 수 없습니다", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("WordScramble", "텍스트 인식 실패", e)
                    Toast.makeText(context, "텍스트 인식 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isRecognizing = false
                }
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
                text = "WordScramble".uppercase(),
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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF9B7EBD))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "뒤섞인 단어",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = gameData.SW,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                                .fillMaxWidth()
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = userInput,
                        onValueChange = { userInput = it.uppercase() },
                        label = { Text("정답 입력") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )

                    Button(
                        onClick = { checkAnswer() },
                        modifier = Modifier
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = "확인",
                            fontSize = 16.sp
                        )
                    }
                }
                // 그림 그리기 캔버스 UI (isCorrect 관련 when 블록 아래에 추가)
// 손글씨 입력 영역
                Text(
                    text = "타이핑하거나 직접 써서 입력하세요",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )

// 그리기 영역 카드
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .background(Color.White)
                    ) {
                        // Android View를 Compose에 통합 - 그리기 캔버스
                        AndroidView(
                            factory = { ctx ->
                                DrawCanvas(ctx).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    drawCanvasRef.value = this
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // 인식하기 버튼
                        FloatingActionButton(
                            onClick = { recognizeHandwrittenText() },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 76.dp, end = 16.dp)
                                .size(48.dp),
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "텍스트 인식"
                            )
                        }

                        // 지우기 버튼
                        FloatingActionButton(
                            onClick = { drawCanvasRef.value?.init() },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                                .size(48.dp),
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "지우기"
                            )
                        }

                        // 로딩 인디케이터
                        if (isRecognizing) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(48.dp)
                            )
                        }
                    }
                }

// 인식된 텍스트 표시 (선택적)
                if (recognizedText.isNotEmpty()) {
                    Text(
                        text = "인식된 텍스트: $recognizedText",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }


                Spacer(modifier = Modifier.height(16.dp))

                when (isCorrect) {
                    true -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                        ) {
                            Text(
                                text = "정답입니다!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    false -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                        ) {
                            Text(
                                text = "틀렸습니다. 다시 시도해보세요.",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFFC62828),
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    null -> {
                        // 아직 답을 제출하지 않음
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
                    drawCanvasRef.value?.init()
                    recognizedText = ""
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