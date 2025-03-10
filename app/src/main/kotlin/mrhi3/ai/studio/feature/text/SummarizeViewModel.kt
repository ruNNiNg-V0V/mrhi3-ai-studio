package mrhi3.ai.studio.feature.text

import MatchingCards
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mrhi3.ai.studio.combination.CombinationData
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

    fun getCombinationQize(gameData: CombinationData): Job {
        _uiState.value = SummarizeUiState.Loading

        val prompt = """
1. 주제를 "한국 전통 문화"로 설정합니다.
2. 답변 예시 {
              "q": "${gameData.q}",
              "k": "${gameData.k}",
              "choices": ["${gameData.choices[0]}","${gameData.choices[1]}","${gameData.choices[2]}"],
              "hints": ["${gameData.hints[0]}","${gameData.hints[1]}","${gameData.hints[2]}"]
            }
3. 답변 예시를 보고 음식, 의복, 악기, 놀이 중 가장알맞은 것을 g1에 저장하세요.
4. g2는 g1(음식) 이면 g2(의복), 의복 -> 악기, 악기 -> 놀이, 놀이 -> 음식과 같이 설정합니다. 
5. p는 g2의 하위 범주에 해당하는 구체적 개념으로된 고유명사 단어로 저장합니다.
6. p,ps1,ps2는 서로 다른 단어이고, p를 구성하는 주된 재료 또는 핵심 요소를 각각 ps1, ps2에 저장합니다 (예 사물놀이 -> 북, 꽹과리)
7. 질문(q)에는 'ps1 + ? -> p' 형태로 저장하며, 답변 예시와 같을 수 없습니다. 
8. 키워드(k)에는 ps2 값을 저장하세요.
9. 선택지(choices)에는 3개의 값입니다. k의 값을 포함한 세 개의 항목이며 나머지 두 값은 p와 전혀 관련이 없는 단어여야 합니다.
10. 선택지(choices)에 들어있는 값들을 무작위로 배열해야 합니다.
11. 힌트(hints)에는 ps2의 값의 세 가지 간단한 특징을 포함하여 각각 20자 이내로 리스트 형식으로 작성해야 합니다.
12. 답변은 질문(q), 키워드(k), 선택지(choices), 힌트(hints)를 포함해서 json만 보내세요.
        """.trimIndent()
        Log.d("getCombinationQize", "${gameData.choices}")
        Log.d("getCombinationQize", "${gameData.hints}")

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
