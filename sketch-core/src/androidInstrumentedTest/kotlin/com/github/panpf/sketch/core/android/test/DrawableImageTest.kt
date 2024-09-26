package com.github.panpf.sketch.core.android.test

import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.BitmapDrawable
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import com.github.panpf.sketch.AndroidBitmap
import com.github.panpf.sketch.DrawableImage
import com.github.panpf.sketch.asDrawable
import com.github.panpf.sketch.asImage
import com.github.panpf.sketch.images.ResourceImages
import com.github.panpf.sketch.images.toDataSource
import com.github.panpf.sketch.test.utils.ByteCountProviderDrawableWrapper
import com.github.panpf.sketch.test.utils.FakeImage
import com.github.panpf.sketch.test.utils.getTestContext
import com.github.panpf.sketch.util.asOrThrow
import com.github.panpf.sketch.util.getDrawableCompat
import com.github.panpf.sketch.util.toLogString
import okio.buffer
import java.nio.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class DrawableImageTest {

    @Test
    fun testAsImage() {
        val context = getTestContext()

        val bitmapDrawable =
            context.getDrawableCompat(android.R.drawable.ic_delete) as BitmapDrawable
        assertTrue(bitmapDrawable.asImage().shareable)

        if (VERSION.SDK_INT >= VERSION_CODES.P) {
            val bytes = ResourceImages.animGif.toDataSource(context).openSource()
                .buffer()
                .use { it.readByteArray() }
            val animatedDrawable =
                ImageDecoder.decodeDrawable(ImageDecoder.createSource(ByteBuffer.wrap(bytes)))
                    .asOrThrow<AnimatedImageDrawable>()
            assertFalse(animatedDrawable.asImage().shareable)
        }
    }

    @Test
    fun testAsDrawable() {
        val context = getTestContext()

        val bitmapDrawable =
            context.getDrawableCompat(android.R.drawable.ic_delete) as BitmapDrawable
        assertSame(
            expected = bitmapDrawable,
            actual = bitmapDrawable.asImage().asDrawable()
        )

        AndroidBitmap(100, 100).asImage().asDrawable().also { drawable ->
            assertTrue(actual = drawable is BitmapDrawable, message = "drawable=$drawable")
        }

        assertFailsWith(IllegalArgumentException::class) {
            FakeImage(100, 100).asDrawable()
        }
    }

    @Test
    fun testConstructor() {
        val context = getTestContext()

        val bitmapDrawable =
            context.getDrawableCompat(android.R.drawable.ic_delete) as BitmapDrawable
        DrawableImage(bitmapDrawable).apply {
            assertTrue(actual = shareable)
        }

        if (VERSION.SDK_INT >= VERSION_CODES.P) {
            val bytes = ResourceImages.animGif.toDataSource(context).openSource()
                .buffer()
                .use { it.readByteArray() }
            val animatedDrawable =
                ImageDecoder.decodeDrawable(ImageDecoder.createSource(ByteBuffer.wrap(bytes)))
                    .asOrThrow<AnimatedImageDrawable>()
            DrawableImage(animatedDrawable).apply {
                assertFalse(actual = shareable)
            }
        }
    }

    @Test
    fun testWidthHeight() {
        val context = getTestContext()

        val drawable1 =
            context.getDrawableCompat(android.R.drawable.ic_delete) as BitmapDrawable
        DrawableImage(drawable1).apply {
            assertEquals(expected = drawable1.intrinsicWidth, actual = width)
            assertEquals(expected = drawable1.intrinsicHeight, actual = height)
        }

        val drawable2 =
            context.getDrawableCompat(com.github.panpf.sketch.test.utils.core.R.drawable.test)
        DrawableImage(drawable2).apply {
            assertEquals(expected = drawable2.intrinsicWidth, actual = width)
            assertEquals(expected = drawable2.intrinsicHeight, actual = height)
        }
    }

    @Test
    fun testByteCount() {
        val context = getTestContext()

        val drawable1 =
            context.getDrawableCompat(android.R.drawable.ic_delete) as BitmapDrawable
        DrawableImage(drawable1).apply {
            assertEquals(expected = drawable1.bitmap.byteCount.toLong(), actual = byteCount)
        }

        DrawableImage(ByteCountProviderDrawableWrapper(drawable1, 100)).apply {
            assertEquals(expected = 100, actual = byteCount)
        }

        val drawable2 =
            context.getDrawableCompat(com.github.panpf.sketch.test.utils.core.R.drawable.test)
        DrawableImage(drawable2).apply {
            assertEquals(
                expected = 4L * drawable2.intrinsicWidth * drawable2.intrinsicHeight,
                actual = byteCount
            )
        }
    }

    @Test
    fun testValid() {
        val context = getTestContext()

        val bitmap = AndroidBitmap(100, 100)
        val drawable = BitmapDrawable(null, bitmap)
        val drawableImage = DrawableImage(drawable)

        assertFalse(bitmap.isRecycled)
        assertTrue(drawableImage.checkValid())

        bitmap.recycle()
        assertTrue(bitmap.isRecycled)
        assertFalse(drawableImage.checkValid())

        val drawable2 =
            context.getDrawableCompat(com.github.panpf.sketch.test.utils.core.R.drawable.test)
        DrawableImage(drawable2).apply {
            assertTrue(checkValid())
        }
    }

    @Test
    fun testEqualsAndHashCode() {
        val element1 = DrawableImage(BitmapDrawable(null, AndroidBitmap(100, 100)))
        val element11 = element1.copy()
        val element2 = element1.copy(drawable = BitmapDrawable(null, AndroidBitmap(200, 200)))
        val element3 = element1.copy(shareable = false)

        assertEquals(element1, element1)
        assertEquals(element1, element11)
        assertNotEquals(element1, element2)
        assertNotEquals(element1, element3)
        assertNotEquals(element2, element11)
        assertNotEquals(element2, element3)
        assertNotEquals(element1, null as Any?)
        assertNotEquals(element1, Any())

        assertEquals(element1.hashCode(), element1.hashCode())
        assertEquals(element1.hashCode(), element11.hashCode())
        assertNotEquals(element1.hashCode(), element2.hashCode())
        assertNotEquals(element1.hashCode(), element3.hashCode())
        assertNotEquals(element2.hashCode(), element11.hashCode())
        assertNotEquals(element2.hashCode(), element3.hashCode())
    }

    @Test
    fun testToString() {
        val drawable = BitmapDrawable(null, AndroidBitmap(100, 100))
        assertEquals(
            expected = "DrawableImage(drawable=${drawable.toLogString()}, shareable=true)",
            actual = DrawableImage(drawable).toString(),
        )
    }
}