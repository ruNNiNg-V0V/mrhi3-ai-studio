package mrhi3.ai.studio

import Card
import MatchingGame
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mrhi3.ai.studio.feature.combination.CombinationGame
import mrhi3.ai.studio.feature.text.SummarizeUiState
import mrhi3.ai.studio.feature.text.SummarizeViewModel
import mrhi3.ai.studio.firebase.showLoading
import mrhi3.ai.studio.multiChoice.MultiChoiceGame
import shuffledCards

sealed class GameData {
    data class MultiChoiceData(
        val q: String = "대한민국",
        val k: String = "서울",
        val choices: List<String> = listOf("평양", "파리", "도쿄", "서울")
    ) : GameData()

    data class CardMatchingData(
        val cards: List<Card> = shuffledCards()
    ) : GameData()
}


@Composable
fun GetGameSource(category: String) {
    val context = LocalContext.current

    var gameData by remember { mutableStateOf<GameData?>(null) }

    when (category) {
        // 게임 카테고리에 맞게 게임 화면 출력
        context.getString(R.string.MultiChoice) -> {
            gameData = GameData.MultiChoiceData()
            MultiChoiceGame(gameData as GameData.MultiChoiceData)
        }

        context.getString(R.string.MatchingCards) -> {
            gameData = GameData.CardMatchingData()
            MatchingGame(gameData as GameData.CardMatchingData)
        }

        context.getString(R.string.WordScramble) -> {
            Log.d("WordScramble", "WordScramble")
        }

        context.getString(R.string.Combination) -> {
            CombinationGame()
        }

        else -> {
            Log.d("else", "등록되지 않은 게임입니다.")
        }
    }

    // AI로 게임 소스 불러오기
    // 사용할 모델 선언
    val prompt: SummarizeViewModel = viewModel(factory = GenerativeViewModelFactory)
    // 모델의 상태 값의 변수
    val summarizeUiState by prompt.uiState.collectAsState()

    // 명령을 마친 후 작업을 관리할 변수
    var isLoading by remember { mutableStateOf(true) }

    // 작업 시작
    val job = remember { prompt.summarizeStreaming(context, "MultiChoice") }

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
                /**
                 * TODO
                 * 문자열을 gameData로 사용할 수 있게 전처리 후 Gson 적용
                 */
            }

            else -> {

            }
        }
    }

}

@Composable
fun BaseGameScreen(
    category: String,
    onBackPressed: () -> Unit,
    onNewGameClick: () -> Unit,
    onSaveClick: () -> Unit
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
                text = category.uppercase(),
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
            GetGameSource(category)
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
                    onClick = onNewGameClick,
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
                    onClick = onSaveClick,
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