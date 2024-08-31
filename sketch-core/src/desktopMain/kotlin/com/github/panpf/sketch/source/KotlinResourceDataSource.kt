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

package com.github.panpf.sketch.source

import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.fetch.newKotlinResourceUri
import com.github.panpf.sketch.source.DataFrom.LOCAL
import com.github.panpf.sketch.util.ClassLoaderResourceLoader
import okio.Path
import okio.Source
import okio.source
import java.io.IOException

/**
 * Kotlin resource data source, used to load pictures from the resources of the Kotlin project
 *
 * @see com.github.panpf.sketch.core.desktop.test.source.KotlinResourceDataSourceTest
 */
class KotlinResourceDataSource constructor(
    val resourcePath: String,
) : DataSource {

    override val key: String by lazy { newKotlinResourceUri(resourcePath) }

    override val dataFrom: DataFrom = LOCAL

    @Throws(IOException::class)
    override fun openSource(): Source =
        ClassLoaderResourceLoader.Default.load(resourcePath).source()

    @Throws(IOException::class)
    override fun getFile(sketch: Sketch): Path = cacheFile(sketch)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as KotlinResourceDataSource
        return resourcePath == other.resourcePath
    }

    override fun hashCode(): Int {
        return resourcePath.hashCode()
    }

    override fun toString(): String = "KotlinResourceDataSource('$resourcePath')"
}