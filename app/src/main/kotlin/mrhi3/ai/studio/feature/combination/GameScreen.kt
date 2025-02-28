package mrhi3.ai.studio.feature.combination


import android.util.Log
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CombinationGame() {

    var buttonColors by remember { mutableStateOf(listOf(Color(0xFF80CBC4), Color(0xFF80CBC4), Color(0xFF80CBC4))) }
    var combinationData by remember { mutableStateOf(BasicSource()) }

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
                            text = combinationData.q,
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

                combinationData.hints.forEach { hint ->
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
            combinationData.choices.forEachIndexed { index, choice ->
                ButtonCard(answer = "${index + 1}",
                    text = choice,
                    onClick = { onChoiceClicks(index, choice, combinationData.k) },
                    backgroundColor = buttonColors[index])
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
