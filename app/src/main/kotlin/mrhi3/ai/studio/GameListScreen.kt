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

import MatchingCards
import MatchingGame
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mrhi3.ai.studio.combination.CombinationData
import mrhi3.ai.studio.combination.CombinationGame
import mrhi3.ai.studio.firebase.Source
import mrhi3.ai.studio.firebase.getData
import mrhi3.ai.studio.multiChoice.CountryOptions
import mrhi3.ai.studio.multiChoice.MultiChoiceGame
import mrhi3.ai.studio.wordscramble.WordScrambleData
import mrhi3.ai.studio.wordscramble.WordScrambleGame

var theSource: Source? = null

fun saveSource(source: Source) {
    theSource = source
}

fun getSource(): Source? {
    return theSource
}

@Composable
fun readData() {
    sourceScreen(getData())
}


@Composable
fun sourceScreen(sources: List<Source>) {

    var expanded by remember { mutableStateOf(false) }
    var currentText by remember { mutableStateOf("") }

    // 게임 리스트는 이 변수로 나타낸다, 이 변수에 변동이 있으면 리스트에 반영됨
    var filteredSources by remember { mutableStateOf(listOf<Source>()) }

    // 초기 필터 상태
    if (filteredSources.isEmpty()) {
        filteredSources = sources
    }

    // 게임 화면 보여주기 상태
    var isMultiChoice by remember { mutableStateOf(false) }
    var isWordScramble by remember { mutableStateOf(false) }
    var isMatchingCards by remember { mutableStateOf(false) }

    if (isMultiChoice) {
        val gameSource = getSource()
        if (gameSource != null) {
            MultiChoiceGame(
                "GameList",
                CountryOptions(
                    q = gameSource.q!!,
                    k = gameSource.k!!,
                    choices = gameSource.choices ?: listOf()
                )
            )
        }
        return
    }

    if (isWordScramble) {
        val gameSource = getSource()
        if (gameSource != null) {
            WordScrambleGame(
                mode = "GameList",
                gameSource = WordScrambleData(
                    SW = gameSource.q!!,
                    CW = gameSource.k!!
                )
            )
        }
        return
    }

    if (isMatchingCards) {
        val gameSource = getSource()
        if (gameSource != null) {
            MatchingGame(
                mode = "GameList",
                gameSource = MatchingCards(
                    gameSource.a!!,
                    gameSource.b!!,
                    gameSource.c!!,
                    gameSource.d!!,
                    gameSource.e!!,
                    gameSource.f!!
                )
            )
        }
    }

    var isCombination by remember { mutableStateOf(false) }
    if (isCombination) {
        val gameSource = getSource()
        CombinationGame(
            "GameList",
            CombinationData(
                q = gameSource!!.q!!,
                k = gameSource!!.k!!,
                choices = gameSource.choices!!,
                hints = gameSource.hints!!
            )
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(16.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.weight(1f)
        ) {
            val context = LocalContext.current
            val categories = listOf(
                context.getString(R.string.MultiChoice),
                context.getString(R.string.WordScramble),
                context.getString(R.string.MatchingCards),
                context.getString(R.string.Combination)
            )
            TextField(
                value = currentText,
                onValueChange = { currentText = it },
                readOnly = true,
                label = { Text("category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor()
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.exposedDropdownSize()
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            currentText = category
                            expanded = false
                            filteredSources = sources.filter { it.cate == category }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        IconButton(onClick = {
            filteredSources = sources
        }) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Refresh"
            )
        }
    }

    LazyColumn(
        Modifier
            .padding(top = 16.dp, bottom = 16.dp)
    ) {
        items(filteredSources) { source ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {


                val menuItem = menuItems.find { it.routeId == source.cate }

                when (source.cate) {
                    "MultiChoice" -> {

                        // 제목 부분
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(menuItem!!.headerColor)
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
                                    text = source.id!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)

                                )

                                // 버튼 스타일링
                                Button(
                                    onClick = {
                                        saveSource(source)
                                        isMultiChoice = true
                                    },
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

                    "WordScramble" -> {

                        // 제목 부분
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(menuItem!!.headerColor)
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
                                    text = "섞인 단어: ${source.q}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                // 버튼 스타일링
                                Button(
                                    onClick = {
                                        saveSource(source)
                                        isWordScramble = true
                                    },
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

                    "MatchingCards" -> {
                        // 제목 부분
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(menuItem!!.headerColor)
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
                                    text = """
                                    ${source.a},${source.b},${source.c},${source.d},${source.e},${source.f}
                                    """.trimIndent(),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                // 버튼 스타일링
                                Button(
                                    onClick = {
                                        saveSource(source)
                                        isMatchingCards = true
                                    },
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

                    "Combination" -> {

                        // 제목 부분
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(menuItem!!.headerColor)
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
                                    text = "섞인 단어: ${source.q}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                // 버튼 스타일링
                                Button(
                                    onClick = {
                                        saveSource(source)
                                        isCombination = true
                                    },
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

                    else -> {
                        Text(
                            text = "카테고리: ${source.cate}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(
                            onClick = {
                                // 다른 게임 유형 처리
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
}

