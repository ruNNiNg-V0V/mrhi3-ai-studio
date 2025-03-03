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
            1. 중복되지 않는 대한민국의 특산물 6 개를 선정한다
            2. 특산물에는 지역명이 들어가지 않는다.
            3. 지역명은 시, 군, 구 중 하나에 해당해야한다. 
            4. 선정된 특산물의 지역명을 가져온다. 
            5. 6짝의 데이터를 A~G의 json으로 만들어서 출력한다 
            6. 답변 예시 {
                A : ["원주시","감자"],
                B : ["제주시","감귤"],
                C : ["영광군","굴비"],
                D : ["나주시","배"],
                E : ["가평시","잣"],
                F : ["신안군","천일렴"]
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
