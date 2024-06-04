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
package com.github.panpf.sketch.request

import com.github.panpf.sketch.ComponentRegistry
import com.github.panpf.sketch.cache.CachePolicy
import com.github.panpf.sketch.decode.Decoder
import com.github.panpf.sketch.fetch.Fetcher
import com.github.panpf.sketch.http.HttpHeaders
import com.github.panpf.sketch.http.isNotEmpty
import com.github.panpf.sketch.http.merged
import com.github.panpf.sketch.merged
import com.github.panpf.sketch.resize.FixedSizeResolver
import com.github.panpf.sketch.resize.Precision
import com.github.panpf.sketch.resize.PrecisionDecider
import com.github.panpf.sketch.resize.Scale
import com.github.panpf.sketch.resize.ScaleDecider
import com.github.panpf.sketch.resize.SizeResolver
import com.github.panpf.sketch.state.ErrorStateImage
import com.github.panpf.sketch.state.StateImage
import com.github.panpf.sketch.transition.CrossfadeTransition
import com.github.panpf.sketch.transform.Transformation
import com.github.panpf.sketch.transition.Transition
import com.github.panpf.sketch.util.Size
import com.github.panpf.sketch.util.keyOrNull

const val DEPTH_FROM_KEY = "sketch#depth_from"

/**
 * Build and set the [ImageOptions]
 */
fun ImageOptions(
    configBlock: (ImageOptions.Builder.() -> Unit)? = null
): ImageOptions = ImageOptions.Builder().apply {
    configBlock?.invoke(this)
}.build()

/**
 * Stores parameters required to download, load, display images
 *
 * IMPORTANT: It is necessary to ensure compliance with the consistency principle,
 * that is, the equals() and hashCode() methods of instances created with the same
 * construction parameters return consistent results. This is important in Compose
 */
interface ImageOptions {

    /**
     * The processing depth of the request.
     */
    val depth: Depth?

    /**
     * where does this depth come from
     */
    val depthFrom: String?
        get() = parameters?.value(DEPTH_FROM_KEY)

    /**
     * A map of generic values that can be used to pass custom data to [Fetcher] and [Decoder].
     */
    val parameters: Parameters?


    /**
     * Set headers for http requests
     *
     * @see com.github.panpf.sketch.http.HurlStack.getResponse
     */
    val httpHeaders: HttpHeaders?

    /**
     * Http download cache policy
     *
     * @see com.github.panpf.sketch.fetch.HttpUriFetcher
     */
    val downloadCachePolicy: CachePolicy?


    /**
     * Lazy calculation of resize size. If resize size is null at runtime, size is calculated and assigned to size
     */
    val sizeResolver: SizeResolver?

    /**
     * val finalSize = sizeResolver.size() * sizeMultiplier
     */
    val sizeMultiplier: Float?

    /**
     * Decide what Precision to use with [sizeResolver] to calculate the size of the final Bitmap
     */
    val precisionDecider: PrecisionDecider?

    /**
     * Which part of the original image to keep when [precisionDecider] returns [Precision.EXACTLY] or [Precision.SAME_ASPECT_RATIO]
     */
    val scaleDecider: ScaleDecider?

    /**
     * The list of [Transformation]s to be applied to this request
     */
    val transformations: List<Transformation>?

    /**
     * Disk caching policy for Bitmaps affected by [sizeResolver] or [transformations]
     *
     * @see com.github.panpf.sketch.cache.internal.ResultCacheDecodeInterceptor
     */
    val resultCachePolicy: CachePolicy?


    /**
     * Placeholder image when loading
     */
    val placeholder: StateImage?

    /**
     * Image to display when uri is empty
     */
    val uriEmpty: StateImage?

    /**
     * Image to display when loading fails
     */
    val error: ErrorStateImage?

    /**
     * How the current image and the new image transition
     */
    val transitionFactory: Transition.Factory?

