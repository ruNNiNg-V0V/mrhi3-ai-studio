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
import mrhi3.ai.studio.GenerativeViewModelFactory
import mrhi3.ai.studio.feature.text.SummarizeUiState
import mrhi3.ai.studio.feature.text.SummarizeViewModel
import mrhi3.ai.studio.firebase.showLoading

data class Card(
    val value: String, // 얘는 키 값
    val label: String, // 얘는 표시 값
    var revealed: Boolean = false,
    var removed: Boolean = false,
    val elevation: Dp = 4.dp
)

@Composable
fun MatchingGame(

) {
    val gameData = remember { mutableStateListOf<Card>() }
    val firstCard = remember { mutableStateOf<Card?>(null) }

    fun setFirstCard(card: Card?) {
        firstCard.value = card
    }

    // 게임 데이터 초기화
    LaunchedEffect(Unit) {
        initializeGameData(gameData)
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

    // AI로 게임 소스 불러오기
    // 사용할 모델 선언
    val prompt: SummarizeViewModel = viewModel(factory = GenerativeViewModelFactory)
    // 모델의 상태 값의 변수
    val summarizeUiState by prompt.uiState.collectAsState()

    // 명령을 마친 후 작업을 관리할 변수
    var isLoading by remember { mutableStateOf(true) }

    // 작업 시작
    val job = remember { prompt.summarizeStreaming() }

    // 완료 확인
    LaunchedEffect(job) {
        job.join() // 작업이 완료될 때까지 기다림
        isLoading = !job.isCompleted
    }

    if (isLoading) {
        showLoading(isLoading)
    } else {
        val result = prompt.getResult()
        when (summarizeUiState) {
            is SummarizeUiState.Success -> {
                Log.d("result", result)
            }

            else -> {

            }
        }
    }


}

fun initializeGameData(gameData: MutableList<Card>) {
    val values = listOf(
        "A", "a",
        "B", "b",
        "C", "c",
        "D", "d",
        "E", "e",
        "F", "f"
    )
    val labels = listOf(
        "강원도", "감자",
        "전라도", "김치",
        "경상도", "사과",
        "충청도", "딸기",
        "경기도", "한우",
        "제주도", "감귤"
    )
    gameData.clear()
    for (i in values.indices) {
        val card = Card(value = values[i], label = labels[i])
        gameData.add(card)
    }
    gameData.shuffled()
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
                        } else if (firstCard.value != card) {
                            if (firstCard.value!!.value.uppercase() == card.value.uppercase()) {
                                firstCard.value!!.removed = true
                                card.removed = true
                                setFirstCard(null)
                            } else {
                                // 새로운 카드가 선택되면 이전에 선택된 카드를 다시 뒤집기
                                firstCard.value!!.revealed = false
                                card.revealed = true
                            }
                        }
                        setFirstCard(card)
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
            .size(112.dp, 112.dp)
            .clickable { onClick() }
            .background(
                color = if (card.removed) Color.Gray else Color(0xFF5D9CEC),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
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