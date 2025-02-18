@file:OptIn(ExperimentalMaterial3Api::class)

package mrhi3.ai.studio.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import mrhi3.ai.studio.R
import mrhi3.ai.studio.firebase.Connection

@Composable
fun setTopAppBar() {

    val context = LocalContext.current
    val appName = context.getString(R.string.app_name)

    TopAppBar(
        title = { Text(appName) },
        navigationIcon = {
            IconButton(onClick = {
            }) {
                val desc = context.getString(R.string.arrowBack)
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = desc
                )
            }
        },
        actions = {
            IconButton(onClick = {
                // 파이어베이스 '연결' 클래스
                val connection = Connection()
                // 사용자 정보가 없다면 로그인할 것
                if (connection.auth.currentUser == null) {
                    val id = context.getString(R.string.id)
                    val pw = context.getString(R.string.pw)
                    connection.signIn(id, pw)
                } else {
                    // 파이어스토어 데이터 불러오기
                    connection.readData()
                }
            }) {
                val icon = R.drawable.firebase_full_color
                val desc = context.getString(R.string.firebase)
                getIcon(icon, desc)
            }
        }
    )
}

@Composable
fun getIcon(icon: Int, desc: String) {
    Icon(
        painter = painterResource(id = icon),
        contentDescription = desc
    )
}