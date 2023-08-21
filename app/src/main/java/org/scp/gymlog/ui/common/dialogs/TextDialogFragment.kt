package org.scp.gymlog.ui.common.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import org.scp.gymlog.R
import java.util.function.Consumer

class TextDialogFragment(
    @param:StringRes private val title: Int,
    @param:StringRes private val text: Int,
    private val callback: Consumer<Boolean>
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_show_text, null)
        val textView = view.findViewById<TextView>(R.id.text)
        textView.setText(text)

        val builder = AlertDialog.Builder(activity)
        builder.setMessage(title)
            .setView(view)
            .setPositiveButton(R.string.button_confirm) { _,_ -> callback.accept(true) }
            .setNegativeButton(R.string.button_cancel) { _,_ -> callback.accept(false) }

        return builder.create()
    }
}