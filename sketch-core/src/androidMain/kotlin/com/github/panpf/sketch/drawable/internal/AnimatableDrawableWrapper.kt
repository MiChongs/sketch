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

@file:Suppress("RedundantConstructorKeyword")

package com.github.panpf.sketch.drawable.internal

import android.graphics.drawable.Drawable
import androidx.appcompat.graphics.drawable.DrawableWrapperCompat
import androidx.vectordrawable.graphics.drawable.Animatable2Compat

/**
 * Provide unified Callback support for Animatable2, Animatable2Compat, Animatable
 *
 * @see com.github.panpf.sketch.core.android.test.drawable.internal.AnimatableDrawableWrapperTest
 */
open class AnimatableDrawableWrapper constructor(
    drawable: Drawable,
) : DrawableWrapperCompat(drawable), Animatable2Compat {

    internal var callbackHelper: AnimatableCallbackHelper? = null

    init {
        callbackHelper = AnimatableCallbackHelper(drawable)
    }

    override fun setDrawable(drawable: Drawable?) {
        callbackHelper?.setDrawable(drawable)
        super.setDrawable(drawable)
    }

    override fun registerAnimationCallback(callback: Animatable2Compat.AnimationCallback) {
        callbackHelper?.registerAnimationCallback(callback)
    }

    override fun unregisterAnimationCallback(callback: Animatable2Compat.AnimationCallback): Boolean {
        return callbackHelper?.unregisterAnimationCallback(callback) == true
    }

    override fun clearAnimationCallbacks() {
        callbackHelper?.clearAnimationCallbacks()
    }

    override fun start() {
        callbackHelper?.start()
    }

    override fun stop() {
        callbackHelper?.stop()
    }

    override fun isRunning(): Boolean {
        return callbackHelper?.isRunning == true
    }
}