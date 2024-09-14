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

package com.github.panpf.zoomimage.sketch

import com.github.panpf.sketch.BitmapImage
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.asImage
import com.github.panpf.sketch.cache.ImageCacheValue
import com.github.panpf.zoomimage.subsampling.AndroidTileBitmap
import com.github.panpf.zoomimage.subsampling.BitmapFrom
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.subsampling.TileBitmapCache

/**
 * Implement [TileBitmapCache] based on Sketch on Android platforms
 *
 * @see com.github.panpf.zoomimage.core.sketch.android.test.SketchTileBitmapCacheTest
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class SketchTileBitmapCache actual constructor(
    private val sketch: Sketch,
) : TileBitmapCache {

    actual override fun get(key: String): TileBitmap? {
        val cacheValue = sketch.memoryCache[key] ?: return null
        cacheValue as ImageCacheValue
        val image = cacheValue.image as BitmapImage
        return AndroidTileBitmap(image.bitmap, key, BitmapFrom.MEMORY_CACHE)
    }

    actual override fun put(
        key: String,
        tileBitmap: TileBitmap,
        imageUrl: String,
        imageInfo: ImageInfo,
    ): TileBitmap? {
        tileBitmap as AndroidTileBitmap
        val bitmap = tileBitmap.bitmap!!
        val cacheValue = ImageCacheValue(bitmap.asImage())
        sketch.memoryCache.put(key, cacheValue)
        return null
    }
}