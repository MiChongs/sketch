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
package com.github.panpf.sketch.decode

import android.os.Build
import androidx.annotation.RequiresApi
import com.github.panpf.sketch.ComponentRegistry
import com.github.panpf.sketch.datasource.AssetDataSource
import com.github.panpf.sketch.datasource.BasedFileDataSource
import com.github.panpf.sketch.datasource.ByteArrayDataSource
import com.github.panpf.sketch.datasource.ContentDataSource
import com.github.panpf.sketch.datasource.DataSource
import com.github.panpf.sketch.datasource.ResourceDataSource
import com.github.panpf.sketch.decode.internal.AnimatedImageDecoderDecoder
import com.github.panpf.sketch.decode.internal.ImageFormat
import com.github.panpf.sketch.decode.internal.isAnimatedHeif
import com.github.panpf.sketch.fetch.FetchResult
import com.github.panpf.sketch.request.internal.RequestContext

/**
 * Adds animation heif support by AnimatedImageDrawable
 */
@RequiresApi(Build.VERSION_CODES.R)
fun ComponentRegistry.Builder.supportAnimatedHeif(): ComponentRegistry.Builder = apply {
    addDecoder(HeifAnimatedDecoder.Factory())
}

/**
 * Only the following attributes are supported:
 *
 * * resize.size
 * * resize.precision: It is always LESS_PIXELS or SMALLER_SIZE
 * * colorSpace
 * * repeatCount
 * * animatedTransformation
 * * onAnimationStart
 * * onAnimationEnd
 */
@RequiresApi(Build.VERSION_CODES.R)
class HeifAnimatedDecoder(
    requestContext: RequestContext,
    dataSource: DataSource,
) : AnimatedImageDecoderDecoder(requestContext, dataSource) {

    class Factory : Decoder.Factory {

        override val key: String = "HeifAnimatedDecoder"

        override fun create(
            requestContext: RequestContext,
            fetchResult: FetchResult
        ): Decoder? {
            val dataSource = fetchResult.dataSource
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                && !requestContext.request.disallowAnimatedImage
                && (dataSource is AssetDataSource
                        || dataSource is ResourceDataSource
                        || dataSource is ContentDataSource
                        || dataSource is ByteArrayDataSource
                        || dataSource is BasedFileDataSource)
            ) {
                val imageFormat = ImageFormat.parseMimeType(fetchResult.mimeType)
                if ((imageFormat == null || imageFormat == ImageFormat.HEIC || imageFormat == ImageFormat.HEIF)
                    && fetchResult.headerBytes.isAnimatedHeif()
                ) {
                    return HeifAnimatedDecoder(requestContext, dataSource)
                }
            }
            return null
        }

        override fun toString(): String = "HeifAnimatedDecoder"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }
}