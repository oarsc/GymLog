package org.oar.gymlog.ui.common.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.annotation.StringRes
import org.oar.gymlog.R
import org.oar.gymlog.databinding.DialogEditTrainingBinding
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

    private lateinit var binding: DialogEditTrainingBinding
    private var gymValue = initialValue.gym

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogEditTrainingBinding.inflate(layoutInflater)

        binding.gym.text = gymValue.name
        binding.notes.setText(initialValue.note)
        binding.clearButton.setOnClickListener {
            binding.notes.text.clear()
        }

        if (initialValue.end != null && canChangeGym) {
            binding.gym.setOnClickListener {

                val labels = Data.gyms
                    .map(Gym::name)
                    .toMutableList()

                val dialog = TextSelectDialogFragment(labels, gymValue.id - 1) { idx, _ ->
                    if (idx != MenuDialogFragment.DIALOG_CLOSED) {
                        gymValue = Data.getGym(idx + 1)
                        binding.gym.text = gymValue.name
                    }
                }
                dialog.show(requireActivity().supportFragmentManager, null)
            }
        }

        return AlertDialog.Builder(activity)
            .setTitle(title)
            .setView(binding.root)
            .setPositiveButton(R.string.button_confirm, null)
            .setNegativeButton(R.string.button_cancel) { _,_ -> cancel.run() }
            .create()
            .apply {
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
        initialValue.note = binding.notes.text.toString().trim()
        confirm.accept(initialValue)
    }
}