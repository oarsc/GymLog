package org.oar.gymlog.ui.common.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.annotation.StringRes
import org.oar.gymlog.R
import org.oar.gymlog.databinding.DialogEditWeightBinding
import org.oar.gymlog.model.Bar
import org.oar.gymlog.model.ExerciseType
import org.oar.gymlog.model.Weight
import org.oar.gymlog.model.WeightSpecification
import org.oar.gymlog.ui.common.dialogs.model.WeightFormData
import org.oar.gymlog.util.Constants
import org.oar.gymlog.util.Data
import org.oar.gymlog.util.FormatUtils.bigDecimal
import org.oar.gymlog.util.FormatUtils.safeBigDecimal
import org.oar.gymlog.util.FormatUtils.toLocaleString
import org.oar.gymlog.util.WeightUtils
import org.oar.gymlog.util.WeightUtils.calculateTotal
import org.oar.gymlog.util.WeightUtils.convertWeight
import org.oar.gymlog.util.WeightUtils.toKilograms
import org.oar.gymlog.util.WeightUtils.toPounds
import org.oar.gymlog.util.extensions.MessagingExts.toast
import java.math.BigDecimal
import java.util.function.Consumer

class EditWeightFormDialogFragment(
    override var initialValue: WeightFormData,
    @StringRes title: Int, confirm: Consumer<WeightFormData>,
    cancel: Runnable = Runnable {}
) : CustomDialogFragment<WeightFormData>(title, confirm, cancel) {

    private lateinit var binding: DialogEditWeightBinding
    private val initialWeight: Weight = initialValue.weight!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogEditWeightBinding.inflate(layoutInflater)
        binding.setInitialData()

        return AlertDialog.Builder(activity)
            .setTitle(title)
            .setView(binding.root)
            .setPositiveButton(R.string.button_confirm) { _,_ -> confirm.accept(confirmData()) }
            .setNegativeButton(R.string.button_cancel) { _,_ -> cancel.run() }
            .create()
    }

    private fun DialogEditWeightBinding.setInitialData() {
        if (initialWeight.value.compareTo(BigDecimal.ZERO) != 0) {
            weightValue.bigDecimal = initialWeight.value
        }
        updateConvertedUnit(initialWeight.value)

        val unitResString = WeightUtils.unit(initialWeight.internationalSystem)
        unit.setText(unitResString)
        totalUnit.setText(unitResString)

        val unitResConvertString = WeightUtils.unit(!initialWeight.internationalSystem)
        convertUnit.setText(unitResConvertString)
        convertTotalUnit.setText(unitResConvertString)

        weightValue.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        weightValue.filters = arrayOf(InputFilter { source, _, _, dest, _, _ ->
                val input = (dest.toString() + source.toString()).safeBigDecimal()
                if (input < Constants.ONE_THOUSAND && input.scale() < 3)
                    null
                else
                    ""
            })
        weightValue.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val newWeight = s.toString().safeBigDecimal()
                updateConvertedUnit(newWeight)
                updateTotalWeight(newWeight)
            }
        })

        // STEP SELECTABLE
        stepBox.setOnClickListener {
            val popup = PopupMenu(activity, stepBox)
            val menu = popup.menu
            Data.STEPS_KG.forEach { size: Int ->
                menu.add(0, size, size,
                    BigDecimal.valueOf(size.toLong()).divide(Constants.ONE_HUNDRED).toLocaleString()
                )
            }

            popup.setOnMenuItemClickListener { menuItem ->
                val id = menuItem.itemId
                val newStep = BigDecimal.valueOf(id.toLong()).divide(Constants.ONE_HUNDRED)
                if (newStep.compareTo(initialValue.step) != 0) {
                    initialValue.step = newStep
                    initialValue.exerciseUpdated = true
                    updateStep()
                }
                true
            }
            popup.show()
        }
        updateStep()

        // BARS SELECTABLE
        if (initialValue.type != ExerciseType.BARBELL && initialValue.bar == null) {
            barsBox.visibility = View.GONE
        }

        barsBox.setOnClickListener {
            val popup = PopupMenu(activity, barsBox)
            val menu = popup.menu

            var order = 0
            menu.add(0, -1, order++, R.string.text_no_bar)
            for (bar in Data.bars) {
                menu.add(0, bar.id, order++, getWeightLabel(bar.weight))
            }

            popup.setOnMenuItemClickListener { item: MenuItem ->
                val id = item.itemId
                val lastBar = initialValue.bar
                if (id < 0) {
                    if (lastBar != null) {
                        initialValue.bar = null
                        initialValue.exerciseUpdated = true
                        if (initialValue.type == ExerciseType.BARBELL) {
                            incompatibleBar.visibility = View.VISIBLE
                            toast(R.string.validation_should_have_bar)
                        } else {
                            incompatibleBar.visibility = View.INVISIBLE
                        }

                        updateConfig(lastBar = lastBar)

                        //updateSelectedBar(lastBar)
                    }
                } else {
                    val bar = Data.getBar(id)
                    if (lastBar != bar) {
                        initialValue.bar = bar
                        initialValue.exerciseUpdated = true
                        if (initialValue.type == ExerciseType.BARBELL) {
                            incompatibleBar.visibility = View.INVISIBLE
                        } else {
                            incompatibleBar.visibility = View.VISIBLE
                            toast(R.string.validation_shouldnt_have_bar)
                        }
                        updateConfig(lastBar = lastBar)
                    }
                }
                true
            }
            popup.show()
        }

        // WEIGHTS CONFIG SELECTABLE
        if (initialValue.type.weightModes.size == 1 && initialValue.type.weightModes.contains(initialValue.weightSpec)) {
            weightsConfigBox.visibility = View.GONE
        }

        weightsConfigBox.setOnClickListener {
            val popup = PopupMenu(activity, weightsConfigBox)
            popup.setOnMenuItemClickListener { menuItem ->
                val newWeightSpec = when(menuItem.itemId) {
                    R.id.total -> WeightSpecification.TOTAL_WEIGHT
                    R.id.noBar -> WeightSpecification.NO_BAR_WEIGHT
                    R.id.oneSide -> WeightSpecification.ONE_SIDE_WEIGHT
                    else -> return@setOnMenuItemClickListener false
                }

                if (newWeightSpec !== initialValue.weightSpec) {
                    val lastWeightSpec = initialValue.weightSpec
                    initialValue.weightSpec = newWeightSpec
                    initialValue.exerciseUpdated = true
                    updateConfig(lastWeightSpec)
                }
                true
            }
            popup.inflate(R.menu.weight_specification_menu)

            val specsOrdinals = initialValue.type.weightModes.map { it.ordinal }
            WeightSpecification.entries.indices
                .filter { !specsOrdinals.contains(it) }
                .mapNotNull {
                    when(it) {
                        0 -> R.id.total
                        1 -> R.id.noBar
                        2 -> R.id.oneSide
                        else -> null
                    }
                }
                .forEach { popup.menu.findItem(it).isVisible = false }

            popup.show()
        }

        updateConfig()
    }

    private fun DialogEditWeightBinding.updateStep() {
        step.bigDecimal = initialValue.step!!.also { step ->
            step.divide(Constants.TWO).also {
                binding.modifier.setStep(if (it.scale() > 2) step else it)
            }
        }
    }

    private fun DialogEditWeightBinding.updateConfig(
        lastWeightSpecification: WeightSpecification = initialValue.weightSpec,
        lastBar: Bar? = initialValue.bar
    ) {
        // Bar
        val bar = initialValue.bar
        if (bar == null) {
            barUsed.setText(R.string.symbol_hyphen)
            incompatibleBar.visibility =
                if (initialValue.type == ExerciseType.BARBELL) View.VISIBLE else View.INVISIBLE
        } else {
            barUsed.text = getWeightLabel(bar.weight)
            incompatibleBar.visibility =
                if (initialValue.type == ExerciseType.BARBELL) View.INVISIBLE else View.VISIBLE
        }

        // Weight
        val weightSpecification = initialValue.weightSpec
        weightSpec.setText(weightSpecification.literal)
        weightSpecIcon.setImageResource(weightSpecification.icon)

        // Recalcs
        weightValue.bigDecimal = convertWeight(
            weight = Weight(weightValue.bigDecimal, true),
            iWeightSpec = lastWeightSpecification,
            iBar = lastBar,
            fWeightSpec = weightSpecification,
            fBar = bar
        ).getValue(true)

        updateTotalWeight()
    }

    private fun getWeightLabel(weight: Weight): StringBuilder {
        return if (weight.internationalSystem)
            StringBuilder(weight.toKg().toLocaleString()).append(" kg")
        else
            StringBuilder(weight.toLbs().toLocaleString()).append(" lbs")
    }

    private fun updateConvertedUnit(value: BigDecimal) {
        val convertedValue = if (initialWeight.internationalSystem)
                value.toPounds()
            else
                value.toKilograms()

        binding.converted.bigDecimal = convertedValue
    }

    private fun updateTotalWeight(totalValue: BigDecimal = binding.weightValue.bigDecimal) {
        val totalWeight = Weight(totalValue, initialWeight.internationalSystem).calculateTotal(
            initialValue.weightSpec,
            initialValue.bar)

        binding.totalWeight.bigDecimal = totalWeight.getValue(initialWeight.internationalSystem)
        binding.convertedTotal.bigDecimal = totalWeight.getValue(!initialWeight.internationalSystem)
    }

    private fun confirmData(): WeightFormData = initialValue.apply {
            weight = Weight(
                value = binding.weightValue.bigDecimal,
                internationalSystem = initialWeight.internationalSystem
            )
        }
}