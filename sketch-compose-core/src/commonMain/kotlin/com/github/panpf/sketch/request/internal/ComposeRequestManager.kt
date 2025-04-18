/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.sketch.request.internal

import com.github.panpf.sketch.util.RememberedCounter

/**
 * Compose version of the request manager
 *
 * @see com.github.panpf.sketch.compose.core.common.test.request.internal.ComposeRequestManagerTest
 */
class ComposeRequestManager : BaseRequestManager() {

    internal val rememberedCounter: RememberedCounter = RememberedCounter()

    fun onRemembered() {
        if (!rememberedCounter.remember()) return
    }

    fun onForgotten() {
        if (!rememberedCounter.forget()) return

        currentRequestDelegate?.dispose()
        callbackAttachedState()
    }

    override fun isAttached(): Boolean = rememberedCounter.isRemembered
}