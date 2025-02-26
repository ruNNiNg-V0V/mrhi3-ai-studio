package mrhi3.ai.studio.feature.combination

import com.google.gson.Gson
import android.util.Log

data class DataCombinationGame(
    val q: String,
    val k: String,
    val c: List<String>,
    val h: List<String>
)
/*var storedData: DataCombinationGame? = null*/

fun messageToGson(gameSource: CombinationMessage): DataCombinationGame {
    val comMessage = gameSource
    val jsonResponse = comMessage.text

    /*```json
    {
    "q": "나무 + ? -> 장구",
    "k": "가죽",
    "c": ["가죽", "돌", "종이"],
    "h": ["종물의 피부", "질긴 재질.", "가방 재료"]
    }
    ```*/
    val cutEnd = removeSurroundingQuotes(jsonResponse)
    val combinationFormatData = parseJsonResponse(cutEnd)
    Log.d("MyTag", combinationFormatData.toString())
    return combinationFormatData
}

fun parseJsonResponse(jsonResponse: String): DataCombinationGame {
    val gson = Gson()
    return gson.fromJson(jsonResponse, DataCombinationGame::class.java)
}

fun removeSurroundingQuotes(input: String): String {
    return input.replace("```json", "").replace("```", "")
}

