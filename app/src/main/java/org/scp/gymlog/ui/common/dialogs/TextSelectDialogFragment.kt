package org.scp.gymlog.ui.common.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.R
import java.util.function.BiConsumer

class TextSelectDialogFragment(
    private val texts: List<String>,
    private val selectedOption: Int = -1,
    private val callback: BiConsumer<Int, String>,
) : DialogFragment() {

    companion object {
        const val DIALOG_CLOSED = -1
    }

    private var callbackCalled = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_menu, null)
        val recyclerView: RecyclerView = view.findViewById(R.id.parentLayout)

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = TextSelectRecyclerViewAdapter(texts, selectedOption) { idx, text ->
            onMenuElementClicked(idx, text)
        }

        val builder = AlertDialog.Builder(activity).setView(view)

        return builder.create()
    }

    private fun onMenuElementClicked(index: Int, text: String) {
        callback.accept(index, text)
        callbackCalled = true
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (!callbackCalled) {
            callback.accept(DIALOG_CLOSED, "")
        }
        super.onDismiss(dialog)
    }
}
