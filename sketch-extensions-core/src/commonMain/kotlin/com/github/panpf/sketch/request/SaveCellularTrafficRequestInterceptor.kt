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

package com.github.panpf.sketch.request

import com.github.panpf.sketch.ComponentRegistry
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.request.Depth.LOCAL
import com.github.panpf.sketch.request.RequestInterceptor.Chain

/**
 * Adds save cellular traffic support
 *
 * @see com.github.panpf.sketch.extensions.core.common.test.request.SaveCellularTrafficRequestInterceptorTest.testSupportSaveCellularTraffic
 */
fun ComponentRegistry.Builder.supportSaveCellularTraffic(): ComponentRegistry.Builder = apply {
    addRequestInterceptor(SaveCellularTrafficRequestInterceptor())
}

/**
 * To save cellular traffic. Prohibit downloading images from the Internet if the current network is cellular,
 * Then can also cooperate with saveCellularTrafficError custom error image display
 *
 * @see ImageRequest.Builder.saveCellularTraffic
 * @see com.github.panpf.sketch.extensions.core.common.test.request.SaveCellularTrafficRequestInterceptorTest
 */
class SaveCellularTrafficRequestInterceptor constructor(
    override val sortWeight: Int = 0,
    isCellularNetworkConnected: ((Sketch) -> Boolean)? = null
) : RequestInterceptor {

    override val key: String? = null

    companion object {
        private const val SAVE_CELLULAR_TRAFFIC_OLD_DEPTH_KEY =
            "sketch#save_cellular_traffic_old_depth"
    }

    var enabled = true

    private val isCellularNetworkConnected: (Sketch) -> Boolean =
        isCellularNetworkConnected ?: { sketch ->
            sketch.systemCallbacks.isCellularNetworkConnected
        }

    override suspend fun intercept(chain: Chain): Result<ImageData> {
        val request = chain.request
        val finalRequest = when {
            enabled && request.isSaveCellularTraffic
                    && !request.isIgnoredSaveCellularTraffic
                    && isCellularNetworkConnected(chain.sketch) -> {
                if (request.depthHolder.depth != LOCAL) {
                    val oldDepth = request.depthHolder.depth
                    request.newRequest {
                        depth(LOCAL, SAVE_CELLULAR_TRAFFIC_KEY)
                        setExtra(
                            key = SAVE_CELLULAR_TRAFFIC_OLD_DEPTH_KEY,
                            value = oldDepth.name,
                            cacheKey = null
                        )
                    }
                } else {
                    request
                }
            }

            else -> {
                val oldDepth =
                    request.extras?.value<String>(SAVE_CELLULAR_TRAFFIC_OLD_DEPTH_KEY)?.let {
                        try {
                            Depth.valueOf(it)
                        } catch (e: Exception) {
                            e.toString()
                            null
                        }
                    }
                if (oldDepth != null && request.depthHolder.depth != oldDepth) {
                    request.newRequest {
                        depth(oldDepth)
                        removeExtra(SAVE_CELLULAR_TRAFFIC_OLD_DEPTH_KEY)
                    }
                } else {
                    request
                }
            }
        }
        return chain.proceed(finalRequest)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as SaveCellularTrafficRequestInterceptor
        if (sortWeight != other.sortWeight) return false
        return true
    }

    override fun hashCode(): Int {
        return sortWeight
    }

    override fun toString(): String =
        "SaveCellularTrafficRequestInterceptor(sortWeight=$sortWeight,enabled=$enabled)"
}