package mrhi3.ai.studio.feature.text

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SummarizeViewModel(
    private val generativeModel: GenerativeModel
) : ViewModel() {

    private val _uiState: MutableStateFlow<SummarizeUiState> =
        MutableStateFlow(SummarizeUiState.Initial)
    val uiState: StateFlow<SummarizeUiState> =
        _uiState.asStateFlow()

    private var outputContent = ""

    fun getResult() = outputContent

    fun summarizeStreaming(): Job {
        _uiState.value = SummarizeUiState.Loading

        // ai에 요청할 명령
        val prompt = """
            1. 주제를 "한국 전통 문화"로 설정합니다.
2. 악기, 의복, 음식, 놀이 하위 카테고리 중 하나를 무작위로 선택하세요.
3. 하위 카테고리에 관련된 완성품 단어를 p에 저장하세요.
4. ps1(구성요소), ps2(구성요소)는 p를 구성하는 서로 다른 두 개의 요소. (예 사물놀이 -> 북, 꽹과리)
5. 질문(q)에는 'ps1 + ? = p' 형태로 저장하세요.
6. 키워드(k)에는 ps2 값을 저장하세요.
7. 선택지(choices)에는 k의 값을 포함한 세 개의 항목을 포함하며 나머지 두 값은 p와 전혀 관련이 없는 단어여야 합니다.
8. 선택지(choices)에 들어있는 값들을 무작위로 배열해야 합니다.
9. 힌트(h)에는 ps2의 값의 세 가지 간단한 특징을 포함하여 10자 이내로 작성해야 합니다.
10. 답변 예시 {
              "q": "붓 + ? -> 서예",
              "k": "먹",
              "choices": ["고추장", "책상", "먹"],
              "hints": ["검은색", "갈아서 사용", "글씨 쓰기"]
            }
11. 답변은 질문(q), 키워드(k), 선택지(choices), 힌트(hints)를 포함해서 json만 보내세요.
        """.trimIndent()

        val job = viewModelScope.launch {
            outputContent = ""
            try {
                generativeModel.generateContentStream(prompt)
                    .collect { response ->
                        val responseText = response.text
                        val cutJson = responseText.toString().replace("json", "")
                        val cutResult = cutJson.replace("```", "")
                        outputContent += cutResult
                        _uiState.value = SummarizeUiState.Success(outputContent)
                    }
            } catch (e: Exception) {
                _uiState.value = SummarizeUiState.Error(e.localizedMessage ?: "")
            }
        }
        return job
    }
}
