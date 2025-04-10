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

package com.github.panpf.sketch.fetch

import com.github.panpf.sketch.annotation.WorkerThread
import com.github.panpf.sketch.request.RequestContext
import com.github.panpf.sketch.source.KotlinResourceDataSource
import com.github.panpf.sketch.util.MimeTypeMap
import com.github.panpf.sketch.util.Uri
import com.github.panpf.sketch.util.defaultFileSystem
import okio.FileSystem

/**
 * Sample: 'file:///kotlin_resource/test.png'
 *
 * @see com.github.panpf.sketch.core.ios.test.fetch.KotlinResourceUriFetcherTest.testNewKotlinResourceUri
 */
fun newKotlinResourceUri(resourcePath: String): String =
    "${KotlinResourceUriFetcher.SCHEME}:///${KotlinResourceUriFetcher.PATH_ROOT}/$resourcePath"

/**
 * Check if the uri is a Kotlin resource uri
 *
 * Sample: 'file:///kotlin_resource/test.png'
 *
 * @see com.github.panpf.sketch.core.ios.test.fetch.KotlinResourceUriFetcherTest.testIsKotlinResourceUri
 */
fun isKotlinResourceUri(uri: Uri): Boolean =
    KotlinResourceUriFetcher.SCHEME.equals(uri.scheme, ignoreCase = true)
            && uri.authority?.takeIf { it.isNotEmpty() } == null
            && KotlinResourceUriFetcher.PATH_ROOT
        .equals(uri.pathSegments.firstOrNull(), ignoreCase = true)

/**
 * Fetcher for Kotlin resource uri
 *
 * @see com.github.panpf.sketch.core.ios.test.fetch.KotlinResourceUriFetcherTest
 */
class KotlinResourceUriFetcher constructor(
    val resourcePath: String,
    val fileSystem: FileSystem = defaultFileSystem()
) : Fetcher {

    companion object {
        const val SCHEME = "file"
        const val PATH_ROOT = "kotlin_resource"
    }

    @WorkerThread
    override suspend fun fetch(): Result<FetchResult> = kotlin.runCatching {
        val mimeType = MimeTypeMap.getMimeTypeFromUrl(resourcePath)
        val dataSource = KotlinResourceDataSource(resourcePath, fileSystem)
        return Result.success(FetchResult(dataSource, mimeType))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as KotlinResourceUriFetcher
        if (resourcePath != other.resourcePath) return false
        return true
    }

    override fun hashCode(): Int {
        return resourcePath.hashCode()
    }

    override fun toString(): String {
        return "KotlinResourceUriFetcher('$resourcePath')"
    }

    class Factory : Fetcher.Factory {

        override fun create(requestContext: RequestContext): KotlinResourceUriFetcher? {
            val uri = requestContext.request.uri
            if (!isKotlinResourceUri(uri)) return null
            val resourcePath = uri.pathSegments.drop(1).joinToString("/")
            return KotlinResourceUriFetcher(
                resourcePath = resourcePath,
                fileSystem = requestContext.sketch.fileSystem
            )
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other != null && this::class == other::class
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }

        override fun toString(): String = "KotlinResourceUriFetcher"
    }
}