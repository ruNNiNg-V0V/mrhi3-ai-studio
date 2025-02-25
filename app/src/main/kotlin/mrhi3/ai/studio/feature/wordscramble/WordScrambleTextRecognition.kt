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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 손글씨 이미지에서 텍스트를 인식하는 유틸리티 클래스
 */
class WordScrambleTextRecognition(context: Context) {
    private val TAG = "WordScrambleTextRecognition"

    // 한국어 인식기 생성
    private val recognizer: TextRecognizer = TextRecognition.getClient(
        KoreanTextRecognizerOptions.Builder().build()
    )

    /**
     * 비트맵 이미지에서 텍스트를 인식하는 메서드
     * 코루틴을 사용하여 비동기 작업을 동기식으로 처리
     *
     * @param bitmap 인식할 이미지
     * @return 인식된 텍스트 문자열
     */
    suspend fun recognizeText(bitmap: Bitmap): String {
        return suspendCancellableCoroutine { continuation ->
            try {
                // InputImage 생성
                val image = InputImage.fromBitmap(bitmap, 0)

                // 이미지 처리 요청
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        // 전체 텍스트 추출
                        val recognizedText = visionText.text.trim()
                        Log.d(TAG, "인식된 텍스트: $recognizedText")

                        // 인식된 텍스트가 비어있지 않으면 첫 번째 단어만 사용
                        val result = if (recognizedText.isNotEmpty()) {
                            // 공백을 기준으로 첫 번째 단어만 추출
                            recognizedText.split("\\s+".toRegex()).firstOrNull() ?: ""
                        } else {
                            ""
                        }

                        continuation.resume(result)
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "텍스트 인식 실패", e)
                        continuation.resumeWithException(e)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "텍스트 인식 중 오류 발생", e)
                continuation.resumeWithException(e)
            }

            // 취소 시 리소스 해제
            continuation.invokeOnCancellation {
                // 필요한 경우 리소스 정리
            }
        }
    }

    /**
     * 인식된 텍스트를 원본 단어와 비교하여 일치하는지 확인
     *
     * @param recognizedText 인식된 텍스트
     * @param originalWord 원본 단어
     * @return 일치 여부
     */
    fun isMatchingAnswer(recognizedText: String, originalWord: String): Boolean {
        // 대소문자 구분 없이 비교
        return recognizedText.equals(originalWord, ignoreCase = true)
    }

    /**
     * 리소스 해제
     */
    fun close() {
        recognizer.close()
    }
}