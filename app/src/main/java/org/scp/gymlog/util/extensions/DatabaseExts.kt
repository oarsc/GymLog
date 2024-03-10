package org.scp.gymlog.util.extensions

import android.app.Activity
import android.content.Context
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.room.Room
import org.scp.gymlog.room.AppDatabase
import java.util.function.Consumer

object DatabaseExts {

    private var db: AppDatabase? = null

    fun Context.getDbConnection() : AppDatabase {
        return db ?: Room.databaseBuilder(this, AppDatabase::class.java, "gymlog-db")
            .build()
            .also { db = it }
    }

    fun Context.dbThread(process: Consumer<AppDatabase>) {
        val database = getDbConnection()

        if (Looper.getMainLooper().isCurrentThread) {
            Thread { process.accept(database) }.apply { start() }
        } else {
            process.accept(database)
        }
    }

    fun Fragment.dbThread(process: Consumer<AppDatabase>) = requireActivity().dbThread(process)
    fun View.dbThread(process: Consumer<AppDatabase>) = (context as Activity).dbThread(process)
}