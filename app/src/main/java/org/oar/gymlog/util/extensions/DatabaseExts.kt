package org.oar.gymlog.util.extensions

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.oar.gymlog.room.AppDatabase
import org.oar.gymlog.util.SafeLooper
import java.util.function.Consumer

object DatabaseExts {

    var dbLooper = SafeLooper("database-thread")

    private var _db: AppDatabase? = null
    val Context.db : AppDatabase
        get() = _db ?: Room.databaseBuilder(this, AppDatabase::class.java, "gymlog-db")
            .build()
            .also { _db = it }

    fun Context.dbThread(process: Consumer<AppDatabase>) {
        if (dbLooper.isCurrentThread) {
            process.accept(db)
        } else {
            dbLooper.post { process.accept(db) }
        }
    }

    fun Fragment.dbThread(process: Consumer<AppDatabase>) = requireActivity().dbThread(process)
    fun View.dbThread(process: Consumer<AppDatabase>) = (context as Activity).dbThread(process)

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun Context.dbThreadSuspend(process: (AppDatabase) -> Unit) =
        suspendCancellableCoroutine { cont ->

            fun execute() {
                try {
                    process(db)
                    cont.resume(Unit) {}
                } catch (e: Exception) {
                    cont.cancel(e)
                }
            }

            if (dbLooper.isCurrentThread) {
                execute()
            } else {
                dbLooper.post { execute() }
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun Context.dbThreadSuspendable(process: suspend (AppDatabase) -> Unit) =
        suspendCancellableCoroutine { cont ->

            fun execute() {
                CoroutineScope(cont.context).launch {
                    try {
                        process(db)
                        cont.resume(Unit) {}
                    } catch (e: Exception) {
                        cont.cancel(e)
                    }
                }
            }

            if (dbLooper.isCurrentThread) {
                execute()
            } else {
                dbLooper.post { execute() }
            }
        }
}