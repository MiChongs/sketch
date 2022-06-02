package com.github.panpf.sketch.decode

import android.graphics.Bitmap
import com.github.panpf.sketch.datasource.DataFrom
import com.github.panpf.sketch.decode.internal.exifOrientationName
import com.github.panpf.sketch.util.toInfoString
import java.util.LinkedList

/**
 * The result of [BitmapDecoder.decode]
 */
data class BitmapDecodeResult constructor(
    val bitmap: Bitmap,
    val imageInfo: ImageInfo,
    /**
     * @see androidx.exifinterface.media.ExifInterface.ORIENTATION_UNDEFINED
     * @see androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
     * @see androidx.exifinterface.media.ExifInterface.ORIENTATION_FLIP_HORIZONTAL
     * @see androidx.exifinterface.media.ExifInterface.ORIENTATION_FLIP_VERTICAL
     * @see androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90
     * @see androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180
     * @see androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270
     * @see androidx.exifinterface.media.ExifInterface.ORIENTATION_TRANSPOSE
     * @see androidx.exifinterface.media.ExifInterface.ORIENTATION_TRANSVERSE
     */
    val imageExifOrientation: Int,
    val dataFrom: DataFrom,
    val transformedList: List<Transformed>? = null
) {

    fun newResult(bitmap: Bitmap, block: (Builder.() -> Unit)? = null): BitmapDecodeResult =
        Builder(
            bitmap = bitmap,
            imageInfo = imageInfo,
            imageExifOrientation = imageExifOrientation,
            dataFrom = dataFrom,
            transformedList = transformedList?.toMutableList()
        ).apply {
            block?.invoke(this)
        }.build()

    override fun toString(): String =
        "BitmapDecodeResult(bitmap=${bitmap.toInfoString()}, " +
                "imageInfo=$imageInfo, " +
                "exifOrientation=${exifOrientationName(imageExifOrientation)}, " +
                "dataFrom=$dataFrom, " +
                "transformedList=$transformedList)"

    class Builder(
        private val bitmap: Bitmap,
        private var imageInfo: ImageInfo,
        /**
         * @see androidx.exifinterface.media.ExifInterface.ORIENTATION_UNDEFINED
         * @see androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
         * @see androidx.exifinterface.media.ExifInterface.ORIENTATION_FLIP_HORIZONTAL
         * @see androidx.exifinterface.media.ExifInterface.ORIENTATION_FLIP_VERTICAL
         * @see androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90
         * @see androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180
         * @see androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270
         * @see androidx.exifinterface.media.ExifInterface.ORIENTATION_TRANSPOSE
         * @see androidx.exifinterface.media.ExifInterface.ORIENTATION_TRANSVERSE
         */
        private val imageExifOrientation: Int,
        private val dataFrom: DataFrom,
        private var transformedList: MutableList<Transformed>? = null
    ) {

        fun imageInfo(imageInfo: ImageInfo): Builder = apply {
            this.imageInfo = imageInfo
        }

        fun addTransformed(transformed: Transformed): Builder = apply {
            if (this.transformedList?.find { it.key == transformed.key } == null) {
                this.transformedList = (this.transformedList ?: LinkedList()).apply {
                    add(transformed)
                }
            }
        }

        fun build(): BitmapDecodeResult = BitmapDecodeResult(
            bitmap = bitmap,
            imageInfo = imageInfo,
            imageExifOrientation = imageExifOrientation,
            dataFrom = dataFrom,
            transformedList = transformedList?.toList()
        )
    }
}