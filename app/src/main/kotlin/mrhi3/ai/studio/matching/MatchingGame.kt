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

data class MatchingCards(
    val A: List<String> = listOf("강원도","감자"),
    val B: List<String> = listOf("전라도","김치"),
    val C: List<String> = listOf("경상도","사과"),
    val D: List<String> = listOf("충청도","딸기"),
    val E: List<String> = listOf("경기도","한우"),
    val F: List<String> = listOf("제주도","감귤")
)

@Composable
fun MatchingGame(gameSource: MatchingCards = MatchingCards()) {

    var dataSource = gameSource

    val cards = mutableListOf<Card>()

    // MatchingCards의 각 속성에 접근하여 처리
    dataSource.run {
        A.forEach { value -> cards.add(Card(value = "A", label = value)) }
        B.forEach { value -> cards.add(Card(value = "B", label = value)) }
        C.forEach { value -> cards.add(Card(value = "C", label = value)) }
        D.forEach { value -> cards.add(Card(value = "D", label = value)) }
        E.forEach { value -> cards.add(Card(value = "E", label = value)) }
        F.forEach { value -> cards.add(Card(value = "F", label = value)) }
    }

    var gameData by remember { mutableStateOf(cards.shuffled()) }
    Log.d("gameData", gameData.toString())
    val firstCard = remember { mutableStateOf<Card?>(null) }

    fun setFirstCard(card: Card?) {
        firstCard.value = card
    }

    // AI로 게임 소스 불러오기
    val prompt: SummarizeViewModel = viewModel(factory = GenerativeViewModelFactory)
    val summarizeUiState by prompt.uiState.collectAsState()
    var isLoading by remember { mutableStateOf(true) }
    val job = remember { prompt.getMatchingCards(dataSource) }

    LaunchedEffect(job) {
        job.join()
        isLoading = summarizeUiState !is SummarizeUiState.Success
        val rawResult = prompt.getResult()
        when (summarizeUiState) {
            is SummarizeUiState.Success -> {
                Log.d("MatchingGameDebug", rawResult)
                val rawJson = rawResult.replace("```", "") // 작은따옴표 제거
                    .replace("\n", "")  // 줄바꿈 제거
                    .replace("json", "")
                    .trim()             // 앞뒤 공백 제거
                Log.d("MatchingGameDebug", "Cleaned JSON: $rawJson")

                val gson = Gson()
                val parsedMap = gson.fromJson(rawJson, MatchingCards::class.java)
                dataSource = parsedMap
                val cards = mutableListOf<Card>()
                // MatchingCards의 각 속성에 접근하여 처리

                parsedMap.run {
                    A.forEach { value -> cards.add(Card(value = "A", label = value)) }
                    B.forEach { value -> cards.add(Card(value = "B", label = value)) }
                    C.forEach { value -> cards.add(Card(value = "C", label = value)) }
                    D.forEach { value -> cards.add(Card(value = "D", label = value)) }
                    E.forEach { value -> cards.add(Card(value = "E", label = value)) }
                    F.forEach { value -> cards.add(Card(value = "F", label = value)) }
                }
                gameData = cards.shuffled()
                Log.d("MatchingGameDebug", "Cleaned JSON: $rawJson")
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
    gameDataLeft: List<Card>,
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
