package org.scp.gymlog.util

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.function.Consumer

/**
 * Alternative to deprecated AsyncTask API
 */
class TaskRunner {
    private val executor: Executor = Executors.newSingleThreadExecutor() // change according to your requirements
    private val handler = Handler(Looper.getMainLooper())

    fun <R> executeAsync(callable: Callable<R>, callback: Consumer<R>) {
        executor.execute {
            try {
                val result = callable.call()
                handler.post { callback.accept(result) }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }
}