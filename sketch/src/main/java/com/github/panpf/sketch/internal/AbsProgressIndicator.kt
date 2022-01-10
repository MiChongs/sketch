package com.github.panpf.sketch.internal

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.view.View
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.DisplayResult.Error
import com.github.panpf.sketch.request.DisplayResult.Success

abstract class AbsProgressIndicator : ProgressIndicator {

    private var show: Boolean = false
    private var view: View? = null
    private var progress: Float = -1F
        set(value) {
            field = value
            view?.postInvalidate()
        }
    private var progressAnimator: ValueAnimator? = null

    override fun onLayout(view: View) {
        this.view = view
        this.progress = progress
    }

    override fun onRequestStart(view: View, request: DisplayRequest) {
        this.view = view
        this.show = request.uriString.startsWith("http")
        this.progress = 0f
        this.progressAnimator?.cancel()
    }

    override fun onRequestError(view: View, request: DisplayRequest, result: Error) {
        this.view = view
        this.progress = -1f
        this.progressAnimator?.cancel()
    }

    override fun onRequestSuccess(
        view: View,
        request: DisplayRequest,
        result: Success
    ) {
        this.view = view
        val progressAnimator = progressAnimator
        if (progressAnimator?.isRunning == true) {
            progressAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    this@AbsProgressIndicator.progress = -1f
                }
            })
        } else {
            this.progress = -1f
        }
    }

    override fun onProgressChanged(
        view: View,
        request: DisplayRequest,
        totalLength: Long,
        completedLength: Long
    ) {
        this.view = view
        val lastProgress = progress.takeIf { it > 0 } ?: 0f
        val newProgress = if (totalLength > 0) completedLength.toFloat() / totalLength else 0f
        progressAnimator?.cancel()
        progressAnimator = ValueAnimator.ofFloat(lastProgress, newProgress).apply {
            addUpdateListener {
                progress = animatedValue as Float
            }
            duration = 150
        }
        progressAnimator?.start()
    }

    override fun onDraw(canvas: Canvas) {
        if (!show) return
        val view = view ?: return
        val progress = progress.takeIf { it >= 0f } ?: return
        canvas.save()
        drawIndicator(canvas, view, progress)
        canvas.restore()
    }

    abstract fun drawIndicator(canvas: Canvas, view: View, progress: Float)
}