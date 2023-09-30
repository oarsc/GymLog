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
import org.scp.gymlog.model.Weight
import org.scp.gymlog.room.daos.BitDao
import org.scp.gymlog.ui.common.components.NumberModifierView
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.FormatUtils.bigDecimal
import org.scp.gymlog.util.FormatUtils.integer
import org.scp.gymlog.util.FormatUtils.safeBigDecimal
import org.scp.gymlog.util.WeightUtils
import org.scp.gymlog.util.WeightUtils.calculate
import org.scp.gymlog.util.WeightUtils.calculateTotal
import org.scp.gymlog.util.WeightUtils.defaultScaled
import org.scp.gymlog.util.WeightUtils.toKilograms
import org.scp.gymlog.util.WeightUtils.toPounds
import org.scp.gymlog.util.extensions.DatabaseExts.dbThread
import org.scp.gymlog.util.extensions.MessagingExts.toast
import java.math.BigDecimal
import java.util.function.Consumer

class EditBitLogDialogFragment (
    @StringRes title: Int,
    private val enableInstantSwitch: Boolean,
    private val internationalSystem: Boolean,
    override var initialValue: Bit,
    confirm: Consumer<Bit>,
    cancel: Runnable = Runnable {}
) : CustomDialogFragment<Bit>(title, confirm, cancel) {

    private val dialogView: View by lazy {
        requireActivity().layoutInflater.inflate(R.layout.dialog_edit_bit_log, null)
    }

    private val editNotes: EditText by lazy { dialogView.findViewById(R.id.editNotes) }
    private val editWeight: EditText by lazy { dialogView.findViewById(R.id.editWeight) }
    private val editReps: EditText by lazy { dialogView.findViewById(R.id.editReps) }
    private val editSuperSet: EditText by lazy { dialogView.findViewById(R.id.editSuperSet) }
    private val instantSwitch: SwitchMaterial by lazy { dialogView.findViewById(R.id.instantSwitch) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        editNotes.setText(initialValue.note)

        val unit = dialogView.findViewById<TextView>(R.id.unit)
        unit.setText(WeightUtils.unit(internationalSystem))
        val convertedUnit = dialogView.findViewById<TextView>(R.id.convertUnit)
        convertedUnit.setText(WeightUtils.unit(!internationalSystem))

        editNotes.setOnClickListener {
            val dialog = EditNotesDialogFragment(
                R.string.text_notes,
                initialValue.variation,
                editNotes.text.toString())
                { text -> editNotes.setText(text) }

            dialog.show(childFragmentManager, null)
        }

        dialogView.findViewById<View>(R.id.clearButton).setOnClickListener { editNotes.text.clear() }

        val converted = dialogView.findViewById<TextView>(R.id.converted)

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

        val modifier = dialogView.findViewById<NumberModifierView>(R.id.weightModifier)
        modifier.setStep(initialValue.variation.step)

        editReps.integer = initialValue.reps

        if (initialValue.superSet > 0) {
            editSuperSet.integer = initialValue.superSet
        }

        if (enableInstantSwitch) {
            instantSwitch.isChecked = initialValue.instant
        } else {
            instantSwitch.isEnabled = false
            instantSwitch.isChecked = false
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


                    if (initialValue.superSet == editSuperSet.integer) {
                        confirmDialog()
                        dismiss()
                    } else {
                        dbThread { db ->
                            val canUpdate = validateBitEdit(db.bitDao())
                            if (canUpdate) {
                                confirmDialog()
                                dismiss()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun validateBitEdit(bitDao: BitDao): Boolean {
        val superSet = editSuperSet.integer
        val trainingBits = bitDao.getHistoryByTrainingId(initialValue.trainingId)

        val index = trainingBits.indices.find { trainingBits[it].bitId == initialValue.id }
            ?: throw RuntimeException("Bit not found on its own training... Id:${initialValue.id} #${initialValue.trainingId}")

        val nextSs = if (index < trainingBits.size - 1) trainingBits[index + 1].superSet else 0
        val prevSs = if (index > 0) trainingBits[index - 1].superSet else 0

        if (superSet == 0) {
            if (nextSs == prevSs && nextSs != 0) {
                toast(R.string.validation_super_set_must_be_in_touch)
                return false
            }
        } else {

            if (trainingBits.any { it.superSet == superSet }) {
                if (nextSs != superSet && prevSs != superSet) {
                    toast(R.string.validation_super_set_must_be_in_touch)
                    return false
                }
            }
            if (nextSs == prevSs && nextSs != 0 && nextSs != superSet) {
                toast(R.string.validation_super_set_must_be_in_touch)
                return false
            }
        }

        if (index == trainingBits.size - 1 && initialValue.trainingId == Data.training?.id) {
            Data.superSet?.also { activeSs ->
                if (activeSs == prevSs) {
                    toast(R.string.validation_super_set_active)
                    return false
                }
            }
        }

        return true
    }

    private fun confirmDialog() {
        initialValue.superSet = editSuperSet.integer
        initialValue.reps = editReps.integer
        initialValue.note = editNotes.text.toString()
        initialValue.instant = instantSwitch.isChecked
        initialValue.weight = Weight(editWeight.bigDecimal, internationalSystem).calculateTotal(
            initialValue.variation.weightSpec,
            initialValue.variation.bar)

        confirm.accept(initialValue)
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
            initialValue.variation.weightSpec,
            initialValue.variation.bar).defaultScaled(internationalSystem)
    }
}