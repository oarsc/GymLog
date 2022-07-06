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
import org.scp.gymlog.util.FormatUtils.bigDecimal
import org.scp.gymlog.util.FormatUtils.integer
import org.scp.gymlog.util.FormatUtils.safeBigDecimal
import org.scp.gymlog.util.WeightUtils
import org.scp.gymlog.util.WeightUtils.calculate
import org.scp.gymlog.util.WeightUtils.calculateTotal
import org.scp.gymlog.util.WeightUtils.defaultScaled
import org.scp.gymlog.util.WeightUtils.toKilograms
import org.scp.gymlog.util.WeightUtils.toPounds
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
        editWeight.bigDecimal = weight

        if (initialValue.weight.internationalSystem != internationalSystem) {
            val savedWeight = getBigDecimalInitialWeight(
                initialValue.weight.internationalSystem)
            converted.bigDecimal = savedWeight
        } else {
            updateConvertedWeight(weight, converted)
        }

        editWeight.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val newWeight = s.toString().safeBigDecimal()
                updateConvertedWeight(newWeight, converted)
            }
        })

        val modifier: NumberModifierView = view.findViewById(R.id.weightModifier)
        modifier.setStep(exercise.step)

        val editReps: EditText = view.findViewById(R.id.editReps)
        editReps.integer = initialValue.reps

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
                val totalWeight = Weight(editWeight.bigDecimal, internationalSystem).calculateTotal(
                    exercise.weightSpec,
                    exercise.bar)

                initialValue.weight = totalWeight
                initialValue.reps = editReps.integer
                initialValue.note = editNotes.text.toString()
                initialValue.instant = instantSwitch.isChecked
                confirm.accept(initialValue)
            }
            .setNegativeButton(R.string.button_cancel) { _,_ -> cancel.run() }

        return builder.create()
    }

    private fun updateConvertedWeight(value: BigDecimal, label: TextView) {
        val convertedValue = if (internationalSystem)
                value.toPounds()
            else
                value.toKilograms()

        label.bigDecimal = convertedValue
    }

    private fun getBigDecimalInitialWeight(internationalSystem: Boolean): BigDecimal {
        return initialValue.weight.calculate(
            exercise.weightSpec,
            exercise.bar).defaultScaled(internationalSystem)
    }
}