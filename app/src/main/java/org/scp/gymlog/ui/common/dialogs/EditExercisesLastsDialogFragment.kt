package org.scp.gymlog.ui.common.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import org.scp.gymlog.R
import org.scp.gymlog.exceptions.LoadException
import org.scp.gymlog.model.Bar
import org.scp.gymlog.model.Exercise
import org.scp.gymlog.model.Weight
import org.scp.gymlog.model.WeightSpecification
import org.scp.gymlog.room.AppDatabase
import org.scp.gymlog.room.DBThread
import org.scp.gymlog.util.Constants
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.FormatUtils.bigDecimal
import org.scp.gymlog.util.FormatUtils.toLocaleString
import java.math.BigDecimal
import java.util.function.Consumer


class EditExercisesLastsDialogFragment(
    @StringRes title: Int,
    override var initialValue: Exercise,
    private val internationalSystem: Boolean,
    confirm: Consumer<Exercise>,
    cancel: Runnable = Runnable {}
) : CustomDialogFragment<Exercise>(title, confirm, cancel) {

    private lateinit var barUsed: TextView
    private lateinit var weightSpecView: TextView
    private lateinit var weightSpecIcon: ImageView
    private lateinit var incompatibleBar: View
    private lateinit var stepView: TextView
    private var exerciseUpdate = false
    private var step: BigDecimal = initialValue.step
    private var bar: Bar? = initialValue.bar
    private var weightSpec: WeightSpecification = initialValue.weightSpec

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_edit_exercise_lasts, null)

        setInitialData(view)

        val builder = AlertDialog.Builder(activity)
        builder.setMessage(title)
            .setView(view)
            .setPositiveButton(R.string.button_confirm) { _,_ -> confirm() }
            .setNegativeButton(R.string.button_cancel) { _,_ -> cancel.run() }
        return builder.create()
    }

    private fun setInitialData(view: View) {
        barUsed = view.findViewById(R.id.barUsed)
        weightSpecView = view.findViewById(R.id.weightSpec)
        weightSpecIcon = view.findViewById(R.id.weightSpecIcon)
        incompatibleBar = view.findViewById(R.id.incompatibleBar)
        stepView = view.findViewById(R.id.step)

        val layoutStep: View = view.findViewById(R.id.stepBox)
        layoutStep.setOnClickListener {
            val popup =  PopupMenu(activity, layoutStep)
            val menu = popup.menu

            Data.STEPS_KG.forEach { size: Int ->
                menu.add(0, size, size,
                    BigDecimal.valueOf(size.toLong()).divide(Constants.ONE_HUNDRED).toLocaleString())
            }

            popup.setOnMenuItemClickListener { item: MenuItem ->
                val id = item.itemId
                val newStep = BigDecimal.valueOf(id.toLong()).divide(Constants.ONE_HUNDRED)
                if (newStep.compareTo(step) != 0) {
                    step = newStep
                    exerciseUpdate = true
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
                    if (bar != null) {
                        bar = null
                        exerciseUpdate = true
                        if (initialValue.requiresBar) {
                            incompatibleBar.visibility = View.VISIBLE
                            Toast.makeText(context, R.string.validation_should_have_bar,
                                Toast.LENGTH_LONG).show()
                        } else {
                            incompatibleBar.visibility = View.INVISIBLE
                        }

                        if (weightSpec === WeightSpecification.TOTAL_WEIGHT) {
                            weightSpec = WeightSpecification.NO_BAR_WEIGHT
                            updateWeightSpec()
                        }

                        updateSelectedBar()
                    }

                } else {
                    val bar = Data.getBar(id)
                    if (this.bar != bar) {
                        this.bar = bar
                        exerciseUpdate = true
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

                if (newWeightSpec !== weightSpec) {
                    weightSpec = newWeightSpec
                    exerciseUpdate = true
                    updateWeightSpec()
                }
                true
            }
            popup.inflate(R.menu.weight_specification_menu)

            if (bar == null) {
                popup.menu.findItem(R.id.total).isVisible = false
            }

            popup.show()
        }
        updateWeightSpec()
    }

    private fun updateStep() {
        stepView.bigDecimal = step
    }

    private fun updateSelectedBar() {
        if (bar == null) {
            barUsed.setText(R.string.symbol_hyphen)
            incompatibleBar.visibility =
                if (initialValue.requiresBar) View.VISIBLE else View.INVISIBLE
        } else {
            barUsed.text = getWeightLabel(bar!!.weight)
            incompatibleBar.visibility =
                if (initialValue.requiresBar) View.INVISIBLE else View.VISIBLE
        }
    }

    private fun updateWeightSpec() {
        weightSpecView.setText(weightSpec.literal)
        weightSpecIcon.setImageResource(weightSpec.icon)
    }

    private fun getWeightLabel(weight: Weight): StringBuilder {
        return if (internationalSystem) {
            StringBuilder(weight.toKg().toLocaleString())
                .append(" kg")
        } else {
            StringBuilder(weight.toLbs().toLocaleString())
                .append(" lbs")
        }
    }

    private fun confirm() {
        if (exerciseUpdate) {
            DBThread.run(requireContext()) { db: AppDatabase ->
                val exercises = db.exerciseDao().getById(initialValue.id)
                    .orElseThrow { LoadException("Didn't find exercise with id: " + initialValue.id) }

                exercises.lastBarId = bar?.id
                exercises.lastWeightSpec = weightSpec
                exercises.lastStep = step.multiply(Constants.ONE_HUNDRED).toInt()

                db.exerciseDao().update(exercises)

                initialValue.bar = bar
                initialValue.weightSpec = weightSpec
                initialValue.step = step
                confirm.accept(initialValue)
            }
        } else {
            cancel.run()
        }
    }
}