    /**
     * Disallow decode animation image, animations such as gif will only decode their first frame and return BitmapDrawable
     */
    val disallowAnimatedImage: Boolean?

    /**
     * Use ResizeDrawable or ResizePainter to wrap an Image to resize it while drawing, it will act on placeholder, uriEmpty, error and the decoded image
     */
    val resizeOnDraw: Boolean?

    /**
     * Bitmap memory caching policy
     *
     * @see com.github.panpf.sketch.cache.internal.MemoryCacheRequestInterceptor
     */
    val memoryCachePolicy: CachePolicy?


    /**
     * Components that are only valid for the current request
     */
    val componentRegistry: ComponentRegistry?


    /**
     * Create a new [ImageOptions.Builder] based on the current [ImageOptions].
     *
     * You can extend it with a trailing lambda function [configBlock]
     */
    fun newBuilder(
        configBlock: (Builder.() -> Unit)? = null
    ): Builder = Builder(this).apply {
        configBlock?.invoke(this)
    }

    /**
     * Create a new [ImageOptions] based on the current [ImageOptions].
     *
     * You can extend it with a trailing lambda function [configBlock]
     */
    fun newOptions(
        configBlock: (Builder.() -> Unit)? = null
    ): ImageOptions = Builder(this).apply {
        configBlock?.invoke(this)
    }.build()

    /**
     * Merge the current [ImageOptions] and the new [ImageOptions] into a new [ImageOptions]. Currently [ImageOptions] takes precedence
     */
    fun merged(
        options: ImageOptions?
    ): ImageOptions {
        if (options == null) return this
        return Builder(this).apply {
            merge(options)
        }.build()
    }

    /**
     * Returns true if all properties are empty
     */
    fun isEmpty(): Boolean =
        depth == null
                && parameters?.isEmpty() != false
                && httpHeaders?.isEmpty() != false
                && downloadCachePolicy == null
                && sizeResolver == null
                && sizeMultiplier == null
                && precisionDecider == null
                && scaleDecider == null
                && transformations == null
                && resultCachePolicy == null
                && placeholder == null
                && uriEmpty == null
                && error == null
                && transitionFactory == null
                && disallowAnimatedImage == null
                && resizeOnDraw == null
                && memoryCachePolicy == null
                && componentRegistry == null

    class Builder {

        private var depth: Depth? = null
        private var parametersBuilder: Parameters.Builder? = null

        private var httpHeadersBuilder: HttpHeaders.Builder? = null
        private var downloadCachePolicy: CachePolicy? = null

        private var sizeResolver: SizeResolver? = null
        private var sizeMultiplier: Float? = null
        private var precisionDecider: PrecisionDecider? = null
        private var scaleDecider: ScaleDecider? = null
        private var transformations: MutableList<Transformation>? = null
        private var resultCachePolicy: CachePolicy? = null

        private var placeholder: StateImage? = null
        private var uriEmpty: StateImage? = null
        private var error: ErrorStateImage? = null
        private var transitionFactory: Transition.Factory? = null
        private var disallowAnimatedImage: Boolean? = null
        private var resizeOnDraw: Boolean? = null
        private var memoryCachePolicy: CachePolicy? = null

        private var componentRegistry: ComponentRegistry? = null

        constructor()

        internal constructor(options: ImageOptions) {
            this.depth = options.depth
            this.parametersBuilder = options.parameters?.newBuilder()

            this.httpHeadersBuilder = options.httpHeaders?.newBuilder()
            this.downloadCachePolicy = options.downloadCachePolicy

            this.sizeResolver = options.sizeResolver
            this.sizeMultiplier = options.sizeMultiplier
            this.precisionDecider = options.precisionDecider
            this.scaleDecider = options.scaleDecider
            this.transformations = options.transformations?.toMutableList()
            this.resultCachePolicy = options.resultCachePolicy

            this.placeholder = options.placeholder
            this.uriEmpty = options.uriEmpty
            this.error = options.error
            this.transitionFactory = options.transitionFactory
            this.disallowAnimatedImage = options.disallowAnimatedImage
            this.resizeOnDraw = options.resizeOnDraw
            this.memoryCachePolicy = options.memoryCachePolicy

            this.componentRegistry = options.componentRegistry
        }


