package org.oar.gymlog.manager.custom

import kotlin.js.Promise

class Thread<T : Any>(
    private val runnable: () -> T
) {
    fun start() =
        Promise { resolve, _ ->
            resolve(runnable())
        }
}