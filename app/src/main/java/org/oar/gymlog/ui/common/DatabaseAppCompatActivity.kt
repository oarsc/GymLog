package org.oar.gymlog.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import org.oar.gymlog.room.AppDatabase
import org.oar.gymlog.util.extensions.DatabaseExts.dbLooper
import org.oar.gymlog.util.extensions.DatabaseExts.dbThread
import org.oar.gymlog.util.extensions.MessagingExts.toast

abstract class DatabaseAppCompatActivity<T: ViewBinding>(
    inflater: ((LayoutInflater) -> T)
) : BindingAppCompatActivity<T>(inflater) {

    companion object {
        const val CONTINUE = 0
        const val ERROR_WITHOUT_MESSAGE = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.root.alpha = 0f

        preLoad(savedInstanceState)
        dbThread { db ->
            val result = onLoad(savedInstanceState, db)
            if (result == CONTINUE) {
                runOnUiThread {
                    onDelayedCreate(savedInstanceState)
                    binding.root.animate().alpha(1f).setDuration(200).start()
                }
            } else {
                if (result != ERROR_WITHOUT_MESSAGE) {
                    toast(result)
                }
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dbLooper.quitSafely()
    }

    protected open fun preLoad(savedInstanceState: Bundle?) {}
    protected abstract fun onLoad(savedInstanceState: Bundle?, db: AppDatabase): Int
    protected abstract fun onDelayedCreate(savedInstanceState: Bundle?)
}