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

package com.github.panpf.sketch.target

import android.widget.RemoteViews
import androidx.annotation.IdRes
import androidx.core.graphics.drawable.toBitmap
import com.github.panpf.sketch.BitmapImage
import com.github.panpf.sketch.DrawableImage
import com.github.panpf.sketch.Image
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.request.ImageOptions
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.sketch.request.LifecycleResolver
import com.github.panpf.sketch.request.Listener
import com.github.panpf.sketch.request.ProgressListener
import com.github.panpf.sketch.request.internal.OneShotRequestManager
import com.github.panpf.sketch.request.internal.RemoteViewsDelegate
import com.github.panpf.sketch.request.internal.RequestDelegate
import com.github.panpf.sketch.request.internal.RequestManager
import com.github.panpf.sketch.resize.ScaleDecider
import kotlinx.coroutines.Job

/**
 * Set Drawable to RemoteViews
 *
 * @see com.github.panpf.sketch.view.core.test.target.RemoteViewsTargetTest
 */
class RemoteViewsTarget constructor(
    private val remoteViews: RemoteViews,
    @IdRes private val imageViewId: Int,
    private val onUpdated: () -> Unit,
) : Target {

    private val requestManager = OneShotRequestManager()

    override fun onStart(sketch: Sketch, request: ImageRequest, placeholder: Image?) =
        setDrawable(request, placeholder)

    override fun onSuccess(
        sketch: Sketch,
        request: ImageRequest,
        result: ImageResult.Success,
        image: Image
    ) = setDrawable(request, image)

    override fun onError(
        sketch: Sketch,
        request: ImageRequest,
        error: ImageResult.Error,
        image: Image?
    ) = setDrawable(request, image)

    private fun setDrawable(request: ImageRequest, result: Image?) {
        if (result != null || request.allowNullImage == true) {
            val bitmap = when (result) {
                is BitmapImage -> result.bitmap
                is DrawableImage -> result.drawable.toBitmap()
                else -> null
            }
            remoteViews.setImageViewBitmap(imageViewId, bitmap)
            onUpdated()
        }
    }


    override fun getRequestManager(): RequestManager = requestManager

    override fun newRequestDelegate(
        sketch: Sketch,
        initialRequest: ImageRequest,
        job: Job
    ): RequestDelegate = RemoteViewsDelegate(sketch, initialRequest, this, job)


    override fun getListener(): Listener? = null

    override fun getProgressListener(): ProgressListener? = null

    override fun getLifecycleResolver(): LifecycleResolver? = null


    override fun getScaleDecider(): ScaleDecider? = null

    override fun getImageOptions(): ImageOptions? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as RemoteViewsTarget
        if (remoteViews != other.remoteViews) return false
        if (imageViewId != other.imageViewId) return false
        if (onUpdated != other.onUpdated) return false
        return true
    }

    override fun hashCode(): Int {
        var result = remoteViews.hashCode()
        result = 31 * result + imageViewId
        result = 31 * result + onUpdated.hashCode()
        return result
    }

    override fun toString(): String {
        return "RemoteViewsDisplayTarget(remoteViews=$remoteViews, imageViewId=$imageViewId, onUpdated=$onUpdated)"
    }
}