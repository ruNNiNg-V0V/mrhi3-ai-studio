package com.example.conbination

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mrhi3.ai.studio.GenerativeViewModelFactory
import mrhi3.ai.studio.R
import mrhi3.ai.studio.feature.combination.CombinationView
import mrhi3.ai.studio.feature.combination.CombinationViewModel
import mrhi3.ai.studio.feature.combination.DataCombinationGame

@Composable
fun BaseGameScreen(
    title: String,
    onBackPressed: () -> Unit,
    onNewGameClick: () -> Unit,
    onSaveClick: () -> Unit,
    content: @Composable () -> Unit
) {
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
                text = title.uppercase(),
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
            content()
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

@Composable
fun Prompt(): DataCombinationGame? {
    val context = LocalContext.current
    val comViewModel: CombinationViewModel = viewModel(factory = GenerativeViewModelFactory)

    var sD: DataCombinationGame? = null

    runBlocking {
        sD = comViewModel.sendMessage(context.getString(R.string.combination_ex))
    }

    return sD
}

@Composable
fun getGameScreen() {
    //프롬포트를 실행하여 받은 데이터 클래스 값을 다음 항수에 넘겨줌
    //Prompt() : 채팅모델에 sendMessage(포롬포트 메시지) 넘겨줌
    //sendMessage() : messageToGson(result)로 json값 넘김
    //messageToGson() : 데이터 클래스의 객체로 storedData를 리턴 리턴 리턴
    val stored = Prompt()
    BaseGameScreenPreviewTest(dataclass = stored)
}


@Composable
fun BaseGameScreenPreviewTest(dataclass: DataCombinationGame?) {
    @Composable
    fun onNewGameClick() {
        val stored = Prompt()
        BaseGameScreenPreviewTest(dataclass = stored)
    }

    MaterialTheme {
        BaseGameScreen(
            title = "게임 타이틀",
            onBackPressed = { },
            onNewGameClick = { },
            onSaveClick = { }
        ) {
            // 게임 콘텐츠가 들어갈 자리
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if(dataclass != null) {
                    CombinationView(data = dataclass)
                } else {
                    Log.d("BaseGameScreenPreviewTest", "datas is null")
                }
            }
        }
    }
}
