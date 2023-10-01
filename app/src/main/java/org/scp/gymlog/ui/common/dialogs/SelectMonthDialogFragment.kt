package org.scp.gymlog.ui.common.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.NumberPicker
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import org.scp.gymlog.R
import org.scp.gymlog.ui.common.components.HistoryCalendarView.Companion.MONTH_NAMES
import org.scp.gymlog.util.DateUtils.NOW
import org.scp.gymlog.util.extensions.DatabaseExts.dbThread
import java.time.Month
import java.util.function.BiConsumer
import java.util.function.Consumer

class SelectMonthDialogFragment(
    @StringRes private val title: Int,
    private val startMonth: Int,
    private val startYear: Int,
    private val confirm: BiConsumer<Month, Int>,
    private val cancel: Runnable = Runnable {}
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_select_month, null)

        val monthSelector = view.findViewById<NumberPicker>(R.id.monthSelector)
        val yearSelector = view.findViewById<NumberPicker>(R.id.yearSelector)

        monthSelector.wrapSelectorWheel = false
        yearSelector.wrapSelectorWheel = false

        dbThread { db ->
            yearSelector.minValue = (db.trainingDao().getFirstTrainingStartDate() ?: NOW).year
            yearSelector.maxValue = NOW.year
            yearSelector.value = startYear

            monthSelector.minValue = 1
            monthSelector.maxValue = 12
            monthSelector.value = startMonth

            context?.also { context ->
                monthSelector.displayedValues = MONTH_NAMES.values
                    .map(context::getString)
                    .toTypedArray()
            }
        }

        val builder = AlertDialog.Builder(activity)
        builder.setMessage(title)
            .setView(view)
            .setPositiveButton(R.string.button_confirm) { _, _ ->
                confirm.accept(Month.of(monthSelector.value), yearSelector.value)
            }
            .setNegativeButton(R.string.button_cancel) { _, _ -> cancel.run() }

        return builder.create()
    }
}