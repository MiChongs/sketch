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
package com.github.panpf.sketch.test.utils

import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.cache.DiskCache
import com.github.panpf.sketch.util.Logger
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import okio.Path

expect fun getTestContext(): PlatformContext

// TODO 不再使用
fun newSketch(block: Sketch.Builder.(context: PlatformContext) -> Unit): Sketch {
    val context = getTestContext()
    return Sketch.Builder(context).apply {
        logger(level = Logger.Level.Verbose)
        val directory = context.newAloneTestDiskCacheDirectory()
        downloadCacheOptions(DiskCache.Options(appCacheDirectory = directory))
        resultCacheOptions(DiskCache.Options(appCacheDirectory = directory))
        block.invoke(this, context)
    }.build()
}

// TODO 不再使用
fun newSketch(): Sketch {
    return newSketch {}
}

// TODO 改成在 block 中使用新创建的 Sketch，block 结束后自动清除 downloadCache 和 resultCache，防止产生越来越多的垃圾文件
fun getTestContextAndNewSketch(block: Sketch.Builder.(context: PlatformContext) -> Unit): Pair<PlatformContext, Sketch> {
    val context = getTestContext()
    return context to newSketch(block)
}

var sketchCount = 0
val sketchCountLock = SynchronizedObject()

fun PlatformContext.newAloneTestDiskCacheDirectory(): Path? {
    val testDiskCacheDirectory = getTestDiskCacheDirectory() ?: return null
    val newSketchCount = synchronized(sketchCountLock) { sketchCount++ }
    return testDiskCacheDirectory.resolve("test_alone_${newSketchCount}")
}

expect fun PlatformContext.getTestDiskCacheDirectory(): Path?