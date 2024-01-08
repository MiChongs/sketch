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
package com.github.panpf.sketch.extensions.core.test.request

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.sketch.request.Depth.LOCAL
import com.github.panpf.sketch.request.Depth.NETWORK
import com.github.panpf.sketch.request.DepthException
import com.github.panpf.sketch.request.ImageOptions
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.SAVE_CELLULAR_TRAFFIC_KEY
import com.github.panpf.sketch.request.ignoreSaveCellularTraffic
import com.github.panpf.sketch.request.isCausedBySaveCellularTraffic
import com.github.panpf.sketch.request.isDepthFromSaveCellularTraffic
import com.github.panpf.sketch.request.isIgnoredSaveCellularTraffic
import com.github.panpf.sketch.request.isSaveCellularTraffic
import com.github.panpf.sketch.request.saveCellularTraffic
import com.github.panpf.sketch.resources.AssetImages
import com.github.panpf.sketch.test.singleton.getTestContextAndSketch
import com.github.panpf.sketch.test.utils.toRequestContext
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SaveCellularTrafficExtensionsTest {

    @Test
    fun testSaveCellularTraffic() {
        val (context, sketch) = getTestContextAndSketch()

        ImageRequest(context, "http://sample.com/sample.jpeg").apply {
            Assert.assertFalse(isSaveCellularTraffic)
        }

        ImageRequest(context, "http://sample.com/sample.jpeg") {
            saveCellularTraffic()
        }.apply {
            Assert.assertTrue(isSaveCellularTraffic)
        }
        ImageRequest(context, "http://sample.com/sample.jpeg") {
            saveCellularTraffic(false)
        }.apply {
            Assert.assertFalse(isSaveCellularTraffic)
        }

        ImageRequest(context, "http://sample.com/sample.jpeg") {
            saveCellularTraffic()
        }.apply {
            Assert.assertTrue(isSaveCellularTraffic)
        }
        ImageRequest(context, "http://sample.com/sample.jpeg") {
            saveCellularTraffic(false)
        }.apply {
            Assert.assertFalse(isSaveCellularTraffic)
        }

        ImageOptions().apply {
            Assert.assertFalse(isSaveCellularTraffic)
        }

        ImageOptions {
            saveCellularTraffic()
        }.apply {
            Assert.assertTrue(isSaveCellularTraffic)
        }
        ImageOptions {
            saveCellularTraffic(false)
        }.apply {
            Assert.assertFalse(isSaveCellularTraffic)
        }

        val key1 = ImageRequest(context, AssetImages.svg.uri).toRequestContext(sketch).key
        val key2 = ImageRequest(context, AssetImages.svg.uri) {
            saveCellularTraffic()
        }.toRequestContext(sketch).key
        Assert.assertNotEquals(key1, key2)

        val cacheKey1 = ImageRequest(context, AssetImages.svg.uri).toRequestContext(sketch).cacheKey
        val cacheKey2 = ImageRequest(context, AssetImages.svg.uri) {
            saveCellularTraffic(true)
        }.toRequestContext(sketch).cacheKey
        Assert.assertEquals(cacheKey1, cacheKey2)
    }

    @Test
    fun testIgnoreSaveCellularTraffic() {
        val (context, sketch) = getTestContextAndSketch()

        ImageRequest(context, "http://sample.com/sample.jpeg").apply {
            Assert.assertFalse(isIgnoredSaveCellularTraffic)
        }

        ImageRequest(context, "http://sample.com/sample.jpeg") {
            ignoreSaveCellularTraffic()
        }.apply {
            Assert.assertTrue(isIgnoredSaveCellularTraffic)
        }
        ImageRequest(context, "http://sample.com/sample.jpeg") {
            ignoreSaveCellularTraffic(false)
        }.apply {
            Assert.assertFalse(isIgnoredSaveCellularTraffic)
        }

        ImageRequest(context, "http://sample.com/sample.jpeg") {
            ignoreSaveCellularTraffic()
        }.apply {
            Assert.assertTrue(isIgnoredSaveCellularTraffic)
        }
        ImageRequest(context, "http://sample.com/sample.jpeg") {
            ignoreSaveCellularTraffic(false)
        }.apply {
            Assert.assertFalse(isIgnoredSaveCellularTraffic)
        }

        ImageOptions().apply {
            Assert.assertFalse(isIgnoredSaveCellularTraffic)
        }

        ImageOptions {
            ignoreSaveCellularTraffic()
        }.apply {
            Assert.assertTrue(isIgnoredSaveCellularTraffic)
        }
        ImageOptions {
            ignoreSaveCellularTraffic(false)
        }.apply {
            Assert.assertFalse(isIgnoredSaveCellularTraffic)
        }

        val key1 = ImageRequest(context, AssetImages.svg.uri).toRequestContext(sketch).key
        val key2 = ImageRequest(context, AssetImages.svg.uri) {
            ignoreSaveCellularTraffic()
        }.toRequestContext(sketch).key
        Assert.assertNotEquals(key1, key2)

        val cacheKey1 = ImageRequest(context, AssetImages.svg.uri).toRequestContext(sketch).cacheKey
        val cacheKey2 = ImageRequest(context, AssetImages.svg.uri) {
            ignoreSaveCellularTraffic(true)
        }.toRequestContext(sketch).cacheKey
        Assert.assertEquals(cacheKey1, cacheKey2)
    }

    @Test
    fun testSetDepthFromSaveCellularTraffic() {
        val (context, sketch) = getTestContextAndSketch()

        ImageRequest(context, "http://sample.com/sample.jpeg").apply {
            Assert.assertFalse(isDepthFromSaveCellularTraffic)
        }

        ImageRequest(context, "http://sample.com/sample.jpeg") {
            depth(NETWORK, SAVE_CELLULAR_TRAFFIC_KEY)
        }.apply {
            Assert.assertTrue(isDepthFromSaveCellularTraffic)
        }
        ImageRequest(context, "http://sample.com/sample.jpeg") {
            depth(NETWORK, "$SAVE_CELLULAR_TRAFFIC_KEY:error")
        }.apply {
            Assert.assertFalse(isDepthFromSaveCellularTraffic)
        }

        ImageRequest(context, "http://sample.com/sample.jpeg") {
            depth(NETWORK, SAVE_CELLULAR_TRAFFIC_KEY)
        }.apply {
            Assert.assertTrue(isDepthFromSaveCellularTraffic)
        }
        ImageRequest(context, "http://sample.com/sample.jpeg") {
            depth(NETWORK, "$SAVE_CELLULAR_TRAFFIC_KEY:error")
        }.apply {
            Assert.assertFalse(isDepthFromSaveCellularTraffic)
        }

        ImageOptions().apply {
            Assert.assertFalse(isDepthFromSaveCellularTraffic)
        }

        ImageOptions {
            depth(NETWORK, SAVE_CELLULAR_TRAFFIC_KEY)
        }.apply {
            Assert.assertTrue(isDepthFromSaveCellularTraffic)
        }
        ImageOptions {
            depth(NETWORK, "$SAVE_CELLULAR_TRAFFIC_KEY:error")
        }.apply {
            Assert.assertFalse(isDepthFromSaveCellularTraffic)
        }

        val key1 = ImageRequest(context, AssetImages.svg.uri).toRequestContext(sketch).key
        val key2 = ImageRequest(context, AssetImages.svg.uri) {
            depth(NETWORK, SAVE_CELLULAR_TRAFFIC_KEY)
        }.toRequestContext(sketch).key
        Assert.assertNotEquals(key1, key2)

        val cacheKey1 = ImageRequest(context, AssetImages.svg.uri).toRequestContext(sketch).cacheKey
        val cacheKey2 = ImageRequest(context, AssetImages.svg.uri) {
            depth(NETWORK, SAVE_CELLULAR_TRAFFIC_KEY)
        }.toRequestContext(sketch).cacheKey
        Assert.assertEquals(cacheKey1, cacheKey2)
    }

    @Test
    fun testIsCausedBySaveCellularTraffic() {
        val context = InstrumentationRegistry.getInstrumentation().context

        ImageRequest(context, "http://sample.com/sample.jpeg") {
            depth(LOCAL, SAVE_CELLULAR_TRAFFIC_KEY)
        }.apply {
            Assert.assertTrue(isCausedBySaveCellularTraffic(this, DepthException("")))
        }

        ImageRequest(context, "http://sample.com/sample.jpeg") {
            depth(LOCAL, SAVE_CELLULAR_TRAFFIC_KEY)
        }.apply {
            Assert.assertFalse(isCausedBySaveCellularTraffic(this, Exception("")))
        }

        ImageRequest(context, "http://sample.com/sample.jpeg") {
            depth(NETWORK, SAVE_CELLULAR_TRAFFIC_KEY)
        }.apply {
            Assert.assertFalse(isCausedBySaveCellularTraffic(this, DepthException("")))
        }

        ImageRequest(context, "http://sample.com/sample.jpeg") {
            depth(LOCAL)
        }.apply {
            Assert.assertFalse(isCausedBySaveCellularTraffic(this, DepthException("")))
        }
    }
}