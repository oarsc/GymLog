package org.scp.gymlog.ui.common.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.StringRes
import org.scp.gymlog.R
import org.scp.gymlog.model.Weight
import org.scp.gymlog.model.WeightSpecification
import org.scp.gymlog.ui.common.components.NumberModifierView
import org.scp.gymlog.ui.common.dialogs.model.WeightFormData
import org.scp.gymlog.util.Constants
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.FormatUtils.bigDecimal
import org.scp.gymlog.util.FormatUtils.safeBigDecimal
import org.scp.gymlog.util.FormatUtils.toLocaleString
import org.scp.gymlog.util.WeightUtils
import org.scp.gymlog.util.WeightUtils.toKilograms
import org.scp.gymlog.util.WeightUtils.toPounds
import java.math.BigDecimal
import java.util.function.Consumer

class EditWeightFormDialogFragment(
    override var initialValue: WeightFormData,
    @StringRes title: Int, confirm: Consumer<WeightFormData>,
    cancel: Runnable = Runnable {}
) : CustomDialogFragment<WeightFormData>(title, confirm, cancel) {

    private lateinit var input: EditText
    private lateinit var convertValue: TextView
    private lateinit var totalValue: TextView
    private lateinit var totalConvertValue: TextView
    private lateinit var barUsed: TextView
    private lateinit var weightSpec: TextView
    private lateinit var weightSpecIcon: ImageView
    private lateinit var incompatibleBar: View
    private lateinit var step: TextView
    private lateinit var modifier: NumberModifierView
    private var weight: Weight = initialValue.weight!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_edit_weight, null)

        setInitialData(view)

        val builder = AlertDialog.Builder(activity)
        builder.setMessage(title)
            .setView(view)
            .setPositiveButton(R.string.button_confirm) { _,_ -> confirm.accept(confirmData()) }
            .setNegativeButton(R.string.button_cancel) { _,_ -> cancel.run() }
        return builder.create()
    }

    private fun setInitialData(view: View) {
        input = view.findViewById(R.id.weightValue)
        convertValue = view.findViewById(R.id.converted)
        totalValue = view.findViewById(R.id.totalWeight)
        totalConvertValue = view.findViewById(R.id.convertedTotal)
        barUsed = view.findViewById(R.id.barUsed)
        weightSpec = view.findViewById(R.id.weightSpec)
        weightSpecIcon = view.findViewById(R.id.weightSpecIcon)
        incompatibleBar = view.findViewById(R.id.incompatibleBar)
        step = view.findViewById(R.id.step)
        modifier = view.findViewById(R.id.modifier)

        if (weight.value.compareTo(BigDecimal.ZERO) != 0) {
            input.bigDecimal = weight.value
        }
        updateConvertedUnit(weight.value)

        val unitResString = WeightUtils.unit(weight.internationalSystem)
        listOf(R.id.unit, R.id.totalUnit)
            .map { id -> view.findViewById<TextView>(id) }
            .forEach { textView -> textView.setText(unitResString) }

        val unitResConvertString = WeightUtils.unit(!weight.internationalSystem)
        listOf(R.id.convertUnit, R.id.convertTotalUnit)
            .map { id -> view.findViewById<TextView>(id) }
            .forEach { textView -> textView.setText(unitResConvertString) }

        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.filters = arrayOf(InputFilter { source, _, _, dest, _, _ ->
                val input = (dest.toString() + source.toString()).safeBigDecimal()
                if (input < Constants.ONE_THOUSAND && input.scale() < 3)
                    null
                else
                    ""
            })
        input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val newWeight = s.toString().safeBigDecimal()
                updateConvertedUnit(newWeight)
                updateTotalWeight(newWeight)
            }
        })

        val layoutStep: View = view.findViewById(R.id.stepBox)
        layoutStep.setOnClickListener {
            val popup = PopupMenu(activity, layoutStep)
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

        val layoutBars: View = view.findViewById(R.id.barsBox)
        layoutBars.setOnClickListener {
            val popup = PopupMenu(activity, layoutBars)
            val menu = popup.menu

            var order = 0
            menu.add(0, -1, order++, R.string.text_no_bar)
            for (bar in Data.bars) {
                menu.add(0, bar.id, order++, getWeightLabel(bar.weight))
            }

            popup.setOnMenuItemClickListener { item: MenuItem ->
                val id = item.itemId
                if (id < 0) {
                    if (initialValue.bar != null) {
                        initialValue.bar = null
                        initialValue.exerciseUpdated = true
                        if (initialValue.requiresBar) {
                            incompatibleBar.visibility = View.VISIBLE
                            Toast.makeText(context, R.string.validation_should_have_bar,
                                Toast.LENGTH_LONG).show()
                        } else {
                            incompatibleBar.visibility = View.INVISIBLE
                        }

                        if (initialValue.weightSpec === WeightSpecification.TOTAL_WEIGHT) {
                            initialValue.weightSpec = WeightSpecification.NO_BAR_WEIGHT
                            updateWeightSpec()
                        }

                        updateSelectedBar()
                    }
                } else {
                    val bar = Data.getBar(id)
                    if (initialValue.bar != bar) {
                        initialValue.bar = bar
                        initialValue.exerciseUpdated = true
                        if (initialValue.requiresBar) {
                            incompatibleBar.visibility = View.INVISIBLE
                        } else {
                            incompatibleBar.visibility = View.VISIBLE
                            Toast.makeText(context, R.string.validation_shouldnt_have_bar,
                                Toast.LENGTH_LONG).show()
                        }
                        updateSelectedBar()
                    }
                }
                true
            }
            popup.show()
        }
        updateSelectedBar()

        val layoutWeightSpec: View = view.findViewById(R.id.weightsConfigBox)
        layoutWeightSpec.setOnClickListener {
            val popup = PopupMenu(activity, layoutWeightSpec)
            popup.setOnMenuItemClickListener { menuItem ->
                val newWeightSpec = when(menuItem.itemId) {
                    R.id.total -> WeightSpecification.TOTAL_WEIGHT
                    R.id.noBar -> WeightSpecification.NO_BAR_WEIGHT
                    R.id.oneSide -> WeightSpecification.ONE_SIDE_WEIGHT
                    else -> return@setOnMenuItemClickListener false
                }

                if (newWeightSpec !== initialValue.weightSpec) {
                    initialValue.weightSpec = newWeightSpec
                    initialValue.exerciseUpdated = true
                    updateWeightSpec()
                }
                true
            }
            popup.inflate(R.menu.weight_specification_menu)

            if (initialValue.bar == null) {
                popup.menu.findItem(R.id.total).isVisible = false
            }

            popup.show()
        }
        updateWeightSpec()
    }

    private fun updateStep() {
        modifier.setStep(initialValue.step!!)
        step.bigDecimal = initialValue.step!!
    }

    private fun updateSelectedBar() {
        val bar = initialValue.bar
        if (bar == null) {
            barUsed.setText(R.string.symbol_hyphen)
            incompatibleBar.visibility =
                if (initialValue.requiresBar) View.VISIBLE else View.INVISIBLE
        } else {
            barUsed.text = getWeightLabel(bar.weight)
            incompatibleBar.visibility =
                if (initialValue.requiresBar) View.INVISIBLE else View.VISIBLE
        }
        updateTotalWeight(null)
    }

    private fun updateWeightSpec() {
        val weightSpecification = initialValue.weightSpec
        weightSpec.setText(weightSpecification!!.literal)
        weightSpecIcon.setImageResource(weightSpecification.icon)
        updateTotalWeight(null)
    }

    private fun getWeightLabel(weight: Weight): StringBuilder {
        return if (this.weight.internationalSystem)
            StringBuilder(weight.toKg().toLocaleString()).append(" kg")
        else
            StringBuilder(weight.toLbs().toLocaleString()).append(" lbs")
    }

    private fun updateConvertedUnit(value: BigDecimal) {
        val convertedValue = if (weight.internationalSystem)
                value.toPounds()
            else
                value.toKilograms()

        convertValue.bigDecimal = convertedValue
    }

    private fun updateTotalWeight(totalValue: BigDecimal?) {
        val value = totalValue ?: input.bigDecimal

        val totalWeight = WeightUtils.getTotalWeight(value,
            initialValue.weightSpec,
            initialValue.bar,
            weight.internationalSystem)

        this.totalValue.bigDecimal = totalWeight

        if (weight.internationalSystem) {
            totalConvertValue.bigDecimal = totalWeight.toPounds()
        } else {
            totalConvertValue.bigDecimal = totalWeight.toKilograms()
        }
    }

    private fun confirmData(): WeightFormData {
        initialValue.weight = Weight(
            input.bigDecimal,
            weight.internationalSystem
        ).also { weight = it }
        return initialValue
    }
}