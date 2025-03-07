package mrhi3.ai.studio.feature.text

import MatchingCards
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

    fun getMatchingCards(gameSource: MatchingCards): Job {
        _uiState.value = SummarizeUiState.Loading

        // ai에 요청할 명령
        val prompt = """
            1. 중복되지 않는 대한민국의 특산물 6 개를 선정한다
            2. 특산물에는 지역명이 들어가지 않는다.
            3. 지역명은 시, 군, 구 중 하나에 해당해야한다. 
            4. 선정된 특산물의 지역명을 가져온다. 
            5. 6짝의 데이터를 A~G의 json으로 만들어서 출력한다 
            6. 답변 예시 {
                a : ["${gameSource.a[0]}","${gameSource.a[1]}"],
                b : ["${gameSource.b[0]}","${gameSource.b[1]}"],
                c : ["${gameSource.c[0]}","${gameSource.c[1]}"],
                d : ["${gameSource.d[0]}","${gameSource.d[1]}"],
                e : ["${gameSource.e[0]}","${gameSource.e[1]}"],
                f : ["${gameSource.f[0]}","${gameSource.f[1]}"]
            }
            7. 답변은 json만
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
