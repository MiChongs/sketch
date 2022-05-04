/*
 * Copyright (C) 2019 panpf <panpfpanpf@outlook.com>
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
package com.github.panpf.sketch.drawable

import android.content.res.Resources
import androidx.annotation.MainThread
import com.github.panpf.sketch.cache.CountBitmap
import com.github.panpf.sketch.datasource.DataFrom
import com.github.panpf.sketch.decode.internal.exifOrientationName

class SketchCountBitmapDrawable constructor(
    resources: Resources,
    private val countBitmap: CountBitmap,
    dataFrom: DataFrom,
) : SketchBitmapDrawable(
    requestKey = countBitmap.requestKey,
    requestUri = countBitmap.imageUri,
    imageInfo = countBitmap.imageInfo,
    imageExifOrientation = countBitmap.exifOrientation,
    dataFrom = dataFrom,
    transformedList = countBitmap.transformedList,
    resources = resources,
    bitmap = countBitmap.bitmap!!
) {

    val isRecycled: Boolean
        get() = countBitmap.isRecycled

    @MainThread
    fun setIsDisplayed(callingStation: String, displayed: Boolean) {
        countBitmap.setIsDisplayed(callingStation, displayed)
    }

    @MainThread
    fun setIsPending(callingStation: String, waitingUse: Boolean) {
        countBitmap.setIsPending(callingStation, waitingUse)
    }

    @MainThread
    fun getPendingCount(): Int {
        return countBitmap.getPendingCount()
    }

    override fun hashCode(): Int {
        return countBitmap.hashCode()
    }

    override fun equals(other: Any?): Boolean =
        other is SketchCountBitmapDrawable
                && countBitmap == other.countBitmap

    override fun toString(): String =
        "SketchCountBitmapDrawable(" +
                imageInfo.toShortString() +
                "," + exifOrientationName(imageExifOrientation) +
                "," + dataFrom +
                "," + bitmapInfo.toShortString() +
                "," + transformedList +
                "," + requestKey +
                ")"
}