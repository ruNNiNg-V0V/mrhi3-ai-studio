package mrhi3.ai.studio.feature.combination

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

class CombinationViewModel(
    private val generativeModel: GenerativeModel, // `generativeModel` 객체 전달받음
) : ViewModel() {

    private val Combination = generativeModel.startChat( // `Combination` 초기화
    )

/*    suspend fun sendMessage(userMessage: String): DataCombinationGame? {
        return withContext(Dispatchers.IO) {
            var sD: DataCombinationGame? = null
            try {
                val response = Combination.sendMessage(userMessage)
                response.text?.let { modelResponse ->
                    val result = CombinationMessage(
                        text = modelResponse,
                        comparticipant = ComParticipant.MODELRETURN,
                        isPending = false
                    )
                    sD = messageToGson(result)
                }
            } catch (e: Exception) {
                val exc = CombinationMessage(
                    text = e.localizedMessage,
                    comparticipant = ComParticipant.ERRORABOUT
                )
                sD = messageToGson(exc)
            }
            sD
        }
    }*/
}
