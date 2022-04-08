package org.scp.gymlog.ui.common.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.WindowManager
import android.widget.EditText
import androidx.annotation.StringRes
import org.scp.gymlog.R
import java.util.function.Consumer

class EditTextDialogFragment constructor(
    @StringRes title: Int,
    override var initialValue: String,
    confirm: Consumer<String>,
    cancel: Runnable = Runnable {}
) : CustomDialogFragment<String>(title, confirm, cancel) {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_edit_text, null)
        val input: EditText = view.findViewById(R.id.dialogText)
        input.setText(initialValue)

        val builder = AlertDialog.Builder(activity)
        builder.setMessage(title)
            .setView(view)
            .setPositiveButton(R.string.button_confirm) { _,_ ->
                val text = input.text.toString()
                confirm.accept(text)
            }
            .setNegativeButton(R.string.button_cancel) { _,_ -> cancel.run() }

        val dialog: Dialog = builder.create()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        input.requestFocus()
        return dialog
    }
}