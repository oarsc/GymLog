package org.oar.gymlog.ui.common.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.BOTTOM
import androidx.constraintlayout.widget.ConstraintSet.LEFT
import androidx.constraintlayout.widget.ConstraintSet.RIGHT
import androidx.constraintlayout.widget.ConstraintSet.TOP
import org.oar.gymlog.R
import org.oar.gymlog.ui.common.components.NumberModifierView
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
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_edit_number, null)
        val input = view.findViewById<EditText>(R.id.dialogText)
        val modifier = view.findViewById<NumberModifierView>(R.id.modifier)
        input.bigDecimal = initialValue

        if (allowNegatives) {
            modifier.allowNegatives()
            input.inputType = InputType.TYPE_CLASS_NUMBER or
                    InputType.TYPE_NUMBER_FLAG_DECIMAL or
                    InputType.TYPE_NUMBER_FLAG_SIGNED
        }

        if (!showButtons) {
            modifier.visibility = View.INVISIBLE

            val constraintLayout = view.findViewById<ConstraintLayout>(R.id.parentLayout)
            val constraintSet = ConstraintSet()
            constraintSet.clone(constraintLayout)

            listOf(RIGHT, LEFT, TOP, BOTTOM)
                .forEach { constraintSet.connect(R.id.dialogText, it, R.id.parentLayout, it, 0) }

            constraintSet.applyTo(constraintLayout)
        }

        val builder = AlertDialog.Builder(activity)
        builder.setMessage(title)
            .setView(view)
            .setPositiveButton(R.string.button_confirm) { _,_ ->
                val value = input.text.toString()
                confirm.accept(value.safeBigDecimal())
            }
            .setNegativeButton(R.string.button_cancel) { _,_ -> cancel.run() }

        val dialog: Dialog = builder.create()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        input.requestFocus()
        return dialog
    }
}