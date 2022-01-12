package com.github.panpf.sketch.fetch

import com.github.panpf.sketch.datasource.DataSource
import com.github.panpf.sketch.request.DataFrom

/**
 * The result of [Fetcher.fetch]
 */
class FetchResult(val dataSource: DataSource) {

    val from: DataFrom = dataSource.from

    override fun toString(): String {
        return "FetchResult(source=$dataSource)"
    }
}
