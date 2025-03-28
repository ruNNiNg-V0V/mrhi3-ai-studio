/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mrhi3.ai.studio

import MatchingGame
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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mrhi3.ai.studio.combination.CombinationGame
import mrhi3.ai.studio.multiChoice.MultiChoiceGame
import mrhi3.ai.studio.ui.theme.GenerativeAISample
import mrhi3.ai.studio.ui.theme.setTopAppBar
import mrhi3.ai.studio.wordscramble.WordScrambleGame


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
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

                        setTopAppBar("Main")

                        // 내비 컨트롤러
                        val navController = rememberNavController()
                        // 내비 빌더
                        NavHost(navController = navController, startDestination = "menu") {
                            // 내비 메뉴 및 클릭 이벤트
                            composable("menu") {
                                MenuScreen(onItemClicked = { routeId ->
                                    // 클릭된 뷰의 routeId에 따라 화면 전환
                                    navController.navigate(routeId)
                                })
                            }
                            composable("MultiChoice") {
                                MultiChoiceGame("Main")
                            }
                            composable("WordScramble") {
                                WordScrambleGame("Main")
                            }
                            composable("Combination") {
                                CombinationGame("Main")
                            }
                            composable("MatchingCards") {
                                MatchingGame("Main")
                            }
                        }
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

