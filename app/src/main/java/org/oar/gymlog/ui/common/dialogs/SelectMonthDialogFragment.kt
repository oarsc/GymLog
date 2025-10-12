package org.oar.gymlog.ui.common.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import org.oar.gymlog.R
import org.oar.gymlog.databinding.DialogSelectMonthBinding
import org.oar.gymlog.ui.common.components.HistoryCalendarView.Companion.MONTH_NAMES
import org.oar.gymlog.util.DateUtils.NOW
import org.oar.gymlog.util.extensions.DatabaseExts.dbThread
import java.time.Month
import java.util.function.BiConsumer

class SelectMonthDialogFragment(
    @StringRes private val title: Int = R.string.dialog_title_select_month,
    private val startMonth: Int,
    private val startYear: Int,
    private val confirm: BiConsumer<Month, Int>,
    private val cancel: Runnable = Runnable {}
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogSelectMonthBinding.inflate(layoutInflater)

        binding.monthSelector.apply {
            wrapSelectorWheel = false
            minValue = 1
            maxValue = 12
            value = startMonth

            val context = requireContext()
            displayedValues = MONTH_NAMES.values
                .map(context::getString)
                .toTypedArray()
        }

        binding.yearSelector.apply {
            wrapSelectorWheel = false
            maxValue = NOW.year
            value = startYear
        }

        dbThread { db ->
            binding.yearSelector.minValue = (db.trainingDao().getFirstTrainingStartDate() ?: NOW).year
        }

        return AlertDialog.Builder(activity)
            .setTitle(title)
            .setView(binding.root)
            .setPositiveButton(R.string.button_confirm) { _, _ ->
                confirm.accept(Month.of(binding.monthSelector.value), binding.yearSelector.value)
            }
            .setNegativeButton(R.string.button_cancel) { _, _ -> cancel.run() }
            .create()
    }
}