package org.oar.gymlog.util

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import java.util.UUID

class SafeLooper(
    private val name: String
) {
    private var looper: Looper? = null
    private var handler: Handler? = null

    val isCurrentThread: Boolean
        get() = looper?.isCurrentThread ?: false

    fun post(runnable: () -> Unit) {
        handler?.apply {
            post(runnable)
            return
        }

        val looper = this.looper
            ?: HandlerThread("$name-${UUID.randomUUID()}")
                .apply { start() }
                .looper
                .also { looper = it }

        handler = Handler(looper).apply {
            post(runnable)
        }
    }

    fun quitSafely() {
        handler = null
        looper?.quitSafely()
        looper = null
    }
}
