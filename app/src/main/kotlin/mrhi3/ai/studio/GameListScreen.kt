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

@file:OptIn(ExperimentalMaterial3Api::class)

package mrhi3.ai.studio

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mrhi3.ai.studio.firebase.Source
import mrhi3.ai.studio.firebase.getData

@Composable
fun readData() {
    sourceScreen(getData())
}

@Composable
fun sourceScreen(sources: List<Source>) {

    /**
     * TODO -> 게임 저장 구현 후 진행할 것
     *  드롭다운 메뉴와 새로고침 버튼 추가
     *  드롭다운 메뉴는 리스팅된 게임을 필터링
     *  새로고침은 readData() 다시 호출
     */

    var expanded by remember { mutableStateOf(false) }
    var selectedSource by remember { mutableStateOf<Source?>(null) }
    var currentText by remember { mutableStateOf("") }

    /**
     * onClick에서 Composable 함수를 호출하는 방법
     * Boolean 타입의 remember 변수를 선언한다, 기본 값은 false
     * if(변수명) { 호출할 함수 }
     * 위와 같이 작성 후 onClick 이벤트에서 변수를 true로 변경
     */
    var haveToRefresh by remember { mutableStateOf(false) }

    if (haveToRefresh) {
        readData()
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(16.dp)
    ) {
        // 게임 필터링
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.weight(1f) // Take up available space
        ) {
            TextField(
                value = selectedSource?.id ?: currentText,
                onValueChange = { currentText = it },
                readOnly = true, // Prevent manual text input
                label = { Text("category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor() // Attach to the DropdownMenu
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.exposedDropdownSize()
            ) {
                sources.forEach { source ->
                    DropdownMenuItem(
                        text = { Text(source.id) },
                        onClick = {
                            selectedSource = source
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 새로고침 버튼
        IconButton(onClick = {
            haveToRefresh = true
        }) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Refresh"
            )
        }
    }

    // 게임 리스트
    LazyColumn(
        Modifier
            .padding(top = 16.dp, bottom = 16.dp)
    ) {
        items(sources) { source ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(all = 16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = source.id,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = source.content,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = source.date,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    TextButton(
                        onClick = {
                            //
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(text = source.email)
                    }
                }
            }
        }
    }
}

// TODO 게임이 완성되면 수정 및 적용
data class GameSource(
    val gameId: String,
    val titleResId: Int,
    val descriptionResId: Int
)

@Composable
fun GameListScreen(
    onItemClicked: (String) -> Unit = { }
) {
    val games = listOf(
        GameSource("select4", R.string.menu_summarize_title, R.string.menu_summarize_description),
        GameSource("shuffle", R.string.menu_reason_title, R.string.menu_reason_description),
        GameSource("couple", R.string.menu_chat_title, R.string.menu_chat_description),
        GameSource("mix", R.string.menu_chat_title, R.string.menu_chat_description)
    )
    LazyColumn(
        Modifier
            .padding(top = 16.dp, bottom = 16.dp)
    ) {
        items(games) { gameSource ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(all = 16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(gameSource.titleResId),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(gameSource.descriptionResId),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    TextButton(
                        onClick = {
                            onItemClicked(gameSource.gameId)
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(text = stringResource(R.string.action_try))
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun GameListScreenPreview() {
    readData()
}