        /**
         * Set the requested depth
         */
        fun depth(depth: Depth?, depthFrom: String? = null): Builder = apply {
            this.depth = depth
            if (depth != null && depthFrom != null) {
                setParameter(DEPTH_FROM_KEY, depthFrom, null)
            } else {
                removeParameter(DEPTH_FROM_KEY)
            }
        }


        /**
         * Bulk set parameters for this request
         */
        fun parameters(parameters: Parameters?): Builder = apply {
            this.parametersBuilder = parameters?.newBuilder()
        }

        /**
         * Set a parameter for this request.
         */
        // TODO All parameters that depend on this parameter should be set to null for easy purging and merging
        fun setParameter(
            key: String,
            value: Any?,
            cacheKey: String? = keyOrNull(value),
            requestKey: String? = keyOrNull(value),
        ): Builder = apply {
            this.parametersBuilder = (this.parametersBuilder ?: Parameters.Builder()).apply {
                set(key, value, cacheKey, requestKey)
            }
        }

        /**
         * Remove a parameter from this request.
         */
        fun removeParameter(key: String): Builder = apply {
            this.parametersBuilder?.remove(key)
        }


        /**
         * Bulk set headers for any network request for this request
         */
        fun httpHeaders(httpHeaders: HttpHeaders?): Builder = apply {
            this.httpHeadersBuilder = httpHeaders?.newBuilder()
        }

        /**
         * Add a header for any network operations performed by this request.
         */
        fun addHttpHeader(name: String, value: String): Builder = apply {
            this.httpHeadersBuilder = (this.httpHeadersBuilder ?: HttpHeaders.Builder()).apply {
                add(name, value)
            }
        }

        /**
         * Set a header for any network operations performed by this request.
         */
        fun setHttpHeader(name: String, value: String): Builder = apply {
            this.httpHeadersBuilder = (this.httpHeadersBuilder ?: HttpHeaders.Builder()).apply {
                set(name, value)
            }
        }

        /**
         * Remove all network headers with the key [name].
         */
        fun removeHttpHeader(name: String): Builder = apply {
            this.httpHeadersBuilder?.removeAll(name)
        }

        /**
         * Set http download cache policy
         */
        fun downloadCachePolicy(cachePolicy: CachePolicy?): Builder = apply {
            this.downloadCachePolicy = cachePolicy
        }

        /**
         * Set how to resize image
         *
         * @param size Expected Bitmap size Resolver
         * @param precision precision of size, default is [Precision.LESS_PIXELS]
         * @param scale Which part of the original image to keep when [precision] is
         * [Precision.EXACTLY] or [Precision.SAME_ASPECT_RATIO], default is [Scale.CENTER_CROP]
         */
        fun resize(
            size: SizeResolver?,
            precision: PrecisionDecider? = null,
            scale: ScaleDecider? = null
        ): Builder = apply {
            this.sizeResolver = size
            this.precisionDecider = precision
            this.scaleDecider = scale
        }

        /**
         * Set how to resize image
         *
         * @param size Expected Bitmap size
         * @param precision precision of size, default is [Precision.LESS_PIXELS]
         * @param scale Which part of the original image to keep when [precision] is
         * [Precision.EXACTLY] or [Precision.SAME_ASPECT_RATIO], default is [Scale.CENTER_CROP]
         */
        fun resize(
            size: Size,
            precision: Precision? = null,
            scale: Scale? = null
        ): Builder = resize(
            SizeResolver(size),
            precision?.let { PrecisionDecider(it) },
            scale?.let { ScaleDecider(it) }
        )

