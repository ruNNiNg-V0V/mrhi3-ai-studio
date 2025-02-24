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

package mrhi3.ai.studio.feature.combination

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CombinationViewModel(
    generativeModel: GenerativeModel
) : ViewModel() {
    private val Combination = generativeModel.startChat(
        history = listOf(
            content(role = "userclick") { text("Hello, I have 2 cats in my house.") },
            content(role = "modelreturn") { text("Great to meet you. What would you like to know?") }
        )
    )

    private val _uiState: MutableStateFlow<CombinationUiState> =
        MutableStateFlow(CombinationUiState(Combination.history.map { content ->
            // Map the initial messagescha
            CombinationMessage(
                text = content.parts.first().asTextOrNull() ?: "",
                comparticipant = if (content.role == "userclick") ComParticipant.USERCLICK else ComParticipant.MODELRETURN,
                isPending = false
            )
        }))
    val uiState: StateFlow<CombinationUiState> =
        _uiState.asStateFlow()


    fun sendMessage(userMessage: String) {
        // Add a pending message
        _uiState.value.addMessage(
            CombinationMessage(
                text = userMessage,
                comparticipant = ComParticipant.USERCLICK,
                isPending = true
            )
        )

        viewModelScope.launch {
            try {
                val response = Combination.sendMessage(userMessage)

                _uiState.value.replaceLastPendingMessage()

                response.text?.let { modelResponse ->
                    _uiState.value.addMessage(
                        CombinationMessage(
                            text = modelResponse,
                            comparticipant = ComParticipant.MODELRETURN,
                            isPending = false
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.value.replaceLastPendingMessage()
                _uiState.value.addMessage(
                    CombinationMessage(
                        text = e.localizedMessage,
                        comparticipant = ComParticipant.ERRORABOUT
                    )
                )
            }
        }
    }
}
