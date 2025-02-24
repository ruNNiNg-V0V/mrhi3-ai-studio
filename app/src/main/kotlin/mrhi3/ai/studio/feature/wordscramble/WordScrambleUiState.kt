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

/**
 * Word Scramble 게임의 UI 상태를 관리하는 클래스
 */
data class WordScrambleUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val gameState: GameState = GameState.Playing,
    val wordModel: WordScrambleModel = WordScrambleModel(),
    val drawMode: DrawMode = DrawMode.PEN
)

/**
 * 게임 상태를 나타내는 열거형
 */
enum class GameState {
    Playing,    // 게임 진행 중
    Correct,    // 정답을 맞춤
    Wrong       // 오답
}

/**
 * 그리기 모드를 나타내는 열거형
 */
enum class DrawMode {
    PEN,        // 펜 모드
}

/**
 * Word Scramble 게임의 이벤트를 정의하는 sealed 클래스
 */
sealed class WordScrambleEvent {
    data class SubmitAnswer(val answer: String) : WordScrambleEvent()
    object NewGame : WordScrambleEvent()
    data class UpdateAnswer(val answer: String) : WordScrambleEvent()
    data class SaveDrawing(val bitmap: Bitmap) : WordScrambleEvent()
    data class ChangeDrawMode(val mode: DrawMode) : WordScrambleEvent()
    object ClearDrawing : WordScrambleEvent()
    data class RecognizeDrawing(val bitmap: Bitmap) : WordScrambleEvent()
}