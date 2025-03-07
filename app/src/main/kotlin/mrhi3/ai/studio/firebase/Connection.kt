package mrhi3.ai.studio.firebase

import MatchingCards
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await
import mrhi3.ai.studio.GameListActivity
import mrhi3.ai.studio.R
import mrhi3.ai.studio.combination.CombinationData
import mrhi3.ai.studio.wordscramble.WordScrambleData
import mrhi3.ai.studio.multiChoice.CountryOptions

@Composable
fun showLoading(isLoading: Boolean) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

val auth = Firebase.auth

fun signIn(context: Context) {
    val id = context.getString(R.string.id)
    val pw = context.getString(R.string.pw)
    auth.signInWithEmailAndPassword(
        id,
        pw
    ).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            // 연결 성공
            val intent = Intent(context, GameListActivity::class.java)
            context.startActivity(intent)
        } else {
            // 연결 실패
            Toast.makeText(context, "파이어베이스 연결 실패!!", Toast.LENGTH_SHORT).show()
        }
    }
}

// 파이어베이스 불러오기 예시 데이터 클래스
data class Source(
    val cate: String,
    val id: String? = "",
    val q: String? ="",
    val k: String? ="",
    val choices: List<String>? = listOf(),
    val hints: List<String>? = listOf(),
    val a:List<String>? = listOf(),
    val b:List<String>? = listOf(),
    val c:List<String>? = listOf(),
    val d:List<String>? = listOf(),
    val e:List<String>? = listOf(),
    val f:List<String>? = listOf()
)

val gson = Gson()

@Composable
fun getData(): List<Source> {
    // 응답을 기다리는 동안 로딩 표시
    var isLoading by remember { mutableStateOf(true) }
    showLoading(isLoading)

    val db = FirebaseFirestore.getInstance()

    var sourceList by remember { mutableStateOf(listOf<Source>()) }

    LaunchedEffect(Unit) {
        try {
            // documents가 초기화 될 때 까지 로딩 나타내기
            val documents = db.collection("games").get().await()
            val sources = documents.map { document ->
                val json = gson.toJson(document.data) // 받아온 데이터를 json으로 변환
                val data = gson.fromJson(json, Source::class.java) // json을 Source로 변환
                // document를 Source로 변환
                Source(
                    cate = data.cate,
                    id = data.id,
                    q = data.q,
                    k = data.k,
                    choices = data.choices?: listOf(),
                    hints = data.hints?: listOf(),
                    a = data.a?: listOf(),
                    b = data.b?: listOf(),
                    c = data.c?: listOf(),
                    d = data.d?: listOf(),
                    e = data.e?: listOf(),
                    f = data.f?: listOf()
                )
            }
            if (sources.isNotEmpty()) {
                sourceList = sources
            }
        } catch (e: Exception) {
            println("Error getting documents: $e")
        } finally {
            isLoading = false // 로딩이 완료되면 로딩 상태를 false로 변경
        }
    }
    return sourceList
}

@Composable
fun saveMultiChoice(context: Context, gameData: CountryOptions) {
    // 응답을 기다리는 동안 로딩 표시
    var isLoading by remember { mutableStateOf(true) }
    showLoading(isLoading)

    val db = FirebaseFirestore.getInstance()

    val data = Source(
        cate = "MultiChoice",
        id = gameData.q + "의 수도는",
        q = gameData.q,
        k = gameData.k,
        choices = gameData.choices,
    )

    db.collection("games")
        .add(data)
        .addOnSuccessListener {
            isLoading = false
            Toast.makeText(context, "게임 데이터 저장 성공!!", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener {
            isLoading = false
            Toast.makeText(context, "게임 데이터 저장 실패!!", Toast.LENGTH_SHORT).show()
        }
}

@Composable
fun saveWordScramble(context: Context, gameData: WordScrambleData) {
    // 응답을 기다리는 동안 로딩 표시
    var isLoading by remember { mutableStateOf(true) }
    showLoading(isLoading)

    val db = FirebaseFirestore.getInstance()

    val data = Source(
        cate = "WordScramble",
        id = "단어 맞추기: ${gameData.CW}", // 또는 다른 식별자 사용
        q = gameData.SW, // 섞인 단어
        k = gameData.CW // 정답 단어
    )

    db.collection("games")
        .add(data)
        .addOnSuccessListener {
            isLoading = false
            Toast.makeText(context, "게임 데이터 저장 성공!!", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener {
            Log.e("saveWordScramble", "게임 데이터 저장 실패!!", it)
            isLoading = false
            Toast.makeText(context, "게임 데이터 저장 실패!!", Toast.LENGTH_SHORT).show()
        }
}



@Composable
fun saveMatchingCards(context: Context, dataSource : MatchingCards) {
    // 응답을 기다리는 동안 로딩 표시
    var isLoading by remember { mutableStateOf(true) }
    showLoading(isLoading)

    val db = FirebaseFirestore.getInstance()

    val data = Source(
        cate = "MatchingCards",
        a = dataSource.a,
        b = dataSource.b,
        c = dataSource.c,
        d = dataSource.d,
        e = dataSource.e,
        f = dataSource.f
    )

    db.collection("games")
        .add(data)
        .addOnSuccessListener {
            isLoading = false
            Toast.makeText(context, "게임 데이터 저장 성공!!", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener {
            isLoading = false
            Toast.makeText(context, "게임 데이터 저장 실패!!", Toast.LENGTH_SHORT).show()
        }
}

@Composable
fun saveCombination(context: Context, comGameData: CombinationData) {

    // 응답을 기다리는 동안 로딩 표시
    var isLoading by remember { mutableStateOf(true) }
    showLoading(isLoading)

    val db = FirebaseFirestore.getInstance()

    /**
     * TODO
     *  게임이 구현되면 data 수정
     *  게임별 json을 받을 수 있는 하나의 data class 추가
     *  getData()에서 불러 올 데이터 new -> games 수정할 것
     */

    val data = Source(
        cate = "Combination",
        id = comGameData.q,
        q = comGameData.q,
        k = comGameData.k,
        choices = comGameData.choices,
        hints = comGameData.hints
    )

    db.collection("games")
        .add(data)
        .addOnSuccessListener {
            isLoading = false
            Toast.makeText(context, "게임 데이터 저장 성공!!", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener {
            isLoading = false
            Toast.makeText(context, "게임 데이터 저장 실패!!", Toast.LENGTH_SHORT).show()
        }
}