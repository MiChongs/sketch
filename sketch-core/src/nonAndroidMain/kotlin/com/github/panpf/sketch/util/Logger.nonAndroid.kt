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

package com.github.panpf.sketch.util

/**
 * Get the platform default log pipeline
 *
 * @see com.github.panpf.sketch.core.nonandroid.test.util.LoggerNonAndroidTest.testDefaultLogPipeline
 */
internal actual fun defaultLogPipeline(): Logger.Pipeline = PrintLogPipeline

/**
 * Used to print log
 *
 * @see com.github.panpf.sketch.core.nonandroid.test.util.LoggerNonAndroidTest.testPrintLogPipeline
 */
data object PrintLogPipeline : Logger.Pipeline {

    override fun log(level: Logger.Level, tag: String, msg: String, tr: Throwable?) {
        if (tr != null) {
            val trString = tr.stackTraceToString()
            println("$level. $tag. $msg. \n$trString")
        } else {
            println("$level. $tag. $msg")
        }
    }

    override fun flush() {

    }

    override fun toString(): String = "PrintLogPipeline"
}