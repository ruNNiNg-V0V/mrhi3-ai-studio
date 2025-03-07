import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import mrhi3.ai.studio.GameListActivity
import mrhi3.ai.studio.GenerativeViewModelFactory
import mrhi3.ai.studio.MainActivity
import mrhi3.ai.studio.feature.text.SummarizeUiState
import mrhi3.ai.studio.feature.text.SummarizeViewModel
import mrhi3.ai.studio.firebase.saveMatchingCards
import mrhi3.ai.studio.firebase.showLoading
import mrhi3.ai.studio.readData

data class Card(
    val value: String, // 키 값
    val label: String, // 표시 값
    var revealed: Boolean = false,
    var removed: Boolean = false,
    val elevation: Dp = 4.dp
)

data class MatchingCards(
    val a: List<String> = listOf("강원도", "감자"),
    val b: List<String> = listOf("전라도", "김치"),
    val c: List<String> = listOf("경상도", "사과"),
    val d: List<String> = listOf("충청도", "딸기"),
    val e: List<String> = listOf("경기도", "한우"),
    val f: List<String> = listOf("제주도", "감귤")
)

@Composable
fun MatchingGame(mode: String, gameSource: MatchingCards = MatchingCards()) {

    val context = LocalContext.current

    var dataSource by remember {
        mutableStateOf(gameSource)
    }

    val cards = mutableListOf<Card>()

    // MatchingCards의 각 속성에 접근하여 처리
    dataSource.run {
        a.forEach { value -> cards.add(Card(value = "a", label = value)) }
        b.forEach { value -> cards.add(Card(value = "b", label = value)) }
        c.forEach { value -> cards.add(Card(value = "c", label = value)) }
        d.forEach { value -> cards.add(Card(value = "d", label = value)) }
        e.forEach { value -> cards.add(Card(value = "e", label = value)) }
        f.forEach { value -> cards.add(Card(value = "f", label = value)) }
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
    var isLoading by remember { mutableStateOf(false) }
    if (mode == "Main") {
        isLoading = true
        // AI로 게임 소스 불러오기
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
                        a.forEach { value -> cards.add(Card(value = "a", label = value)) }
                        b.forEach { value -> cards.add(Card(value = "b", label = value)) }
                        c.forEach { value -> cards.add(Card(value = "c", label = value)) }
                        d.forEach { value -> cards.add(Card(value = "d", label = value)) }
                        e.forEach { value -> cards.add(Card(value = "e", label = value)) }
                        f.forEach { value -> cards.add(Card(value = "f", label = value)) }
                    }
                    gameData = cards.shuffled()
                    Log.d("MatchingGameDebug", "Cleaned JSON: $rawJson")
                }

                else -> {
                    Log.d("MatchingGameDebug", "Failed to load data")
                }

            }
        }
    }

    var haveToNewGame by remember { mutableStateOf(false) }

    if (haveToNewGame) {
        LaunchedEffect(haveToNewGame) {
            isLoading = true
            // 새 코루틴 작업 인스턴스를 생성합니다.
            val newJob = prompt.getMatchingCards(dataSource)
            newJob.join() // 작업이 완료될 때까지 대기
            isLoading = !newJob.isCompleted

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
                        a.forEach { value -> cards.add(Card(value = "a", label = value)) }
                        b.forEach { value -> cards.add(Card(value = "b", label = value)) }
                        c.forEach { value -> cards.add(Card(value = "c", label = value)) }
                        d.forEach { value -> cards.add(Card(value = "d", label = value)) }
                        e.forEach { value -> cards.add(Card(value = "e", label = value)) }
                        f.forEach { value -> cards.add(Card(value = "f", label = value)) }
                    }
                    gameData = cards.shuffled()
                    Log.d("MatchingGameDebug", "Cleaned JSON: $rawJson")
                }

                else -> {
                    Log.d("MatchingGameDebug", "Failed to load data")
                }

            }

            haveToNewGame = false  // 작업 완료 후 상태 리셋
            isLoading = false
        }
    }

    var haveToSave by remember { mutableStateOf(false) }

    if (haveToSave) {
        saveMatchingCards(context = context, dataSource = dataSource)
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
                val intent = Intent(context, GameListActivity::class.java)
                context.startActivity(intent)
            }
        }
    }

    if (isLoading) {
        showLoading(isLoading)
    }

    if (gameData.all { it.removed }) {
        Toast.makeText(context, "GameClear!!", Toast.LENGTH_SHORT).show()
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
                text = "MatchingCards".uppercase(),
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
            // 게임 화면 구현
            MatchingGameUI(
                gameDataLeft = gameData,
                firstCard = firstCard,
                setFirstCard = ::setFirstCard
            )
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
                color = if (card.removed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .background(
                    color = if (isSelected || card.removed || card.revealed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Text(
                text = if (isSelected || card.removed || card.revealed) card.label else "?",
                color = if (isSelected || card.removed || card.revealed) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}