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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class MenuItem(
    val routeId: String,
    val titleResId: Int,
    val descriptionResId: Int,
    val iconResId: Int = R.drawable.firebase_full_color, // 기본 아이콘, 나중에 실제 이미지로 대체
    val headerColor: Color = Color(0xFF6650a4) // 기본 헤더 색상
)

val menuItems = listOf(
    MenuItem(
        "MultiChoice",
        R.string.menu_MultiChoice_title,
        R.string.menu_MultiChoice_description,
        R.drawable.game1,
        Color(0xFF441752)
    ),
    MenuItem(
        "WordScramble",
        R.string.menu_WordScramble_title,
        R.string.menu_WordScramble_description,
        R.drawable.game3,
        Color(0xFF9B7EBD)
    ),
    MenuItem(
        "Combination",
        R.string.menu_Combination_title,
        R.string.menu_Combination_description,
        R.drawable.game2,
        Color(0xFF7E60BF)
    ),
    MenuItem(
        "MatchingCards",
        R.string.menu_MatchingCards_title,
        R.string.menu_MatchingCards_description,
        R.drawable.firebase_full_color,
        Color(0xFF433878)
    )
)

@Composable
fun MenuScreen(
    onItemClicked: (String) -> Unit = { }
) {
    LazyColumn(
        Modifier
            .padding(top = 16.dp, bottom = 16.dp)
    ) {
        items(menuItems) { menuItem ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 제목 부분
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(menuItem.headerColor)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(menuItem.titleResId),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 22.sp
                        )
                    }

                    // 설명과 썸네일
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 게임 썸네일
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))  // 모서리를 둥글게 만들고 싶다면 유지
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = menuItem.iconResId),
                                contentDescription = stringResource(menuItem.titleResId),
                                modifier = Modifier
                                    .fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))

                        // 게임 설명
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(menuItem.descriptionResId),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // 버튼 스타일링
                            Button(
                                onClick = { onItemClicked(menuItem.routeId) },
                                modifier = Modifier.align(Alignment.End),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = menuItem.headerColor,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.action_try),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun MenuScreenPreview() {
    MenuScreen()
}