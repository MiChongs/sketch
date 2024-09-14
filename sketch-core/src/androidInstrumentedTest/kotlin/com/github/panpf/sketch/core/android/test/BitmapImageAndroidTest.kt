package com.github.panpf.sketch.core.android.test

import com.github.panpf.sketch.AndroidBitmap
import com.github.panpf.sketch.BitmapImage
import com.github.panpf.sketch.asImage
import com.github.panpf.sketch.images.ResourceImages
import com.github.panpf.sketch.test.utils.decode2
import com.github.panpf.sketch.util.toLogString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class BitmapImageAndroidTest {

    @Test
    fun testAsImage() {
        assertEquals(
            expected = BitmapImage(AndroidBitmap(100, 100)),
            actual = AndroidBitmap(100, 100).asImage()
        )
    }

    @Test
    fun testConstructor() {
        val mutableBitmap = AndroidBitmap(100, 100)
        assertTrue(mutableBitmap.isMutable)
        BitmapImage(mutableBitmap).apply {
            assertSame(expected = mutableBitmap, actual = bitmap)
            assertFalse(actual = shareable)
        }
        BitmapImage(mutableBitmap, shareable = true).apply {
            assertSame(expected = mutableBitmap, actual = bitmap)
            assertTrue(actual = shareable)
        }

        val immutableBitmap = ResourceImages.jpeg.decode2().bitmap
        assertFalse(immutableBitmap.isMutable)
        BitmapImage(immutableBitmap).apply {
            assertSame(expected = immutableBitmap, actual = bitmap)
            assertTrue(actual = shareable)
        }
    }

    @Test
    fun testWidthHeight() {
        BitmapImage(AndroidBitmap(100, 200)).apply {
            assertEquals(expected = 100, actual = width)
            assertEquals(expected = 200, actual = height)
        }
        BitmapImage(AndroidBitmap(200, 100)).apply {
            assertEquals(expected = 200, actual = width)
            assertEquals(expected = 100, actual = height)
        }
    }

    @Test
    fun testByteCount() {
        BitmapImage(AndroidBitmap(100, 200)).apply {
            assertEquals(expected = 100 * 200 * 4L, actual = byteCount)
            assertEquals(expected = 100 * 200 * 4L, actual = allocationByteCount)
        }
        BitmapImage(AndroidBitmap(200, 300)).apply {
            assertEquals(expected = 200 * 300 * 4L, actual = byteCount)
            assertEquals(expected = 200 * 300 * 4L, actual = allocationByteCount)
        }
    }

    @Test
    fun testCachedInMemory() {
        val bitmap = AndroidBitmap(100, 200)
        BitmapImage(bitmap).apply {
            assertTrue(actual = cachedInMemory)
        }
        BitmapImage(bitmap, cachedInMemory = false).apply {
            assertFalse(actual = cachedInMemory)
        }
    }

    @Test
    fun testCheckValid() {
        BitmapImage(AndroidBitmap(100, 200)).apply {
            assertTrue(actual = checkValid())
            assertTrue(actual = checkValid())
            assertTrue(actual = checkValid())
        }
    }

    @Test
    fun testToString() {
        val bitmap = AndroidBitmap(100, 200)
        assertEquals(
            "BitmapImage(bitmap=${bitmap.toLogString()}, shareable=false)",
            BitmapImage(bitmap).toString()
        )

        val bitmap2 = AndroidBitmap(200, 100)
        assertEquals(
            "BitmapImage(bitmap=${bitmap2.toLogString()}, shareable=false)",
            BitmapImage(bitmap2, shareable = false).toString()
        )
    }
}