package org.scp.gymlog.util

import java.util.function.Supplier

open class SecondTickThread(private val onTickListener: Supplier<Boolean>) : Thread() {

    var onFinishListener: Runnable? = null

    override fun run() {
        try {
            while (onTickListener.get()) sleep(1000)
        } catch (e: InterruptedException) {
            // Thread interrupted
        } finally {
            onFinishListener?.run()
        }
    }
}