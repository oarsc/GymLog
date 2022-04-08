package org.scp.gymlog.room

import android.content.Context
import java.util.function.Consumer

object DBThread {
    fun run(context: Context, process: Consumer<AppDatabase>) {
        val database = Connection[context]
        Thread { process.accept(database) }.start()
    }
}