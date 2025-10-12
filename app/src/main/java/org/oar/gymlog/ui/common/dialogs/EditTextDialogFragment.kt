package org.oar.gymlog.ui.common.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.StringRes
import org.oar.gymlog.R
import org.oar.gymlog.databinding.DialogEditTextBinding
import java.util.function.Consumer


class EditTextDialogFragment(
    @StringRes title: Int,
    override var initialValue: String = "",
    confirm: Consumer<String>,
    cancel: Runnable = Runnable {}
) : CustomDialogFragment<String>(title, confirm, cancel) {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogEditTextBinding.inflate(layoutInflater)

        binding.dialogText.setText(initialValue)

        return AlertDialog.Builder(activity)
            .setTitle(title)
            .setView(binding.root)
            .setPositiveButton(R.string.button_confirm) { _,_ ->
                val text = binding.dialogText.text.toString()
                confirm.accept(text)
            }
            .setNegativeButton(R.string.button_cancel) { _,_ -> cancel.run() }
            .create()
            .apply {
                window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            }
            .also {
                binding.dialogText.requestFocus()
            }
    }
}