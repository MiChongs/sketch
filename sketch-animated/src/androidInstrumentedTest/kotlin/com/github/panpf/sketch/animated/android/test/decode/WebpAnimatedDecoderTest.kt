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

package com.github.panpf.sketch.animated.android.test.decode

import android.graphics.ColorSpace
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.sketch.ComponentRegistry
import com.github.panpf.sketch.decode.ImageInfo
import com.github.panpf.sketch.decode.WebpAnimatedDecoder
import com.github.panpf.sketch.decode.internal.createInSampledTransformed
import com.github.panpf.sketch.decode.supportAnimatedWebp
import com.github.panpf.sketch.drawable.AnimatableDrawable
import com.github.panpf.sketch.fetch.FetchResult
import com.github.panpf.sketch.getDrawableOrThrow
import com.github.panpf.sketch.images.ResourceImages
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.colorSpace
import com.github.panpf.sketch.request.onAnimationEnd
import com.github.panpf.sketch.request.onAnimationStart
import com.github.panpf.sketch.request.repeatCount
import com.github.panpf.sketch.source.AssetDataSource
import com.github.panpf.sketch.source.DataFrom.LOCAL
import com.github.panpf.sketch.test.singleton.sketch
import com.github.panpf.sketch.test.utils.intrinsicSize
import com.github.panpf.sketch.test.utils.toRequestContext
import com.github.panpf.sketch.util.Size
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
class WebpAnimatedDecoderTest {

    @Test
    fun testSupportAnimatedWebp() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return

