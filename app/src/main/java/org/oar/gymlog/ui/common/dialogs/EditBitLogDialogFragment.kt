package org.oar.gymlog.ui.common.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import org.oar.gymlog.R
import org.oar.gymlog.model.Bit
import org.oar.gymlog.model.Weight
import org.oar.gymlog.room.daos.BitDao
import org.oar.gymlog.room.entities.BitEntity
import org.oar.gymlog.ui.common.components.NumberModifierView
import org.oar.gymlog.util.Constants
import org.oar.gymlog.util.Constants.NANO_MILLI_L
import org.oar.gymlog.util.Data
import org.oar.gymlog.util.DateUtils.NOW
import org.oar.gymlog.util.DateUtils.getTimeMillisString
import org.oar.gymlog.util.FormatUtils.bigDecimal
import org.oar.gymlog.util.FormatUtils.integer
import org.oar.gymlog.util.FormatUtils.safeBigDecimal
import org.oar.gymlog.util.WeightUtils
import org.oar.gymlog.util.WeightUtils.calculate
import org.oar.gymlog.util.WeightUtils.calculateTotal
import org.oar.gymlog.util.WeightUtils.defaultScaled
import org.oar.gymlog.util.WeightUtils.toKilograms
import org.oar.gymlog.util.WeightUtils.toPounds
import org.oar.gymlog.util.extensions.ComponentsExts.runOnUiThread
import org.oar.gymlog.util.extensions.DatabaseExts.dbThread
import org.oar.gymlog.util.extensions.MessagingExts.toast
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.function.BiConsumer
import java.util.function.Consumer


class EditBitLogDialogFragment (
    @StringRes title: Int,
    private val enableInstantSwitch: Boolean,
    private val internationalSystem: Boolean,
    override var initialValue: Bit,
    private val confirmListener: BiConsumer<Bit, Boolean>,
    private val cancelListener: Consumer<Boolean> = Consumer {}
) : CustomDialogFragment<Bit>(title, {}, {}) {

    private val dialogView: View by lazy {
        requireActivity().layoutInflater.inflate(R.layout.dialog_edit_bit_log, null)
    }

    private val editNotes: EditText by lazy { dialogView.findViewById(R.id.editNotes) }
    private val editWeight: EditText by lazy { dialogView.findViewById(R.id.editWeight) }
    private val editReps: EditText by lazy { dialogView.findViewById(R.id.editReps) }
    private val editSuperSet: EditText by lazy { dialogView.findViewById(R.id.editSuperSet) }
    private val editTimestamp: EditText by lazy { dialogView.findViewById(R.id.editTimestamp) }
    private val instantSwitch: SwitchMaterial by lazy { dialogView.findViewById(R.id.instantSwitch) }

    private var timestampSelected = initialValue.timestamp
    private var callbackCalled = false
    private var cloned = false
    private var variation = initialValue.variation

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

        editTimestamp.setText(initialValue.timestamp.getTimeMillisString())
        editTimestamp.setOnClickListener {
            showDateTimePickerDialog(timestampSelected) { newDateTime ->
                timestampSelected = newDateTime
                editTimestamp.setText(newDateTime.getTimeMillisString())
            }
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
            .setNeutralButton(R.string.button_clone, null) // if we set here the listener, the dialog will be automatically closed


        return builder.create().apply {
            setOnShowListener {
                val button = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
                button.setOnClickListener {
                    if (initialValue.superSet == editSuperSet.integer) {
                        confirmDialog()
                    } else {
                        dbThread { db ->
                            val canUpdate = validateBitEdit(db.bitDao())
                            if (canUpdate) {
                                confirmDialog()
                            }
                        }
                    }
                }

                val neutralButton = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_NEUTRAL)
                neutralButton.setOnClickListener { v ->
                    if (!cloned) dbThread { db ->
                        val bitDao = db.bitDao()

                        val nextTimestamp = bitDao.getNext(initialValue.timestamp)?.timestamp

                        if (nextTimestamp != null && nextTimestamp <= initialValue.timestamp.plusNanos(NANO_MILLI_L)) {
                            toast(R.string.validation_cannot_clone)
                            return@dbThread
                        }

                        if (timestampSelected == initialValue.timestamp) {
                            timestampSelected = initialValue.timestamp.plusNanos(NANO_MILLI_L)
                        } else if (nextTimestamp != null && timestampSelected >= nextTimestamp) {
                            timestampSelected = nextTimestamp.minusNanos(NANO_MILLI_L)
                        }

                        cloned = true
                        runOnUiThread {
                            editTimestamp.setText(timestampSelected.getTimeMillisString())
                            neutralButton.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (!callbackCalled) {
            cancelListener.accept(cloned)
        }
        super.onDismiss(dialog)
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
        dbThread { db ->
            val bitDao = db.bitDao()

            val minimumTimestamp = bitDao.getPrevious(initialValue.timestamp)
                ?.timestamp?.plusNanos(NANO_MILLI_L)
            val maximumTimestamp = bitDao.getNext(initialValue.timestamp)
                ?.timestamp?.minusNanos(NANO_MILLI_L) ?: NOW

            if (cloned) {
                val newBitEntity = initialValue.toEntity()
                    .apply {
                        bitId = 0
                        applyData()
                        timestamp = minimumTimestamp?.let(timestamp::coerceAtLeast) ?: timestamp
                        timestamp = timestamp.coerceAtMost(maximumTimestamp)
                    }

                newBitEntity.bitId = bitDao.insert(newBitEntity).toInt()
                confirmListener.accept(Bit(newBitEntity), true)
                callbackCalled = true
                dismiss()
            } else {
                initialValue.applyData()
                initialValue.apply {
                    timestamp = minimumTimestamp?.let(timestamp::coerceAtLeast) ?: timestamp
                    timestamp = timestamp.coerceAtMost(maximumTimestamp)
                }

                bitDao.update(initialValue.toEntity())
                confirmListener.accept(initialValue, cloned)
                callbackCalled = true
                dismiss()
            }
        }
    }

    private fun Bit.applyData() {
        superSet = editSuperSet.integer
        reps = editReps.integer
        note = editNotes.text.toString()
        instant = instantSwitch.isChecked
        timestamp = timestampSelected
        weight = Weight(editWeight.bigDecimal, internationalSystem).calculateTotal(
            variation.weightSpec, variation.bar)
    }

    private fun BitEntity.applyData() {
        superSet = editSuperSet.integer
        reps = editReps.integer
        note = editNotes.text.toString()
        instant = instantSwitch.isChecked
        timestamp = timestampSelected
        totalWeight = Weight(editWeight.bigDecimal, internationalSystem).calculateTotal(
            variation.weightSpec, variation.bar).value.multiply(Constants.ONE_HUNDRED).toInt()
        kilos = internationalSystem
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

    fun showDateTimePickerDialog(
        initialDateTime: LocalDateTime = LocalDateTime.now(),
        onDateTimeSelected: (LocalDateTime) -> Unit
    ) {
        val dialog = SelectTimeDialogFragment(
            time = initialDateTime.toLocalTime(),
            confirm = { time ->
                onDateTimeSelected(
                    LocalDateTime.of(initialDateTime.toLocalDate(), time)
                )
            }
        )

        val context = requireContext()
        context as FragmentActivity
        dialog.show(context.supportFragmentManager, null)
    }
}