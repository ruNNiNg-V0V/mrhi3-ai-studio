

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mrhi3.ai.studio.wordscramble.WordScrambleData
import mrhi3.ai.studio.wordscramble.WordScrambleGame
import mrhi3.ai.studio.firebase.Source
import mrhi3.ai.studio.firebase.getData
import mrhi3.ai.studio.multiChoice.CountryOptions
import mrhi3.ai.studio.multiChoice.MultiChoiceGame

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

    if (isMultiChoice) {
        val gameSource = getSource()
        if (gameSource != null) {
            MultiChoiceGame(
                "GameList",
                CountryOptions(
                    q = gameSource.q,
                    k = gameSource.k,
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
                    SW = gameSource.q,
                    CW = gameSource.k
                )
            )
        }
        return
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
                Column(
                    modifier = Modifier
                        .padding(all = 16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = source.id,
                        style = MaterialTheme.typography.titleMedium
                    )

                    when (source.cate) {
                        "MultiChoice" -> {
                            Text(
                                text = "선택지: " + (source.choices?.joinToString(", ") ?: ""),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            TextButton(
                                onClick = {
                                    saveSource(source)
                                    isMultiChoice = true
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(text = stringResource(R.string.action_try))
                            }
                        }
                        "WordScramble" -> {
                            Text(
                                text = "섞인 단어: ${source.q}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            TextButton(
                                onClick = {
                                    saveSource(source)
                                    isWordScramble = true
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(text = stringResource(R.string.action_try))
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
}