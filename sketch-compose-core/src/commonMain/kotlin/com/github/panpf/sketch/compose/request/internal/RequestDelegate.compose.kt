package com.github.panpf.sketch.compose.request.internal

import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.compose.target.ComposeTarget
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.internal.BaseRequestDelegate
import kotlinx.coroutines.Job

class ComposeTargetRequestDelegate(
    sketch: Sketch,
    initialRequest: ImageRequest,
    target: ComposeTarget,
    job: Job
) : BaseRequestDelegate(sketch, initialRequest, target, job) {

    override fun assertActive() {
        // AsyncImageState will only execute the request when it is remembered, so there is no need to judge whether it has been remembered.
    }

    override fun finish() {
        // Monitoring of TargetLifecycle cannot be removed here.
        // Because GenericComposeTarget needs to stop or start animation by listening to TargetLifecycle Image
    }
}