package com.github.panpf.sketch.test.utils

import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.UriInvalidException
import com.github.panpf.sketch.state.ConditionStateImage

data object UriInvalidCondition : ConditionStateImage.Condition {

    override fun accept(request: ImageRequest, throwable: Throwable?): Boolean =
        throwable is UriInvalidException && request.uri.toString()
            .let { it.isEmpty() || it.isBlank() }
}