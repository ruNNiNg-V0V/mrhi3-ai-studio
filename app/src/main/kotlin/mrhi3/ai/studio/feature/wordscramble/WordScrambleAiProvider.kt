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

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mrhi3.ai.studio.feature.chat.ChatViewModel
import java.util.concurrent.CancellationException

/**
 * AI를 사용하여 단어를 가져오는 클래스
 */
class WordScrambleAiProvider(private val chatViewModel: ChatViewModel) {
    private val TAG = "WordScrambleAiProvider"

    /**
     * AI로부터 랜덤 단어를 가져옵니다.
     *
     * @param maxLength 단어의 최대 길이 (기본값: 7)
     * @return 가져온 단어 또는 실패 시 기본 단어
     */
    suspend fun getRandomWord(maxLength: Int = 7): String {
        return try {
            withContext(Dispatchers.IO) {
                val prompt = buildPrompt(maxLength)
                val response = sendPromptToAi(prompt)

                // 응답에서 단어 추출
                extractWordFromResponse(response, maxLength)
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "AI로부터 단어를 가져오는 중 오류 발생", e)
            getDefaultWord()
        }
    }

    /**
     * AI에 보낼 프롬프트를 생성합니다.
     */
    private fun buildPrompt(maxLength: Int): String {
        return """
            Word Scramble 게임을 위한 랜덤 단어를 제공해주세요.
            조건:
            1. 영어 단어여야 합니다.
            2. 길이는 최대 $maxLength 글자여야 합니다.
            3. 일반적으로 알려진, 흔한 단어여야 합니다.
            4. 특수문자, 숫자, 공백이 없어야 합니다.
            5. 응답은 단어 하나만 제공해주세요.
            6. 모든 글자는 대문자로 작성해주세요.
        """.trimIndent()
    }

    /**
     * ChatViewModel을 사용하여 AI에 프롬프트를 보내고 응답을 받습니다.
     */
    private suspend fun sendPromptToAi(prompt: String): String {
        try {
            // 메시지 전송
            chatViewModel.sendMessage(prompt)

            // 응답 기다리기 (최대 5초)
            var attempts = 0
            val maxAttempts = 10
            var response = ""

            while (attempts < maxAttempts) {
                // 마지막 메시지가 모델의 응답인지 확인
                val messages = chatViewModel.uiState.value.messages
                val lastMessage = messages.lastOrNull()

                if (lastMessage != null &&
                    lastMessage.participant == mrhi3.ai.studio.feature.chat.Participant.MODEL &&
                    !lastMessage.isPending) {
                    response = lastMessage.text
                    break
                }

                // 0.5초 대기
                kotlinx.coroutines.delay(500)
                attempts++
            }

            return response
        } catch (e: Exception) {
            Log.e(TAG, "AI 응답을 받는 중 오류 발생", e)
            throw e
        }
    }

    /**
     * AI 응답에서 유효한 단어를 추출합니다.
     */
    private fun extractWordFromResponse(response: String, maxLength: Int): String {
        // 줄바꿈과 특수문자 제거 후 단어 추출
        val words = response
            .replace("[^A-Za-z\\s]".toRegex(), "")
            .split("\\s+".toRegex())
            .filter { it.isNotEmpty() && it.length <= maxLength }

        // 첫 번째 유효한 단어 반환 또는 기본 단어
        return words.firstOrNull()?.uppercase() ?: getDefaultWord()
    }

    /**
     * AI 응답 실패 시 제공할 기본 단어 목록
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
}