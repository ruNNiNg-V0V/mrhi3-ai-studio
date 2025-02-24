package mrhi3.ai.studio.feature.combination

data class DataCombinationGame(
    val question: String,
    val keyword: String,
    val choices: List<String>,
    val hints: List<String>,
    val onAnswer1Click: () -> Unit,
    val onAnswer2Click: () -> Unit,
    val onAnswer3Click: () -> Unit
)

