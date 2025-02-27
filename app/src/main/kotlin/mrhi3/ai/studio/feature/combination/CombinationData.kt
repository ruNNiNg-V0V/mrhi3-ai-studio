package mrhi3.ai.studio.feature.combination

data class CombinationData (
    val q: String,
    val k: String,
    val choices: List<String>,
    val hints: List<String>
)

fun BasicSource() : CombinationData {
    val data = CombinationData(
        q = "윷 + ? -> 윷놀이",
        k = "말",
        choices = listOf("의자", "연필", "말"),
        hints = listOf("4개 존재", "놀이 도구", "움직임")
    )
    return data
}