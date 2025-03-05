package mrhi3.ai.studio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import mrhi3.ai.studio.ui.theme.GenerativeAISample
import mrhi3.ai.studio.ui.theme.setTopAppBar

class GameListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 테마
            GenerativeAISample {
                // 최상위 뷰
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        setTopAppBar("GameList")
                        readData()
                    }
                }
            }
            // 뒤로가기 버튼을 무시하는 코드
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // 뒤로가기 버튼 동작을 막음
                }
            })
        }
    }
}