        ComponentRegistry.Builder().apply {
            build().apply {
                assertEquals(
                    "ComponentRegistry(" +
                            "fetcherFactoryList=[]," +
                            "decoderFactoryList=[]," +
                            "requestInterceptorList=[]," +
                            "decodeInterceptorList=[]" +
                            ")",
                    toString()
                )
            }

            supportAnimatedWebp()
            build().apply {
                assertEquals(
                    "ComponentRegistry(" +
                            "fetcherFactoryList=[]," +
                            "decoderFactoryList=[WebpAnimatedDecoder]," +
                            "requestInterceptorList=[]," +
                            "decodeInterceptorList=[]" +
                            ")",
                    toString()
                )
            }

            supportAnimatedWebp()
            build().apply {
                assertEquals(
                    "ComponentRegistry(" +
                            "fetcherFactoryList=[]," +
                            "decoderFactoryList=[WebpAnimatedDecoder,WebpAnimatedDecoder]," +
                            "requestInterceptorList=[]," +
                            "decodeInterceptorList=[]" +
                            ")",
                    toString()
                )
            }
        }
    }

    @Test
    fun testFactory() = runTest {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return@runTest

        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = context.sketch
        val factory = WebpAnimatedDecoder.Factory()

        assertEquals("WebpAnimatedDecoder", factory.toString())

        // normal
        ImageRequest(context, ResourceImages.animWebp.uri).let {
            val fetchResult =
                FetchResult(
                    AssetDataSource(context, ResourceImages.animWebp.resourceName),
                    "image/webp"
                )
            factory.create(it.toRequestContext(sketch), fetchResult)
        }.apply {
            assertNotNull(this)
        }

        ImageRequest(context, ResourceImages.animWebp.uri).let {
            val fetchResult =
                FetchResult(AssetDataSource(context, ResourceImages.animWebp.resourceName), null)
            factory.create(it.toRequestContext(sketch), fetchResult)
        }.apply {
            assertNotNull(this)
        }

        // disallowAnimatedImage true
        ImageRequest(context, ResourceImages.animWebp.uri) {
            disallowAnimatedImage()
        }.let {
            val fetchResult =
                FetchResult(AssetDataSource(context, ResourceImages.animWebp.resourceName), null)
            factory.create(it.toRequestContext(sketch), fetchResult)
        }.apply {
            assertNull(this)
        }

        // data error
        ImageRequest(context, ResourceImages.png.uri).let {
            val fetchResult =
                FetchResult(AssetDataSource(context, ResourceImages.png.resourceName), null)
            factory.create(it.toRequestContext(sketch), fetchResult)
        }.apply {
            assertNull(this)
        }

        ImageRequest(context, ResourceImages.animGif.uri).let {
            val fetchResult =
                FetchResult(
                    AssetDataSource(context, ResourceImages.animGif.resourceName),
                    "image/webp"
                )
            factory.create(it.toRequestContext(sketch), fetchResult)
        }.apply {
            assertNull(this)
        }

        // mimeType error
        ImageRequest(context, ResourceImages.animWebp.uri).let {
            val fetchResult = FetchResult(
                AssetDataSource(context, ResourceImages.animWebp.resourceName),
                "image/jpeg",
            )
            factory.create(it.toRequestContext(sketch), fetchResult)
        }.apply {
            assertNotNull(this)
        }

        // Disguised, mimeType; data error
        ImageRequest(context, ResourceImages.png.uri).let {
            val fetchResult =
                FetchResult(
                    AssetDataSource(context, ResourceImages.png.resourceName),
                    "image/webp"
                )
            factory.create(it.toRequestContext(sketch), fetchResult)
        }.apply {
            assertNull(this)
        }
    }

    // TODO test: decodeImageInfo

    @Test
    fun testDecodeDrawable() = runTest {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return@runTest

        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = context.sketch
        val factory = WebpAnimatedDecoder.Factory()

        val request = ImageRequest(context, ResourceImages.animWebp.uri) {
            colorSpace(ColorSpace.Named.SRGB)
            onAnimationEnd { }
            onAnimationStart { }
        }
        val fetchResult = sketch.components.newFetcherOrThrow(
            request
                .toRequestContext(sketch, Size.Empty)
        )
            .let { runBlocking { it.fetch() }.getOrThrow() }
        factory.create(request.toRequestContext(sketch), fetchResult)!!
            .let { runBlocking { it.decode() }.getOrThrow() }.apply {
                assertEquals(ImageInfo(480, 270, "image/webp"), this.imageInfo)
                assertEquals(Size(480, 270), image.getDrawableOrThrow().intrinsicSize)
                assertEquals(LOCAL, this.dataFrom)
                assertNull(this.transformeds)
                val animatedImageDrawable =
                    ((image.getDrawableOrThrow() as AnimatableDrawable).drawable as com.github.panpf.sketch.drawable.ScaledAnimatedImageDrawable).drawable
                assertEquals(-1, animatedImageDrawable.repeatCount)
            }

        val request1 = ImageRequest(context, ResourceImages.animWebp.uri) {
            repeatCount(3)
            size(300, 300)
        }
        val fetchResult1 = sketch.components.newFetcherOrThrow(
            request1
                .toRequestContext(sketch, Size.Empty)
        )
            .let { runBlocking { it.fetch() }.getOrThrow() }
        factory.create(request1.toRequestContext(sketch), fetchResult1)!!
            .let { runBlocking { it.decode().getOrThrow() } }.apply {
                assertEquals(ImageInfo(480, 270, "image/webp"), this.imageInfo)
                assertEquals(Size(240, 135), image.getDrawableOrThrow().intrinsicSize)
                assertEquals(LOCAL, this.dataFrom)
                assertEquals(listOf(createInSampledTransformed(2)), this.transformeds)
                val animatedImageDrawable =
                    ((image.getDrawableOrThrow() as AnimatableDrawable).drawable as com.github.panpf.sketch.drawable.ScaledAnimatedImageDrawable).drawable
                assertEquals(3, animatedImageDrawable.repeatCount)
            }
    }

    @Test
    fun testFactoryEqualsAndHashCode() {
        val element1 = WebpAnimatedDecoder.Factory()
        val element11 = WebpAnimatedDecoder.Factory()
        val element2 = WebpAnimatedDecoder.Factory()

        assertNotSame(element1, element11)
        assertNotSame(element1, element2)
        assertNotSame(element2, element11)

        assertEquals(element1, element1)
        assertEquals(element1, element11)
        assertEquals(element1, element2)
        assertEquals(element2, element11)
        assertNotEquals(element1, null as Any?)
        assertNotEquals(element1, Any())

        assertEquals(element1.hashCode(), element1.hashCode())
        assertEquals(element1.hashCode(), element11.hashCode())
        assertEquals(element1.hashCode(), element2.hashCode())
        assertEquals(element2.hashCode(), element11.hashCode())
    }
}