        /**
         * Set how to resize image
         *
         * @param width Expected Bitmap width
         * @param height Expected Bitmap height
         * @param precision precision of size, default is [Precision.LESS_PIXELS]
         * @param scale Which part of the original image to keep when [precision] is
         * [Precision.EXACTLY] or [Precision.SAME_ASPECT_RATIO], default is [Scale.CENTER_CROP]
         */
        fun resize(
            width: Int,
            height: Int,
            precision: Precision? = null,
            scale: Scale? = null
        ): Builder = resize(
            FixedSizeResolver(width, height),
            precision?.let { PrecisionDecider(it) },
            scale?.let { ScaleDecider(it) }
        )

        /**
         * Set the [SizeResolver] to lazy resolve the requested size.
         */
        fun size(sizeResolver: SizeResolver?): Builder = apply {
            this.sizeResolver = sizeResolver
        }

        /**
         * Set the resize size
         */
        fun size(size: Size): Builder = size(SizeResolver(size))

        /**
         * Set the resize size
         */
        fun size(width: Int, height: Int): Builder =
            size(FixedSizeResolver(width, height))

        /**
         * val finalSize = sizeResolver.size() * sizeMultiplier
         */
        fun sizeMultiplier(multiplier: Float?): Builder = apply {
            this.sizeMultiplier = multiplier
        }

        /**
         * Set the resize precision, default is [Precision.LESS_PIXELS]
         */
        fun precision(precisionDecider: PrecisionDecider?): Builder = apply {
            this.precisionDecider = precisionDecider
        }

        /**
         * Set the resize precision, default is [Precision.LESS_PIXELS]
         */
        fun precision(precision: Precision): Builder =
            precision(PrecisionDecider(precision))

        /**
         * Set the resize scale, default is [Scale.CENTER_CROP]
         */
        fun scale(scaleDecider: ScaleDecider?): Builder = apply {
            this.scaleDecider = scaleDecider
        }

        /**
         * Set the resize scale, default is [Scale.CENTER_CROP]
         */
        fun scale(scale: Scale): Builder = scale(ScaleDecider(scale))

        /**
         * Set the list of [Transformation]s to be applied to this request.
         */
        fun transformations(transformations: List<Transformation>?): Builder = apply {
            this.transformations = transformations?.toMutableList()
        }

        /**
         * Set the list of [Transformation]s to be applied to this request.
         */
        fun transformations(vararg transformations: Transformation): Builder =
            transformations(transformations.toList())

        /**
         * Append the list of [Transformation]s to be applied to this request.
         */
        fun addTransformations(transformations: List<Transformation>): Builder = apply {
            val filterTransformations = transformations.filter { newTransformation ->
                this.transformations?.find { it.key == newTransformation.key } == null
            }
            this.transformations = (this.transformations ?: mutableListOf()).apply {
                addAll(filterTransformations)
            }
        }

        /**
         * Append the list of [Transformation]s to be applied to this request.
         */
        fun addTransformations(vararg transformations: Transformation): Builder =
            addTransformations(transformations.toList())

        /**
         * Bulk remove from current [Transformation] list
         */
        fun removeTransformations(removeTransformations: List<Transformation>): Builder =
            apply {
                this.transformations = this.transformations?.filter { oldTransformation ->
                    removeTransformations.find { it.key == oldTransformation.key } == null
                }?.toMutableList()
            }

        /**
         * Bulk remove from current [Transformation] list
         */
        fun removeTransformations(vararg removeTransformations: Transformation): Builder =
            removeTransformations(removeTransformations.toList())

        /**
         * Set disk caching policy for Bitmaps affected by [size] or [transformations]
         */
        fun resultCachePolicy(cachePolicy: CachePolicy?): Builder =
            apply {
                this.resultCachePolicy = cachePolicy
            }


        /**
         * Set placeholder image when loading
         */
        fun placeholder(stateImage: StateImage?): Builder = apply {
            this.placeholder = stateImage
        }

