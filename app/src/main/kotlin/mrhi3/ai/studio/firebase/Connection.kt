package mrhi3.ai.studio.firebase

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class Connection {

    val auth = Firebase.auth

    fun signIn(id: String, pw: String) {
        auth.signInWithEmailAndPassword(
            id,
            pw
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("성공", "로그인")
                // 연결 성공 후 파이어스토어 데이터 불러오기
                readData()
            } else {
                // 실패 알림
                Log.d("error", "연결 실패")
            }
        }
    }

    fun readData() {
        val db = FirebaseFirestore.getInstance()

        db.collection("news")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    println("${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }
}