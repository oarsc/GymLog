package org.scp.gymlog.ui.common

import android.os.Bundle
import org.scp.gymlog.room.AppDatabase
import org.scp.gymlog.util.extensions.DatabaseExts.dbThread
import org.scp.gymlog.util.extensions.MessagingExts.toast

abstract class DBAppCompatActivity : CustomAppCompatActivity() {

    companion object {
        const val CONTINUE = 0
        const val ERROR_WITHOUT_MESSAGE = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbThread { db ->
            val result = onLoad(savedInstanceState, db)
            if (result == CONTINUE) {
                runOnUiThread { onDelayedCreate(savedInstanceState) }
            } else {
                if (result != ERROR_WITHOUT_MESSAGE) {
                    toast(result)
                }
                finish()
            }
        }
    }

    protected abstract fun onLoad(savedInstanceState: Bundle?, db: AppDatabase): Int
    protected abstract fun onDelayedCreate(savedInstanceState: Bundle?)
}