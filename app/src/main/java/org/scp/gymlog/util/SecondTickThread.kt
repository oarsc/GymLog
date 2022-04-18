package org.scp.gymlog.util

abstract class SecondTickThread : Thread() {

    open fun onStart() {}
    abstract fun onTick() : Boolean
    open fun onFinish() {}

    override fun run() {
        onStart()
        try {
            while (onTick()) sleep(1000)
        } catch (e: InterruptedException) {
            // Thread interrupted
        } finally {
            onFinish()
        }
    }
}