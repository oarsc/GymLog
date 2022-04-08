package org.scp.gymlog.ui.common.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.StringRes
import com.google.android.material.switchmaterial.SwitchMaterial
import org.scp.gymlog.R
import org.scp.gymlog.model.Bit
import org.scp.gymlog.model.Exercise
import org.scp.gymlog.model.Weight
import org.scp.gymlog.ui.common.components.NumberModifierView
import org.scp.gymlog.util.FormatUtils
import org.scp.gymlog.util.WeightUtils
import java.math.BigDecimal
import java.util.function.Consumer

class EditBitLogDialogFragment @JvmOverloads constructor(
    @StringRes title: Int,
    private val exercise: Exercise,
    private val enableInstantSwitch: Boolean,
    private val internationalSystem: Boolean,
    override var initialValue: Bit,
    confirm: Consumer<Bit>,
    cancel: Runnable = Runnable {}
) : CustomDialogFragment<Bit>(title, confirm, cancel) {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_edit_bit_log, null)

        val editNotes: EditText = view.findViewById(R.id.editNotes)
        editNotes.setText(initialValue.note)

        val unit: TextView = view.findViewById(R.id.unit)
        unit.setText(WeightUtils.unit(internationalSystem))
        val convertedUnit: TextView = view.findViewById(R.id.convertUnit)
        convertedUnit.setText(WeightUtils.unit(!internationalSystem))

        editNotes.setOnClickListener {
            val dialog = EditNotesDialogFragment(
                R.string.text_notes,
                exercise.id,
                editNotes.text.toString())
                { text -> editNotes.setText(text) }

            dialog.show(childFragmentManager, null)
        }

        view.findViewById<View>(R.id.clearButton).setOnClickListener { editNotes.text.clear() }

        val editWeight: EditText = view.findViewById(R.id.editWeight)
        val converted: TextView = view.findViewById(R.id.converted)

        val weight = getBigDecimalInitialWeight(internationalSystem)
        editWeight.setText(FormatUtils.toString(weight))

        if (initialValue.weight.internationalSystem != internationalSystem) {
            val savedWeight = getBigDecimalInitialWeight(
                initialValue.weight.internationalSystem)
            converted.text = FormatUtils.toString(savedWeight)
        } else {
            updateConvertedWeight(weight, converted)
        }

        editWeight.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val newWeight = FormatUtils.toBigDecimal(s.toString())
                updateConvertedWeight(newWeight, converted)
            }
        })

        val modifier: NumberModifierView = view.findViewById(R.id.weightModifier)
        modifier.setStep(exercise.step)

        val editReps: EditText = view.findViewById(R.id.editReps)
        editReps.setText(java.lang.String.valueOf(initialValue.reps))

        val instantSwitch: SwitchMaterial = view.findViewById(R.id.instantSwitch)
        if (enableInstantSwitch) {
            instantSwitch.isChecked = initialValue.instant
        } else {
            instantSwitch.isEnabled = false
            instantSwitch.isChecked = false
        }

        val builder = AlertDialog.Builder(activity)
        builder.setMessage(title)
            .setView(view)
            .setPositiveButton(R.string.button_confirm) { _,_ ->
                val totalWeight = WeightUtils.getTotalWeight(
                    FormatUtils.toBigDecimal(editWeight.text.toString()),
                    exercise.weightSpec,
                    exercise.bar,
                    internationalSystem)

                initialValue.weight = Weight(totalWeight, internationalSystem)
                initialValue.reps = FormatUtils.toInt(editReps.text.toString())
                initialValue.note = editNotes.text.toString()
                initialValue.instant = instantSwitch.isChecked
                confirm.accept(initialValue)
            }
            .setNegativeButton(R.string.button_cancel) { _,_ -> cancel.run() }

        return builder.create()
    }

    private fun updateConvertedWeight(value: BigDecimal, label: TextView) {
        val convertedValue = if (internationalSystem)
                WeightUtils.toPounds(value)
            else
                WeightUtils.toKilograms(value)

        label.text = FormatUtils.toString(convertedValue)
    }

    private fun getBigDecimalInitialWeight(internationalSystem: Boolean): BigDecimal {
        return WeightUtils.getWeightFromTotalDefaultScaled(
            initialValue.weight,
            exercise.weightSpec,
            exercise.bar,
            internationalSystem)
    }
}