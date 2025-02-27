package mrhi3.ai.studio

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GetGameScreen(route: String) {
    BaseGameScreen(route, {}, {}, {})
}

@Composable
fun GetGameSource(category: String) {
    val context = LocalContext.current
    when (category) {
        // 게임 카테고리에 맞게 게임 화면 출력

        context.getString(R.string.MultiChoice) -> {
            Log.d("MultiChoice", "MultiChoice")
        }

        context.getString(R.string.WordScramble) -> {
            Log.d("WordScramble", "WordScramble")
        }

        context.getString(R.string.Combination) -> {
            Log.d("Combination", "Combination")
        }

        context.getString(R.string.MatchingCards) -> {
            Log.d("MatchingCards", "MatchingCards")
        }

        else -> {
            Log.d("else","등록되지 않은 게임입니다.")
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
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        // Title Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp)
                )
                .shadow(4.dp)
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
                text = category,
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
        ) {
            // 게임 구성 화면이 들어갈 자리
            GetGameSource(category)
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
