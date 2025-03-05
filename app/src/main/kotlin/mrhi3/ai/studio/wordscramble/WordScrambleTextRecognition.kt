package mrhi3.ai.studio.wordscramble

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 손글씨 이미지에서 텍스트를 인식하는 유틸리티 클래스
 */
class WordScrambleTextRecognition(context: Context) {
    private val TAG = "WordScrambleTextRecognition"

    // 텍스트 인식기 생성 (영문 알파벳 인식용)
    private val recognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

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
                            // 공백을 기준으로 첫 번째 단어만 추출하고 영문자만 필터링
                            recognizedText.split("\\s+".toRegex()).firstOrNull()?.filter { it.isLetter() }?.uppercase() ?: ""
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
     * 리소스 해제
     */
    fun close() {
        recognizer.close()
    }
}