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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mrhi3.ai.studio.GameData

data class Card(
    val value: String, // 얘는 키 값
    val label: String, // 얘는 표시 값
    var revealed: Boolean = false,
    var removed: Boolean = false,
    val elevation: Dp = 4.dp
)

@Composable
fun MatchingGame(gameData: GameData.CardMatchingData) {
    val firstCard = remember { mutableStateOf<Card?>(null) }

    fun setFirstCard(card: Card?) {
        firstCard.value = card
    }

    // 게임 화면 구현
    MatchingGameUI(
        gameDataLeft = gameData.cards,
        firstCard = firstCard,
        setFirstCard = ::setFirstCard
    )

}

fun shuffledCards(): List<Card> {
    val cards = ArrayList<Card>()
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
    cards.clear()
    for (i in values.indices) {
        val card = Card(value = values[i], label = labels[i])
        cards.add(card)
    }
    cards.shuffled()
    return cards.toList()
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