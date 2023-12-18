/*
 * Copyright (C) 2022 panpf <panpfpanpf@outlook.com>
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
package com.github.panpf.sketch.gif.test.request.internal

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.panpf.sketch.decode.GifAnimatedDrawableDecoder
import com.github.panpf.sketch.drawable.SketchAnimatableDrawable
import com.github.panpf.sketch.gif.test.getTestContext
import com.github.panpf.sketch.gif.test.newSketch
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.DisplayResult
import com.github.panpf.sketch.test.utils.TestAssets
import com.github.panpf.sketch.test.utils.TestHttpStack
import com.github.panpf.sketch.test.utils.asOrNull
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DisplayRequestExecuteTest {

    @Test
    fun testDisallowAnimatedImage() {
        if (VERSION.SDK_INT < VERSION_CODES.P) return

        val context = getTestContext()
        val sketch = newSketch {
            components {
                addDrawableDecoder(GifAnimatedDrawableDecoder.Factory())
            }
            httpStack(TestHttpStack(context))
        }
        val imageUri = TestAssets.SAMPLE_ANIM_GIF_URI
        val request = DisplayRequest(context, imageUri)

        request.let { runBlocking { sketch.execute(it) } }
            .asOrNull<DisplayResult.Success>()!!.apply {
                Assert.assertTrue(drawable is SketchAnimatableDrawable)
            }

        request.newDisplayRequest {
            disallowAnimatedImage(false)
        }.let { runBlocking { sketch.execute(it) } }
            .asOrNull<DisplayResult.Success>()!!.apply {
                Assert.assertTrue(drawable is SketchAnimatableDrawable)
            }

        request.newDisplayRequest {
            disallowAnimatedImage(null)
        }.let { runBlocking { sketch.execute(it) } }
            .asOrNull<DisplayResult.Success>()!!.apply {
                Assert.assertTrue(drawable is SketchAnimatableDrawable)
            }

        request.newDisplayRequest {
            disallowAnimatedImage(true)
        }.let { runBlocking { sketch.execute(it) } }
            .asOrNull<DisplayResult.Success>()!!.apply {
                Assert.assertFalse(drawable is SketchAnimatableDrawable)
            }
    }
}