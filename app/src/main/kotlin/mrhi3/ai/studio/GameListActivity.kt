package mrhi3.ai.studio

import android.os.Bundle
import androidx.activity.ComponentActivity
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

class GameListActivity: ComponentActivity() {
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
                        setTopAppBar()
                    }
                }
            }
        }
    }

    @Preview
    @Composable
    fun prev(){
    // 테마
        GenerativeAISample {
            // 최상위 뷰
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column {
                    setTopAppBar()
                }
            }
        }
    }
}