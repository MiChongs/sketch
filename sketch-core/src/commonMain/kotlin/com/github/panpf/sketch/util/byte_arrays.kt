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
 * Returns `true` if the specified range in this byte array is equal to the specified byte array.
 *
 * @see com.github.panpf.sketch.core.common.test.util.ByteArraysTest.testRangeEquals
 */
fun ByteArray.rangeEquals(offset: Int, bytes: ByteArray): Boolean {
    require(bytes.isNotEmpty()) { "bytes is empty" }

    var index = 0
    var result = false
    while (index < bytes.size && (index + offset) < this.size) {
        result = bytes[index] == this[offset + index]
        if (!result) {
            return false
        } else {
            index++
        }
    }
    return result
}

/**
 * Returns the index within this byte array of the first occurrence of the specified byte, starting from the specified index.
 *
 * @param fromIndex   the begin index, inclusive.
 * @param toIndex     the end index, inclusive.
 *
 * @see com.github.panpf.sketch.core.common.test.util.ByteArraysTest.testIndexOf
 */
fun ByteArray.indexOf(byte: Byte, fromIndex: Int, toIndex: Int): Int {
    require(fromIndex in 0L..toIndex) { "fromIndex=$fromIndex toIndex=$toIndex" }
    var index = fromIndex
    while (index < toIndex && index < this.size) {
        if (this[index] == byte) {
            return index
        } else {
            index++
        }
    }
    return -1
}

/**
 * Returns the index within this byte array of the first occurrence of the specified byte array, starting from the specified index.
 *
 * @param fromIndex   the begin index, inclusive.
 * @param toIndex     the end index, exclusive.
 *
 * @see com.github.panpf.sketch.core.common.test.util.ByteArraysTest.testIndexOf
 */
fun ByteArray.indexOf(bytes: ByteArray, fromIndex: Int, toIndex: Int): Int {
    require(fromIndex in 0L..toIndex) { "fromIndex=$fromIndex toIndex=$toIndex" }
    require(bytes.isNotEmpty()) { "bytes is empty" }

    val firstByte = bytes[0]
    val lastIndex = toIndex + 1 - bytes.size
    var currentIndex = fromIndex
    while (currentIndex < lastIndex) {
        currentIndex = indexOf(firstByte, currentIndex, lastIndex)
        if (currentIndex == -1 || rangeEquals(currentIndex, bytes)) {
            return currentIndex
        }
        currentIndex++
    }
    return -1
}