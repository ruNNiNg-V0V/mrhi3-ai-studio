/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mrhi3.ai.studio.feature.wordscramble

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

// Pen 클래스를 DrawCanvas 클래스 외부로 분리
class Pen(
    val x: Float,
    val y: Float,
    val moveStatus: Int,
    val color: Int,
    val size: Int
) {
    companion object {
        const val STATE_START = 0    // 펜의 상태(움직임 시작)
        const val STATE_MOVE = 1     // 펜의 상태(움직이는 중)
    }

    /**
     * 현재 pen의 상태가 움직이는 상태인지 반환합니다.
     */
    fun isMove(): Boolean {
        return moveStatus == STATE_MOVE
    }
}

// DrawCanvas 클래스 정의
class DrawCanvas(context: android.content.Context) : View(context) {
    companion object {
        const val PEN_SIZE = 3       // 펜 사이즈
    }

    private val drawCommandList = ArrayList<Pen>()  // 그리기 경로가 기록된 리스트
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var loadDrawImage: Bitmap? = null                // 호출된 이전 그림
    private var color = Color.BLACK   // 현재 펜 색상
    private var size = PEN_SIZE      // 현재 펜 크기

    init {
        init()
    }

    /**
     * 그리기에 필요한 요소를 초기화 합니다.
     */
    fun init() {
        drawCommandList.clear()
        loadDrawImage = null
        color = Color.BLACK
        size = PEN_SIZE
        invalidate()
    }

    /**
     * 현재까지 그린 그림을 Bitmap으로 반환합니다.
     */
    fun getCurrentCanvas(): Bitmap {
        val bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        this.draw(canvas)
        return bitmap
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.WHITE)

        loadDrawImage?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }

        for (i in drawCommandList.indices) {
            val p = drawCommandList[i]
            paint.color = p.color
            paint.strokeWidth = p.size.toFloat()

            if (p.isMove()) {
                val prevP = drawCommandList[i - 1]
                canvas.drawLine(prevP.x, prevP.y, p.x, p.y, paint)
            }
        }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val action = e.action
        val state = if (action == MotionEvent.ACTION_DOWN) Pen.STATE_START else Pen.STATE_MOVE
        drawCommandList.add(Pen(e.x, e.y, state, color, size))
        invalidate()
        return true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordScrambleScreen(
    onBackPressed: () -> Unit,
    viewModelFactory: WordScrambleViewModelFactory
) {
    val viewModel: WordScrambleViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    // Canvas 관련 상태 관리
    val drawCanvasRef = remember { mutableStateOf<DrawCanvas?>(null) }

    // 텍스트 필드 상태 관리
    var textFieldValue by remember { mutableStateOf("") }

    // 인식된 텍스트를 표시하기 위한 상태
    var recognizedText by remember { mutableStateOf("") }

    // 화면이 종료될 때 리소스 정리
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                // 필요한 정리 작업
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 상태 변화에 따른 효과 처리
    LaunchedEffect(uiState) {
        // 로딩 상태, 게임 상태, 에러 메시지 등에 따른 처리
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }

        when (uiState.gameState) {
            GameState.Correct -> {
                Toast.makeText(context, "정답입니다!", Toast.LENGTH_SHORT).show()
                recognizedText = "인식된 텍스트: ${uiState.wordModel.userAnswer} (정답)"
            }
            GameState.Wrong -> {
                if (uiState.wordModel.userAnswer.isNotEmpty()) {
                    Toast.makeText(
                        context,
                        "틀렸습니다. 다시 시도하세요.",
                        Toast.LENGTH_SHORT
                    ).show()
                    recognizedText = "인식된 텍스트: ${uiState.wordModel.userAnswer} (오답)"
                }
            }
            else -> {} // Playing 상태일 때는 아무 작업도 하지 않음
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Word Scramble") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "뒤로 가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 문제 영역 카드
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 게임 제목
                    Text(
                        text = "단어 맞추기",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // 스크램블된 단어 표시
                    Text(
                        text = uiState.wordModel.scrambledWord,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 6.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    // 인식된 텍스트 표시
                    Text(
                        text = recognizedText,
                        fontSize = 16.sp,
                        color = if (uiState.gameState == GameState.Correct)
                            androidx.compose.ui.graphics.Color.Green.copy(alpha = 0.8f)
                        else if (uiState.gameState == GameState.Wrong)
                            androidx.compose.ui.graphics.Color.Red.copy(alpha = 0.8f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // 그리기 영역 카드
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .background(androidx.compose.ui.graphics.Color.White)
                ) {
                    // Android View를 Compose에 통합 - 그리기 캔버스
                    AndroidView(
                        factory = { ctx ->
                            DrawCanvas(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                drawCanvasRef.value = this
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // 분석하기 플로팅 버튼 (상단)
                    FloatingActionButton(
                        onClick = {
                            drawCanvasRef.value?.let { canvas ->
                                canvas.invalidate()
                                val bitmap = canvas.getCurrentCanvas()
                                viewModel.handleEvent(WordScrambleEvent.SaveDrawing(bitmap))
                                viewModel.handleEvent(WordScrambleEvent.RecognizeDrawing(bitmap))
                                Toast.makeText(context, "그림을 분석 중입니다...", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 76.dp, end = 16.dp)
                            .size(48.dp),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "분석하기"
                        )
                    }

                    // 전체 지우기 버튼 (하단)
                    FloatingActionButton(
                        onClick = {
                            drawCanvasRef.value?.init()
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .size(48.dp),
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "전체 지우기"
                        )
                    }
                }
            }

            // 하단 버튼 영역
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.handleEvent(WordScrambleEvent.NewGame)
                        drawCanvasRef.value?.init()
                        textFieldValue = ""
                        recognizedText = ""
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "새 게임",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("새 게임", fontSize = 16.sp)
                }

                Button(
                    onClick = {
                        // 저장 기능은 아직 구현하지 않음 (SQLite 연동 예정)
                        Toast.makeText(context, "저장 기능은 아직 구현되지 않았습니다.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("저장하기", fontSize = 16.sp)
                }
            }
        }
    }
}