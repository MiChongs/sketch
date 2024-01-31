package com.github.panpf.sketch.request.internal

import androidx.annotation.MainThread
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.request.Disposable
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.sketch.request.ReusableDisposable
import com.github.panpf.sketch.util.getCompletedOrNull
import com.github.panpf.sketch.util.isMainThread
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Responsible for storing current requests and calling back events such as attach, detach.
 */
interface RequestManager {

    /** Attach [requestDelegate] to this view and cancel the old request. */
    @MainThread
    fun setRequest(requestDelegate: RequestDelegate?)

    /** Return 'true' if [disposable] is not attached to this view. */
    fun isDisposed(disposable: Disposable): Boolean

    /**
     * Create and return a new disposable unless this is a restarted request.
     */
    fun getDisposable(job: Deferred<ImageResult>): Disposable

    /** Cancel any in progress work and detach currentRequestDelegate from this view. */
    fun dispose()

    /** Return the completed value of the latest job if it has completed. Else, return 'null'. */
    fun getResult(): ImageResult?

    fun restart()

    fun getRequest(): ImageRequest?

    fun getSketch(): Sketch?
}

open class BaseRequestManager : RequestManager {

    // The disposable for the current request attached to this view.
    private var currentDisposable: ReusableDisposable? = null

    // A pending operation that is posting to the main thread to clear the current request.
    private var pendingClear: Job? = null

    // Only accessed from the main thread.
    protected var currentRequestDelegate: RequestDelegate? = null
    private var isRestart = false

    @MainThread
    override fun setRequest(requestDelegate: RequestDelegate?) {
        currentRequestDelegate?.dispose()
        currentRequestDelegate = requestDelegate
        callbackAttachedState()
    }

    @Synchronized
    override fun isDisposed(disposable: Disposable): Boolean {
        return disposable !== currentDisposable
    }

    @Synchronized
    override fun getDisposable(job: Deferred<ImageResult>): ReusableDisposable {
        // If this is a restarted request, update the current disposable and return it.
        val disposable = currentDisposable
        if (disposable != null && isMainThread() && isRestart) {
            isRestart = false
            disposable.job = job
            return disposable
        }

        // Cancel any pending clears since they were for the previous request.
        pendingClear?.cancel()
        pendingClear = null

        // Create a new disposable as this is a new request.
        return ReusableDisposable(this@BaseRequestManager, job).also {
            currentDisposable = it
        }
    }

    @Synchronized
    @OptIn(DelicateCoroutinesApi::class)
    override fun dispose() {
        pendingClear?.cancel()
        pendingClear = GlobalScope.launch(Dispatchers.Main.immediate) {
            setRequest(null)
        }
        currentDisposable = null
    }

    @Synchronized
    override fun getResult(): ImageResult? {
        return currentDisposable?.job?.getCompletedOrNull()
    }

    @Synchronized
    override fun restart() {
        val requestDelegate = currentRequestDelegate ?: return

        // As this is called from the main thread, isRestart will
        // be cleared synchronously as part of request.restart().
        isRestart = true
        requestDelegate.sketch.enqueue(requestDelegate.initialRequest)
    }

    @Synchronized
    override fun getRequest(): ImageRequest? {
        return currentRequestDelegate?.initialRequest
    }

    @Synchronized
    override fun getSketch(): Sketch? {
        return currentRequestDelegate?.sketch
    }

    open fun isAttached(): Boolean {
        return true
    }

    protected fun callbackAttachedState() {
        val currentRequestDelegate = currentRequestDelegate
        if (currentRequestDelegate is AttachObserver) {
            val isAttached = isAttached()
            currentRequestDelegate.onAttachedChanged(isAttached)
        }
    }
}