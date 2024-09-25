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

package com.github.panpf.sketch.cache

import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.cache.internal.EmptyDiskCache
import com.github.panpf.sketch.cache.internal.LruDiskCache
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.RequestContext
import com.github.panpf.sketch.util.appCacheDirectory
import okio.Closeable
import okio.FileSystem
import okio.Path
import kotlin.math.roundToLong

/**
 * Disk cache for bitmap or uri data
 *
 * @see com.github.panpf.sketch.core.common.test.cache.DiskCacheTest
 */
interface DiskCache : Closeable {

    companion object {
        const val DIRECTORY_NAME = "sketch4"
    }

    val fileSystem: FileSystem

    /**
     * Get the cache directory on disk
     */
    val directory: Path

    /**
     * Maximum allowed sum of the size of the all cache
     */
    val maxSize: Long

    /**
     * Version, used to delete the old cache, update this value when you want to actively delete the old cache
     */
    val appVersion: Int

    /**
     * Controlled by sketch, sketch fixes the wrong disk cache generated by the old version and will modify this version to update the cache
     */
    val internalVersion: Int

    /**
     * Sum of the size of the all cache
     */
    val size: Long

    /**
     * Read the entry associated with [key].
     *
     * IMPORTANT: **You must** call either [Snapshot.close] or [Snapshot.closeAndOpenEditor] when
     * finished reading the snapshot. An open snapshot prevents opening a new [Editor] or deleting
     * the entry on disk.
     */
    fun openSnapshot(key: String): Snapshot?

    /**
     * Write to the entry associated with [key].
     *
     * IMPORTANT: **You must** call one of [Editor.commit], [Editor.commitAndOpenSnapshot], or
     * [Editor.abort] to complete the edit. An open editor prevents opening a new [Snapshot],
     * opening a new [Editor], or deleting the entry on disk.
     */
    fun openEditor(key: String): Editor?

    /**
     * Delete the entry referenced by [key].
     *
     * @return 'true' if [key] was removed successfully. Else, return 'false'.
     */
    fun remove(key: String): Boolean

    /**
     * Clear all cached
     */
    fun clear()

    /**
     * Executes the given [action] under this mutex's lock.
     */
    suspend fun <R> withLock(key: String, action: suspend DiskCache.() -> R): R

    /**
     * Snapshot the values for an entry.
     */
    interface Snapshot : Closeable {

        /** Get the metadata file path for this entry. */
        val metadata: Path

        /** Get the data file path for this entry. */
        val data: Path

        /** Close the snapshot to allow editing. */
        override fun close()

        /** Close the snapshot and call [openEditor] for this entry atomically. */
        fun closeAndOpenEditor(): Editor?
    }

    /**
     * Edits the values for an entry.
     */
    interface Editor {

        /** Get the metadata file path for this entry. */
        val metadata: Path

        /** Get the data file path for this entry. */
        val data: Path

        /** Commit the edit so the changes are visible to readers. */
        fun commit()

        /** Commit the write and call [openSnapshot] for this entry atomically. */
        fun commitAndOpenSnapshot(): Snapshot?

        /** Abort the edit. Any written data will be discarded. */
        fun abort()
    }

