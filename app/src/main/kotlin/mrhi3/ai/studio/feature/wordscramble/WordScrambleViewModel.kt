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

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mrhi3.ai.studio.feature.chat.ChatViewModel

/**
 * Word Scramble 게임의 ViewModel
 */
class WordScrambleViewModel(
    private val context: Context,
    private val chatViewModel: ChatViewModel? = null
) : ViewModel() {
    private val TAG = "WordScrambleViewModel"

    // UI 상태를 관리하는 StateFlow
    private val _uiState = MutableStateFlow(WordScrambleUiState())
    val uiState: StateFlow<WordScrambleUiState> = _uiState.asStateFlow()

    // 텍스트 인식 유틸리티
    private val textRecognition = WordScrambleTextRecognition(context)

    // AI 단어 제공자 (chatViewModel이 null이 아닌 경우에만 초기화)
    private val aiProvider = chatViewModel?.let { WordScrambleAiProvider(it) }

    init {
        startNewGame()
    }

    /**
     * 이벤트 처리 함수
     */
    fun handleEvent(event: WordScrambleEvent) {
        when (event) {
            is WordScrambleEvent.SubmitAnswer -> checkAnswer(event.answer)
            is WordScrambleEvent.NewGame -> startNewGame()
            is WordScrambleEvent.UpdateAnswer -> updateAnswer(event.answer)
            is WordScrambleEvent.SaveDrawing -> saveDrawing(event.bitmap)
            is WordScrambleEvent.ChangeDrawMode -> changeDrawMode(event.mode)
            is WordScrambleEvent.ClearDrawing -> clearDrawing()
            is WordScrambleEvent.RecognizeDrawing -> recognizeDrawing(event.bitmap)
        }
    }

    /**
     * 새 게임 시작
     */
    private fun startNewGame() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                // AI를 통해 단어 가져오기 시도 (실패 시 기본 단어 사용)
                val originalWord = getWordForGame()
                val scrambledWord = WordScrambleUtils.scrambleWord(originalWord)

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        gameState = GameState.Playing,
                        wordModel = WordScrambleModel(
                            originalWord = originalWord,
                            scrambledWord = scrambledWord,
                            userAnswer = "",
                            drawingBitmap = null,
                            isCorrect = false
                        ),
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting new game", e)
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "게임을 시작하는 중 오류가 발생했습니다."
                ) }
            }
        }
    }

    /**
     * 게임에 사용할 단어를 가져옵니다.
     * AI 사용 가능 시 AI에서 가져오고, 아니면 기본 단어 목록에서 가져옵니다.
     */
    private suspend fun getWordForGame(maxLength: Int = 7): String {
        return try {
            if (aiProvider != null) {
                aiProvider.getRandomWord(maxLength)
            } else {
                getDefaultWord()
            }
        } catch (e: Exception) {
            Log.e(TAG, "AI에서 단어를 가져오는 중 오류 발생", e)
            getDefaultWord()
        }
    }

    /**
     * 기본 단어 목록에서 랜덤 단어를 반환합니다.
     */
    private fun getDefaultWord(): String {
        val defaultWords = listOf(
            "APPLE", "BANANA", "CHERRY", "DRAGON", "FLOWER",
            "GARDEN", "HOUSE", "ISLAND", "JUNGLE", "KNIGHT",
            "LEMON", "MONKEY", "NATURE", "ORANGE", "PURPLE",
            "QUEEN", "RIVER", "SUNSET", "TIGER", "UMBRELLA"
        )
        return defaultWords.random()
    }

    /**
     * 사용자 입력 업데이트
     */
    private fun updateAnswer(answer: String) {
        _uiState.update { currentState ->
            val wordModel = currentState.wordModel.copy(userAnswer = answer)
            currentState.copy(wordModel = wordModel)
        }
    }

    /**
     * 정답 확인
     */
    private fun checkAnswer(answer: String) {
        val currentModel = _uiState.value.wordModel
        val isCorrect = WordScrambleUtils.checkAnswer(currentModel.originalWord, answer)

        _uiState.update { currentState ->
            currentState.copy(
                gameState = if (isCorrect) GameState.Correct else GameState.Wrong,
                wordModel = currentState.wordModel.copy(
                    userAnswer = answer,
                    isCorrect = isCorrect
                )
            )
        }
    }

    /**
     * 그림 저장
     */
    private fun saveDrawing(bitmap: Bitmap) {
        _uiState.update { currentState ->
            currentState.copy(
                wordModel = currentState.wordModel.copy(
                    drawingBitmap = bitmap
                )
            )
        }
    }

    /**
     * 그리기 모드 변경
     */
    private fun changeDrawMode(mode: DrawMode) {
        _uiState.update { it.copy(drawMode = mode) }
    }

    /**
     * 그림 지우기
     */
    private fun clearDrawing() {
        _uiState.update { currentState ->
            currentState.copy(
                wordModel = currentState.wordModel.copy(
                    drawingBitmap = null
                )
            )
        }
    }

    /**
     * 손글씨 이미지에서 텍스트 인식
     */
    private fun recognizeDrawing(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                // ML Kit를 사용하여 텍스트 인식
                val recognizedText = textRecognition.recognizeText(bitmap)

                if (recognizedText.isNotEmpty()) {
                    // 인식된 텍스트가 있으면 정답 확인
                    val currentModel = _uiState.value.wordModel
                    val isCorrect = textRecognition.isMatchingAnswer(
                        recognizedText,
                        currentModel.originalWord
                    )

                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            gameState = if (isCorrect) GameState.Correct else GameState.Wrong,
                            wordModel = currentState.wordModel.copy(
                                userAnswer = recognizedText,
                                isCorrect = isCorrect
                            )
                        )
                    }
                } else {
                    // 인식된 텍스트가 없는 경우
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            errorMessage = "텍스트를 인식할 수 없습니다. 다시 시도해주세요."
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "텍스트 인식 중 오류 발생", e)
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = "텍스트 인식 중 오류가 발생했습니다: ${e.message}"
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        textRecognition.close()
    }
}