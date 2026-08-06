package org.oar.gymlog.ui.common.components

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.appcompat.widget.AppCompatEditText

class CustomEditText(context: Context, attrs: AttributeSet?) : AppCompatEditText(context, attrs) {
    private var setOnBackKeyboardListener: (() -> Boolean)? = null

    fun setOnBackKeyboardListener(listener: () -> Boolean) {
        setOnBackKeyboardListener = listener
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            if (setOnBackKeyboardListener?.invoke() == false) return true
        }
        return super.dispatchKeyEvent(event)
    }
}