@file:OptIn(ExperimentalMaterial3Api::class)

package mrhi3.ai.studio.ui.theme

import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import mrhi3.ai.studio.GameListActivity
import mrhi3.ai.studio.MainActivity
import mrhi3.ai.studio.R
import mrhi3.ai.studio.firebase.auth
import mrhi3.ai.studio.firebase.signIn

@Composable
fun setTopAppBar(mode: String) {
    val context = LocalContext.current
    val appName = context.getString(R.string.app_name)
    TopAppBar(
        title = { Text(appName) },
        navigationIcon = {
            if (mode != "Main") {
                IconButton(onClick = {
                    // 메인으로 이동
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)
                }) {
                    val desc = context.getString(R.string.arrowBack)
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = desc
                    )
                }
            }
        },
        actions = {
            if (mode == "Main") {
                IconButton(onClick = {
                    // 게임리스트로 이동
                    // 이동하기 전 연결 체크
                    if (auth.currentUser == null) {
                        signIn(context)
                    } else {
                        val intent = Intent(context, GameListActivity::class.java)
                        context.startActivity(intent)
                    }
                }) {
                    val icon = painterResource(id = R.drawable.firebase_full_color)
                    val desc = context.getString(R.string.firebase)
                    Icon(
                        painter = icon,
                        contentDescription = desc,
                        // svg 이미지 색상 그대로 설정
                        tint = Color.Unspecified
                    )
                }
            }
        }
    )
}
