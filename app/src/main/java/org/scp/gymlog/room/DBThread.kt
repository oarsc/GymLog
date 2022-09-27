package org.scp.gymlog.room

import android.content.Context
import java.util.function.Consumer

object DBThread {
    fun run(context: Context, process: Consumer<AppDatabase>): Thread {
        val database = Connection[context]
        return Thread { process.accept(database) }.apply {
            start()
        }
    }
}