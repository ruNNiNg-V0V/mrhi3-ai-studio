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

package mrhi3.ai.studio.feature.wordscramble

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mrhi3.ai.studio.feature.chat.ChatViewModel

/**
 * WordScrambleViewModel을 생성하기 위한 Factory 클래스
 * Context와 ChatViewModel을 ViewModel에 전달하기 위해 사용합니다.
 */
class WordScrambleViewModelFactory(
    private val context: Context,
    private val chatViewModel: ChatViewModel? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WordScrambleViewModel::class.java)) {
            return WordScrambleViewModel(context, chatViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}