        /**
         * Set placeholder image when uri is empty
         */
        fun uriEmpty(stateImage: StateImage?): Builder = apply {
            this.uriEmpty = stateImage
        }

        /**
         * Set image to display when loading fails.
         *
         * You can also set image of different error types via the trailing lambda function
         */
        fun error(
            defaultStateImage: StateImage?,
            configBlock: (ErrorStateImage.Builder.() -> Unit)? = null
        ): Builder = apply {
            this.error = ErrorStateImage(defaultStateImage, configBlock)
                .takeIf { it.stateList.isNotEmpty() }
        }

        /**
         * Set image to display when loading fails.
         *
         * You can also set image of different error types via the trailing lambda function
         */
        fun error(
            configBlock: (ErrorStateImage.Builder.() -> Unit)? = null
        ): Builder = error(null, configBlock)

        /**
         * Set the transition between the current image and the new image
         */
        fun transitionFactory(transitionFactory: Transition.Factory?): Builder = apply {
            this.transitionFactory = transitionFactory
        }

        /**
         * Sets the transition that crossfade
         */
        fun crossfade(
            durationMillis: Int = CrossfadeTransition.DEFAULT_DURATION_MILLIS,
            fadeStart: Boolean = CrossfadeTransition.DEFAULT_FADE_START,
            preferExactIntrinsicSize: Boolean = CrossfadeTransition.DEFAULT_PREFER_EXACT_INTRINSIC_SIZE,
            alwaysUse: Boolean = CrossfadeTransition.DEFAULT_ALWAYS_USE,
        ): Builder = apply {
            this.transitionFactory = CrossfadeTransition.Factory(
                durationMillis = durationMillis,
                fadeStart = fadeStart,
                preferExactIntrinsicSize = preferExactIntrinsicSize,
                alwaysUse = alwaysUse
            )
        }

        /**
         * Sets the transition that crossfade
         */
        fun crossfade(enable: Boolean): Builder = apply {
            if (enable) {
                crossfade()
            } else {
                this.transitionFactory = null
            }
        }

        /**
         * Set disallow decode animation image, animations such as gif will only decode their first frame and return BitmapDrawable
         */
        fun disallowAnimatedImage(disabled: Boolean? = true): Builder = apply {
            this.disallowAnimatedImage = disabled
        }

        /**
         * Use ResizeDrawable or ResizePainter to wrap an Image to resize it while drawing, it will act on placeholder, uriEmpty, error and the decoded image
         */
        fun resizeOnDraw(apply: Boolean? = true): Builder = apply {
            this.resizeOnDraw = apply
        }

        /**
         * Set bitmap memory caching policy
         */
        fun memoryCachePolicy(cachePolicy: CachePolicy?): Builder = apply {
            this.memoryCachePolicy = cachePolicy
        }

        /**
         * Set the [ComponentRegistry]
         */
        fun components(components: ComponentRegistry?): Builder = apply {
            this.componentRegistry = components
        }

        /**
         * Build and set the [ComponentRegistry]
         */
        fun components(configBlock: (ComponentRegistry.Builder.() -> Unit)): Builder =
            components(ComponentRegistry.Builder().apply(configBlock).build())

        /**
         * Merge the [ComponentRegistry]
         */
        fun mergeComponents(components: ComponentRegistry?): Builder = apply {
            this.componentRegistry = this.componentRegistry.merged(components)
        }

        /**
         * Merge the [ComponentRegistry]
         */
        fun mergeComponents(configBlock: (ComponentRegistry.Builder.() -> Unit)): Builder =
            mergeComponents(ComponentRegistry.Builder().apply(configBlock).build())


