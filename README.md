# mrhi3-ai-studio

구글 AI 샘플을 활용한 프로젝트입니다.

## 📌 API Key 등록
`local.properties` 파일에 API 키를 등록하세요.
```properties
apikey=발급 받은 키
```

## 🚀 메인 프로세스
### MainActivity (`mrhi3.ai.studio`)
지정된 `routeId`를 통해 클릭 이벤트로 화면을 전환합니다.

```kotlin
val navController = rememberNavController()

NavHost(navController = navController, startDestination = "menu") {
    composable("menu") {
        MenuScreen(onItemClicked = { routeId ->
            navController.navigate(routeId)
        })
    }
    composable("summarize") { SummarizeRoute() }
    composable("photo_reasoning") { PhotoReasoningRoute() }
    composable("chat") { ChatRoute() }
}
```

---

## 📖 Summarize 기능
### SummarizeScreen (`mrhi3.ai.studio.feature.text`)
- `routeId`별 화면을 전환하며, 클릭 이벤트(`onSummarizeClicked`)를 지정합니다.

```kotlin
@Composable
internal fun SummarizeRoute(
    summarizeViewModel: SummarizeViewModel = viewModel(factory = GenerativeViewModelFactory)
) {
    val summarizeUiState by summarizeViewModel.uiState.collectAsState()
    
    SummarizeScreen(summarizeUiState, onSummarizeClicked = { inputText ->
        summarizeViewModel.summarizeStreaming(inputText)
    })
}
```

#### 📌 클릭 이벤트 (`onSummarizeClicked`)
```kotlin
TextButton(
    onClick = {
        if (textToSummarize.isNotBlank()) {
            onSummarizeClicked(textToSummarize)
        }
    },
    modifier = Modifier
        .padding(end = 16.dp, bottom = 16.dp)
        .align(Alignment.End)
) {
    Text(stringResource(R.string.action_go))
}
```

---

### 🎯 SummarizeViewModel (`mrhi3.ai.studio.feature.text`)
클릭 이벤트(`onSummarizeClicked`)에서 호출하는 함수로, AI에 요약을 요청합니다.

```kotlin
fun summarizeStreaming(inputText: String) {
    _uiState.value = SummarizeUiState.Loading
    val prompt = "Summarize the following text for me: $inputText"

    viewModelScope.launch {
        try {
            var outputContent = ""
            generativeModel.generateContentStream(prompt)
                .collect { response ->
                    outputContent += response.text
                    _uiState.value = SummarizeUiState.Success(outputContent)
                }
        } catch (e: Exception) {
            _uiState.value = SummarizeUiState.Error(e.localizedMessage ?: "")
        }
    }
}
```

