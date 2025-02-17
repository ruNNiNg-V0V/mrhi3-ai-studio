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

@Composable
fun setTopAppBar() {

    val context = LocalContext.current
    val appName = context.getString(R.string.app_name)

    TopAppBar(
        title = { Text(appName) },
        navigationIcon = {
            IconButton(onClick = {
                // 게임 선택 페이지로
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "뒤로가기"
                )
            }
        },
        actions = {
            IconButton(onClick = {
                // Firebase 페이지로
            }) {
                getFirebaseIcon()
            }
        }
    )
}

@Composable
fun getFirebaseIcon() {
    Icon(
        painter = painterResource(id = R.drawable.firebase_full_color),
        contentDescription = "파이어베이스"
    )
}