        /**
         * Merge the specified [ImageOptions] into the current [Builder]. Currently [Builder] takes precedence
         */
        fun merge(options: ImageOptions?): Builder = apply {
            if (options == null) return@apply

            if (this.depth == null) {
                this.depth = options.depth
            }
            options.parameters?.let {
                parametersBuilder = parametersBuilder?.build().merged(it)?.newBuilder()
            }

            options.httpHeaders?.let {
                httpHeadersBuilder = httpHeadersBuilder?.build().merged(it)?.newBuilder()
            }
            if (this.downloadCachePolicy == null) {
                this.downloadCachePolicy = options.downloadCachePolicy
            }

            if (this.sizeResolver == null) {
                this.sizeResolver = options.sizeResolver
            }
            if (this.sizeMultiplier == null) {
                this.sizeMultiplier = options.sizeMultiplier
            }
            if (this.precisionDecider == null) {
                this.precisionDecider = options.precisionDecider
            }
            if (this.scaleDecider == null) {
                this.scaleDecider = options.scaleDecider
            }
            options.transformations?.takeIf { it.isNotEmpty() }?.let {
                addTransformations(it)
            }
            if (this.resultCachePolicy == null) {
                this.resultCachePolicy = options.resultCachePolicy
            }

            if (this.placeholder == null) {
                this.placeholder = options.placeholder
            }
            if (this.uriEmpty == null) {
                this.uriEmpty = options.uriEmpty
            }
            if (this.error == null) {
                this.error = options.error
            }
            if (this.transitionFactory == null) {
                this.transitionFactory = options.transitionFactory
            }
            if (this.disallowAnimatedImage == null) {
                this.disallowAnimatedImage = options.disallowAnimatedImage
            }
            if (this.resizeOnDraw == null) {
                this.resizeOnDraw = options.resizeOnDraw
            }
            if (this.memoryCachePolicy == null) {
                this.memoryCachePolicy = options.memoryCachePolicy
            }

            componentRegistry = componentRegistry.merged(options.componentRegistry)
        }


        fun build(): ImageOptions {
            val parameters = parametersBuilder?.build()?.takeIf { it.isNotEmpty() }
            val httpHeaders = httpHeadersBuilder?.build()?.takeIf { it.isNotEmpty() }
            val transformations = transformations?.takeIf { it.isNotEmpty() }
            return ImageOptionsImpl(
                depth = depth,
                parameters = parameters,
                httpHeaders = httpHeaders,
                downloadCachePolicy = downloadCachePolicy,
                resultCachePolicy = resultCachePolicy,
                sizeResolver = sizeResolver,
                sizeMultiplier = sizeMultiplier,
                precisionDecider = precisionDecider,
                scaleDecider = scaleDecider,
                transformations = transformations,
                placeholder = placeholder,
                uriEmpty = uriEmpty,
                error = error,
                transitionFactory = transitionFactory,
                disallowAnimatedImage = disallowAnimatedImage,
                resizeOnDraw = resizeOnDraw,
                memoryCachePolicy = memoryCachePolicy,
                componentRegistry = componentRegistry,
            )
        }
    }

    data class ImageOptionsImpl(
        override val depth: Depth?,
        override val parameters: Parameters?,
        override val httpHeaders: HttpHeaders?,
        override val downloadCachePolicy: CachePolicy?,
        override val sizeResolver: SizeResolver?,
        override val sizeMultiplier: Float?,
        override val precisionDecider: PrecisionDecider?,
        override val scaleDecider: ScaleDecider?,
        override val transformations: List<Transformation>?,
        override val resultCachePolicy: CachePolicy?,
        override val placeholder: StateImage?,
        override val uriEmpty: StateImage?,
        override val error: ErrorStateImage?,
        override val transitionFactory: Transition.Factory?,
        override val disallowAnimatedImage: Boolean?,
        override val resizeOnDraw: Boolean?,
        override val memoryCachePolicy: CachePolicy?,
        override val componentRegistry: ComponentRegistry?,
    ) : ImageOptions
}

/**
 * Returns true as long as any property is not empty
 */
fun ImageOptions.isNotEmpty(): Boolean = !isEmpty()