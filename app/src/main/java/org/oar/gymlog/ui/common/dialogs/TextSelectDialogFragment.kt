package org.oar.gymlog.ui.common.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oar.gymlog.databinding.DialogMenuBinding
import java.util.function.BiConsumer

class TextSelectDialogFragment(
    private val texts: List<String>,
    private val selectedOption: Int = -1,
    private val callback: BiConsumer<Int, String>,
) : DialogFragment() {

    private var callbackCalled = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogMenuBinding.inflate(layoutInflater)

        binding.parentLayout.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = TextSelectRecyclerViewAdapter(
                texts = texts,
                selectedOption = selectedOption,
                onClick = ::onMenuElementClicked
            )
        }

        return AlertDialog.Builder(activity)
            .setView(binding.root)
            .create()
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

    companion object {
        const val DIALOG_CLOSED = -1
    }
}
