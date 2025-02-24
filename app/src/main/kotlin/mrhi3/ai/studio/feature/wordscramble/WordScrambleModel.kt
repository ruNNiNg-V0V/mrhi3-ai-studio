package mrhi3.ai.studio.feature.wordscramble

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.google.gson.Gson
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Word Scramble 게임의 데이터 모델
 */
data class WordScrambleModel(
    val id: Long = 0,  // SQLite의 primary key로 사용
    val originalWord: String = "APPLE",
    val scrambledWord: String = "LPAPE",
    val userAnswer: String = "",
    val drawingBitmap: Bitmap? = null,
    val isCorrect: Boolean = false,
    val timestamp: String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
) {
    // JSON 변환을 위한 데이터 클래스
    private data class WordScrambleJson(
        val id: Long,
        val originalWord: String,
        val scrambledWord: String,
        val userAnswer: String,
        val drawingBase64: String?,  // Bitmap을 Base64로 인코딩
        val isCorrect: Boolean,
        val timestamp: String
    )

    /**
     * 모델을 JSON 문자열로 변환
     */
    fun toJson(): String {
        val base64Drawing = drawingBitmap?.let { bitmap ->
            ByteArrayOutputStream().use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
            }
        }

        val jsonModel = WordScrambleJson(
            id = id,
            originalWord = originalWord,
            scrambledWord = scrambledWord,
            userAnswer = userAnswer,
            drawingBase64 = base64Drawing,
            isCorrect = isCorrect,
            timestamp = timestamp
        )

        return Gson().toJson(jsonModel)
    }

    companion object {
        /**
         * JSON 문자열에서 모델 생성
         */
        fun fromJson(json: String): WordScrambleModel {
            val jsonModel = Gson().fromJson(json, WordScrambleJson::class.java)

            val bitmap = jsonModel.drawingBase64?.let { base64String ->
                val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            }

            return WordScrambleModel(
                id = jsonModel.id,
                originalWord = jsonModel.originalWord,
                scrambledWord = jsonModel.scrambledWord,
                userAnswer = jsonModel.userAnswer,
                drawingBitmap = bitmap,
                isCorrect = jsonModel.isCorrect,
                timestamp = jsonModel.timestamp
            )
        }
    }
}

/**
 * 단어 스크램블을 위한 유틸리티 함수
 */
object WordScrambleUtils {
    /**
     * 단어를 무작위로 섞어서 반환합니다.
     * 원본과 같아지지 않도록 합니다.
     */
    fun scrambleWord(word: String): String {
        var scrambled: String
        do {
            val charList = word.toCharArray().toList()
            scrambled = charList.shuffled().joinToString("  ")
        } while (scrambled == word)
        return scrambled
    }

    /**
     * 사용자 입력이 정답인지 확인합니다.
     */
    fun checkAnswer(original: String, answer: String): Boolean {
        return original.equals(answer, ignoreCase = true)
    }
}