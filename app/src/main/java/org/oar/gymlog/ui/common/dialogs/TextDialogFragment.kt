package org.oar.gymlog.ui.common.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import org.oar.gymlog.R
import org.oar.gymlog.databinding.DialogShowTextBinding
import java.util.function.Consumer

class TextDialogFragment(
    @param:StringRes private val title: Int,
    @param:StringRes private val textId: Int,
    private val callback: Consumer<Boolean>
) : DialogFragment() {

    var customText: String? = null

    constructor(
        @StringRes title: Int,
        text: String,
        callback: Consumer<Boolean>
    ): this(title, -1, callback) {
        customText = text
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogShowTextBinding.inflate(layoutInflater)

        binding.text.text = customText ?: getString(textId)

        return AlertDialog.Builder(activity)
            .setTitle(title)
            .setView(binding.root)
            .setPositiveButton(R.string.button_confirm) { _,_ -> callback.accept(true) }
            .setNegativeButton(R.string.button_cancel) { _,_ -> callback.accept(false) }
            .create()
    }
}