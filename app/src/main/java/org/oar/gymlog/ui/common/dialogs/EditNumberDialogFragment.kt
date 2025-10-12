package org.oar.gymlog.ui.common.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.WindowManager
import androidx.annotation.StringRes
import org.oar.gymlog.R
import org.oar.gymlog.databinding.DialogEditNumberBinding
import org.oar.gymlog.util.FormatUtils.bigDecimal
import org.oar.gymlog.util.FormatUtils.safeBigDecimal
import java.math.BigDecimal
import java.util.function.Consumer

class EditNumberDialogFragment @JvmOverloads constructor(
    @StringRes title: Int,
    initialValueStr: String,
    confirm: Consumer<BigDecimal>,
    cancel: Runnable = Runnable {}
) : CustomDialogFragment<BigDecimal>(title, confirm, cancel) {

    override var initialValue: BigDecimal = initialValueStr.safeBigDecimal()
    var allowNegatives = false
    var showButtons = true

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogEditNumberBinding.inflate(layoutInflater)

        binding.dialogText.bigDecimal = initialValue

        if (allowNegatives) {
            binding.modifier.allowNegatives()
            binding.dialogText.inputType = InputType.TYPE_CLASS_NUMBER or
                    InputType.TYPE_NUMBER_FLAG_DECIMAL or
                    InputType.TYPE_NUMBER_FLAG_SIGNED
        }

        if (!showButtons) {
            binding.modifier.visibility = View.GONE
        }

        return AlertDialog.Builder(activity)
            .setTitle(title)
            .setView(binding.root)
            .setPositiveButton(R.string.button_confirm) { _,_ ->
                val value = binding.dialogText.text.toString()
                confirm.accept(value.safeBigDecimal())
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