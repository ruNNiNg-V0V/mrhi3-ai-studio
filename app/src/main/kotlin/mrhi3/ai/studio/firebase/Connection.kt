package mrhi3.ai.studio.firebase

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import mrhi3.ai.studio.GameListActivity
import mrhi3.ai.studio.R

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