    /**
     * @see com.github.panpf.sketch.core.common.test.cache.DiskCacheTest.testBuilder
     */
    open class Builder(
        val context: PlatformContext,
        val fileSystem: FileSystem,
        private val subDirectoryName: String? = null,
        private val maxSizePercent: Float = 1.0f,
        private val internalVersion: Int = 1,
    ) {

        companion object {
            const val DEFAULT_APP_VERSION = 1
        }

        private var directory: Path? = null
        private var appCacheDirectory: Path? = null
        private var maxSize: Long? = null
        private var appVersion: Int? = null

        fun directory(directory: Path? = null): Builder = apply {
            this.directory = directory
        }

        fun appCacheDirectory(appCacheDirectory: Path? = null): Builder = apply {
            this.appCacheDirectory = appCacheDirectory
        }

        fun maxSize(size: Long? = null): Builder = apply {
            require(size == null || size > 0L) {
                "maxSize must be greater than 0L"
            }
            this.maxSize = size
        }

        fun appVersion(appVersion: Int? = null): Builder = apply {
            require(appVersion == null || appVersion in 1.rangeTo(Short.MAX_VALUE)) {
                "appVersion must be in 1 to ${Short.MAX_VALUE} range"
            }
            this.appVersion = appVersion
        }

        fun options(options: Options): Builder = apply {
            this.directory = options.directory
            this.appCacheDirectory = options.appCacheDirectory
            this.maxSize = options.maxSize
            this.appVersion = options.appVersion
        }

        fun mergeOptions(options: Options): Builder = apply {
            this.directory = this.directory ?: options.directory
            this.appCacheDirectory = this.appCacheDirectory ?: options.appCacheDirectory
            this.maxSize = this.maxSize ?: options.maxSize
            this.appVersion = this.appVersion ?: options.appVersion
        }

        fun build(): DiskCache {
            val platformDefaultMaxSize = defaultDiskCacheMaxSize(context)
            @Suppress("FoldInitializerAndIfToElvis", "RedundantSuppression")
            if (platformDefaultMaxSize == null) {
                return EmptyDiskCache(fileSystem)
            }

            val directory = directory
            val appCacheDirectory = appCacheDirectory
            val finalDirectory = directory
                ?: (appCacheDirectory ?: context.appCacheDirectory())
                    ?.resolve(DIRECTORY_NAME)
                    ?.let { if (subDirectoryName != null) it.resolve(subDirectoryName) else it }
                ?: return EmptyDiskCache(fileSystem)

            val maxSizeBytes = maxSize
            val finalMaxSizeBytes = maxSizeBytes
                ?: (platformDefaultMaxSize * maxSizePercent).roundToLong()

            val appVersion = appVersion
            val finalAppVersion = appVersion ?: DEFAULT_APP_VERSION
            return LruDiskCache(
                context = context,
                fileSystem = fileSystem,
                maxSize = finalMaxSizeBytes,
                directory = finalDirectory,
                appVersion = finalAppVersion,
                internalVersion = internalVersion
            )
        }
    }

    /**
     * @see com.github.panpf.sketch.core.common.test.cache.DiskCacheTest.testDownloadBuilder
     */
    class DownloadBuilder(
        context: PlatformContext,
        fileSystem: FileSystem
    ) : Builder(
        context = context,
        fileSystem = fileSystem,
        subDirectoryName = SUB_DIRECTORY_NAME,
        maxSizePercent = MAX_SIZE_PERCENT,
        internalVersion = INTERNAL_VERSION
    ) {
        companion object {
            const val SUB_DIRECTORY_NAME = "download"
            const val MAX_SIZE_PERCENT = 0.6f
            const val INTERNAL_VERSION = 1
        }
    }

    /**
     * @see com.github.panpf.sketch.core.common.test.cache.DiskCacheTest.testResultBuilder
     */
    class ResultBuilder(
        context: PlatformContext,
        fileSystem: FileSystem
    ) : Builder(
        context = context,
        fileSystem = fileSystem,
        subDirectoryName = SUB_DIRECTORY_NAME,
        maxSizePercent = MAX_SIZE_PERCENT,
        internalVersion = INTERNAL_VERSION
    ) {
        companion object {
            const val SUB_DIRECTORY_NAME = "result"
            const val MAX_SIZE_PERCENT = 0.4f
            const val INTERNAL_VERSION = 1
        }
    }

    data class Options(
        val directory: Path? = null,
        val appCacheDirectory: Path? = null,
        val maxSize: Long? = null,
        val appVersion: Int? = null,
    )
}

/**
 * Get the default maximum size of the disk cache
 *
 * @see com.github.panpf.sketch.core.jscommon.test.cache.DiskCacheJsCommonTest.testDefaultDiskCacheMaxSize
 * @see com.github.panpf.sketch.core.nonjscommon.test.cache.DiskCacheNonJsCommonTest.testDefaultDiskCacheMaxSize
 */
internal expect fun defaultDiskCacheMaxSize(context: PlatformContext): Long?

/**
 * Result cache key
 *
 * @see com.github.panpf.sketch.core.common.test.cache.DiskCacheTest.testResultCacheKey
 */
val RequestContext.resultCacheKey: String
    get() = cacheKey

/**
 * Download cache key
 *
 * @see com.github.panpf.sketch.core.common.test.cache.DiskCacheTest.testDownloadCacheKey
 */
val ImageRequest.downloadCacheKey: String
    get() = uri.toString()