package mrhi3.ai.studio.feature.text

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mrhi3.ai.studio.multiChoice.CountryOptions
import mrhi3.ai.studio.wordscramble.WordScrambleData


class SummarizeViewModel(
    private val generativeModel: GenerativeModel
) : ViewModel() {

    private val _uiState: MutableStateFlow<SummarizeUiState> =
        MutableStateFlow(SummarizeUiState.Initial)
    val uiState: StateFlow<SummarizeUiState> =
        _uiState.asStateFlow()

    private var outputContent = ""

    fun getResult() = outputContent

    fun getCountryOptions(gameData: CountryOptions): Job {
        _uiState.value = SummarizeUiState.Loading

        val prompt = """
                    1. 무작위로 나라를 하나 선정
                    2. 선정된 나라를 수도를 포함하여 각기 다른 나라의 수도를 넷 선정
                    3. 답변 예시 {
                        q: "${gameData.q}",
                        k : "${gameData.k}",
                        choices : ${gameData.choices}
                    }
                    4. choices의 개수가 4개여야 한다
                    5. 답변은 json만
        """.trimIndent()

        val job = viewModelScope.launch {
            outputContent = ""
            try {
                generativeModel.generateContentStream(prompt)
                    .collect { response ->
                        outputContent += response.text
                        _uiState.value = SummarizeUiState.Success(outputContent)
                    }
            } catch (e: Exception) {
                _uiState.value = SummarizeUiState.Error(e.localizedMessage ?: "")
            }
        }
        return job
    }

    fun getWordScramble(gameData: WordScrambleData): Job {
        _uiState.value = SummarizeUiState.Loading

        val prompt = """
        Generate a word scramble game data in JSON format with two fields:
        1. CW (Correct Word): the original word that players need to guess
        2. SW (Scrambled Word): a shuffled version of the CW

        Please provide a single English word (5-8 letters) that is common and easy and capital letters.
        IMPORTANT: Return only the JSON data, without any explanation or markdown formatting.
        example:
        {
            "CW": "${gameData.CW}",
            "SW": "${gameData.SW}"
        }
        """.trimIndent()

        val job = viewModelScope.launch {
            outputContent = ""
            try {
                generativeModel.generateContentStream(prompt)
                    .collect { response ->
                        outputContent += response.text
                        _uiState.value = SummarizeUiState.Success(outputContent)
                    }
            } catch (e: Exception) {
                _uiState.value = SummarizeUiState.Error(e.localizedMessage ?: "")
            }
        }
        return job

    }

}
