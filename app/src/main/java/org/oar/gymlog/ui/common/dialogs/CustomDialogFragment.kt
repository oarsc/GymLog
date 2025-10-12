package org.oar.gymlog.ui.common.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import java.util.function.Consumer

abstract class CustomDialogFragment<T>(
    @param:StringRes protected var title: Int,
    protected var confirm: Consumer<T>,
    protected var cancel: Runnable
) : DialogFragment() {

    protected abstract var initialValue: T
    abstract override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
}