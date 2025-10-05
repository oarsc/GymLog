package org.oar.gymlog.ui.common.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import org.oar.gymlog.R
import org.oar.gymlog.model.Gym
import org.oar.gymlog.model.Training
import org.oar.gymlog.util.Data
import java.util.function.Consumer

class EditTrainingDialogFragment (
    @StringRes title: Int,
    override var initialValue: Training,
    private val canChangeGym: Boolean,
    confirm: Consumer<Training>,
    cancel: Runnable = Runnable {}
) : CustomDialogFragment<Training>(title, confirm, cancel) {

    private val dialogView: View by lazy {
        requireActivity().layoutInflater.inflate(R.layout.dialog_edit_training, null)
    }

    private val editGym: TextView by lazy { dialogView.findViewById(R.id.gym) }
    private val editNotes: EditText by lazy { dialogView.findViewById(R.id.notes) }
    private val clearButton: ImageView by lazy { dialogView.findViewById(R.id.clearButton) }
    private var gymValue: Gym = initialValue.gym

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        editNotes.setText(initialValue.note)

        editGym.text = gymValue.name
        editNotes.setText(initialValue.note)
        clearButton.setOnClickListener { editNotes.text.clear() }

        if (initialValue.end != null && canChangeGym) {
            editGym.setOnClickListener {

                val labels = Data.gyms
                    .map(Gym::name)
                    .toMutableList()

                val dialog = TextSelectDialogFragment(labels, gymValue.id - 1) { idx, _ ->
                    if (idx != MenuDialogFragment.DIALOG_CLOSED) {
                        gymValue = Data.getGym(idx + 1)
                        editGym.text = gymValue.name
                    }
                }
                dialog.show(requireActivity().supportFragmentManager, null)

            }
        }

        val builder = AlertDialog.Builder(activity)
        builder.setMessage(title)
            .setView(dialogView)
            .setPositiveButton(R.string.button_confirm, null)
            .setNegativeButton(R.string.button_cancel) { _,_ -> cancel.run() }

        return builder.create().apply {
            setOnShowListener {
                val button = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
                button.setOnClickListener{
                    confirmDialog()
                    dismiss()
                }
            }
        }
    }

    private fun confirmDialog() {
        initialValue.gym = gymValue
        initialValue.note = editNotes.text.toString().trim()
        confirm.accept(initialValue)
    }
}