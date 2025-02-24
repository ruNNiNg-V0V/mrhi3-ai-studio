package mrhi3.ai.studio.feature.combination

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CombinationGame(
    question: String,
    keyword: String,
    choices: String,
    hints: String,
    onAnswer1Click: () -> Unit,
    onAnswer2Click: () -> Unit,
    onAnswer3Click: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFFF5F5F5),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
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
                            text = question ,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = "힌트 \n$hints",
                fontSize = 20.sp,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.DarkGray)
                    .height(160.dp)
                    .padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement =  Arrangement.spacedBy(8.dp)
        ) {
            ButtonCard(answer = "1", text = choices, onClick = onAnswer1Click)
            ButtonCard(answer = "2", text = choices, onClick = onAnswer2Click)
            ButtonCard(answer = "3", text = choices, onClick = onAnswer3Click)
        }
    }
}

@Composable
fun ButtonCard(answer: String, text: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .height(50.dp)
            .shadow(4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0xFF80CBC4))
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


// 사용 예시
@Preview(showBackground = true)
@Composable
fun CombinationPreview() {
    CombinationGame(
        question = "String",
        keyword = "String",
        choices = "List<String>",
        hints = "List<String>",
        onAnswer1Click = { },
        onAnswer2Click = { },
        onAnswer3Click = { }
    )
}
