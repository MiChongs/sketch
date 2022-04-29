package com.github.panpf.sketch.util

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.View
import com.github.panpf.sketch.R
import com.github.panpf.sketch.drawable.SketchCountBitmapDrawable
import com.github.panpf.sketch.drawable.SketchDrawable
import com.github.panpf.sketch.drawable.internal.CrossfadeDrawable
import com.github.panpf.sketch.request.DisplayResult
import com.github.panpf.sketch.request.internal.ViewTargetRequestManager

class SketchUtils {
    companion object {
        private fun requestManagerOrNull(view: View): ViewTargetRequestManager? =
            view.getTag(R.id.sketch_request_manager) as ViewTargetRequestManager?

        fun dispose(view: View) = requestManagerOrNull(view)?.dispose()

        fun getResult(view: View): DisplayResult? = requestManagerOrNull(view)?.getResult()

        fun restart(view: View) = requestManagerOrNull(view)?.restart()
    }
}

/**
 * Find the last [SketchDrawable] from the specified Drawable
 */
@SuppressLint("RestrictedApi")
fun Drawable.findLastSketchDrawable(): SketchDrawable? {
    val drawable = this
    return when {
        drawable is CrossfadeDrawable -> {
            drawable.end?.findLastSketchDrawable()
        }
        drawable is LayerDrawable -> {
            drawable.getLastChildDrawable()?.findLastSketchDrawable()
        }
        drawable is SketchDrawable -> drawable
        drawable is androidx.appcompat.graphics.drawable.DrawableWrapper -> {
            drawable.wrappedDrawable?.findLastSketchDrawable()
        }
        VERSION.SDK_INT >= VERSION_CODES.M && drawable is android.graphics.drawable.DrawableWrapper -> {
            drawable.drawable?.findLastSketchDrawable()
        }
        else -> null
    }
}

/**
 * Traverse all SketchCountBitmapDrawable in specified Drawable
 */
@SuppressLint("RestrictedApi")
fun Drawable.foreachSketchCountDrawable(block: (SketchCountBitmapDrawable) -> Unit) {
    val drawable = this
    when {
        drawable is SketchCountBitmapDrawable -> {
            block(drawable)
        }
        drawable is LayerDrawable -> {
            val layerCount = drawable.numberOfLayers
            for (index in 0 until layerCount) {
                drawable.getDrawable(index).foreachSketchCountDrawable(block)
            }
        }
        drawable is CrossfadeDrawable -> {
            drawable.end?.foreachSketchCountDrawable(block)
        }
        drawable is androidx.appcompat.graphics.drawable.DrawableWrapper -> {
            drawable.wrappedDrawable?.foreachSketchCountDrawable(block)
        }
        VERSION.SDK_INT >= VERSION_CODES.M && drawable is android.graphics.drawable.DrawableWrapper -> {
            drawable.drawable?.foreachSketchCountDrawable(block)
        }
    }
}