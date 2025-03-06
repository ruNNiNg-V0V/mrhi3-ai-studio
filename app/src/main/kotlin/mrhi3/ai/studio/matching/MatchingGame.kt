import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import mrhi3.ai.studio.GenerativeViewModelFactory
import mrhi3.ai.studio.feature.text.SummarizeUiState
import mrhi3.ai.studio.feature.text.SummarizeViewModel
import mrhi3.ai.studio.firebase.showLoading

data class Card(
    val value: String, // 키 값
    val label: String, // 표시 값
    var revealed: Boolean = false,
    var removed: Boolean = false,
    val elevation: Dp = 4.dp
)

// JSON 문자열에서 '''를 제거하는 함수
fun cleanJsonString(rawJson: String): String {
    return rawJson
        .replace("```", "") // 작은따옴표 제거
        .replace("\n", "")  // 줄바꿈 제거
        .replace("json","")
        .trim()             // 앞뒤 공백 제거
}

// JSON 데이터를 UTF-8로 디코딩하고 추가 처리를 수행하는 함수
fun processAiResult(rawResult: String): String {
    val cleanedResult = cleanJsonString(rawResult)
    return cleanedResult
}

// JSON 문자열을 Card 리스트로 변환하는 함수
fun parseJsonToGameData(jsonString: String): List<Card> {
    val gson = Gson()
    val type = object : TypeToken<Map<String, List<String>>>() {}.type
    val parsedMap: Map<String, List<String>> = gson.fromJson(jsonString, type)

    val cards = mutableListOf<Card>()
    parsedMap.forEach { (key, values) ->
        values.forEach { value ->
            cards.add(Card(value = key, label = value))
        }
    }

    return cards.shuffled() // 카드 섞기
}

// JSON 데이터를 gameData에 초기화
fun initializeGameDataFromJson(gameData: MutableList<Card>, rawJson: String) {
    try {
        val cleanedJson = cleanJsonString(rawJson)
        Log.d("MatchingGameDebug", "Cleaned JSON: $cleanedJson")
        val parsedCards = parseJsonToGameData(cleanedJson)
        gameData.clear()
        gameData.addAll(parsedCards)
    } catch (e: Exception) {
        Log.e("MatchingGameDebug", "Failed to initialize game data: ${e.message}")
    }
}

@Composable
fun MatchingGame() {
    val gameData = remember { mutableStateListOf<Card>() }
    val firstCard = remember { mutableStateOf<Card?>(null) }

    fun setFirstCard(card: Card?) {
        firstCard.value = card
    }

    // 게임 데이터 초기화
    LaunchedEffect(Unit) {
        val rawJson = """
            {
                "A": ["강원도", "감자"],
                "B": ["전라도", "김치"],
                "C": ["경상도", "사과"],
                "D": ["충청도", "딸기"],
                "E": ["경기도", "한우"],
                "F": ["제주도", "감귤"]
            }
        """
        initializeGameDataFromJson(gameData, rawJson)
    }

    // AI로 게임 소스 불러오기
    val prompt: SummarizeViewModel = viewModel(factory = GenerativeViewModelFactory)
    val summarizeUiState by prompt.uiState.collectAsState()
    var isLoading by remember { mutableStateOf(true) }
    val job = remember { prompt.summarizeStreaming() }

    LaunchedEffect(job) {
        job.join()
        isLoading = summarizeUiState !is SummarizeUiState.Success
        val rawResult = prompt.getResult()
        val processedResult = processAiResult(rawResult)
        when (summarizeUiState) {
            is SummarizeUiState.Success -> {
                Log.d("MatchingGameDebug", processedResult)
                initializeGameDataFromJson(gameData, processedResult)
            }
            else -> {
                Log.d("MatchingGameDebug", "Failed to load data")
            }
        }
    }

    if (isLoading) {
        showLoading(isLoading)
    }

    // 게임 화면 구현
    MatchingGameUI(
        gameDataLeft = gameData,
        firstCard = firstCard,
        setFirstCard = ::setFirstCard
    )

    if (gameData.all { it.removed }) {
        Text("Game Cleared!")
    }
}

@Composable
fun MatchingGameUI(
    gameDataLeft: MutableList<Card>,
    firstCard: MutableState<Card?>,
    setFirstCard: (Card?) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // 왼쪽 화면 (카드 앞면)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(gameDataLeft) { card ->
                CardItem(
                    card = card,
                    isSelected = firstCard.value == card,
                    onClick = {
                        if (firstCard.value == null) {
                            card.revealed = true
                            setFirstCard(card)
                        } else if (firstCard.value != card) {
                            if (firstCard.value!!.value.uppercase() == card.value.uppercase()) {
                                firstCard.value!!.removed = true
                                card.removed = true
                                setFirstCard(null)
                            } else {
                                // 새로운 카드가 선택되면 이전에 선택된 카드를 다시 뒤집기
                                firstCard.value!!.revealed = false
                                card.revealed = true
                                setFirstCard(card)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CardItem(
    card: Card,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(112.dp)
            .clickable { onClick() }
            .background(
                color = if (card.removed) Color.Gray else Color(0xFF5D9CEC),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp) // 내부 여백 추가
                .background(
                    color = if (isSelected || card.removed || card.revealed) Color(0xFF5D9CEC) else Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Text(
                text = if (isSelected || card.removed || card.revealed) card.label else "?",
                color = Color(0xFFE6E9ED),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
