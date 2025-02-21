package mrhi3.ai.studio.firebase

import android.content.Context
import android.content.Intent
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
import kotlinx.coroutines.tasks.await
import mrhi3.ai.studio.GameListActivity
import mrhi3.ai.studio.R
import com.google.gson.Gson

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
    val id: String,
    val content: String,
    val date: String,
    val email: String
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
            val documents = db.collection("news").get().await()
            val sources = documents.map { document ->
                val id = document.id // document가 json으로 변경되면서 id는 빠짐
                val json = gson.toJson(document.data) // 받아온 데이터를 json으로 변환
                val data = gson.fromJson(json, Source::class.java) // json을 Source로 변환
                // document를 Source로 변환
                Source(
                    id = id,
                    content = data.content,
                    date = data.date,
                    email = data.email
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

