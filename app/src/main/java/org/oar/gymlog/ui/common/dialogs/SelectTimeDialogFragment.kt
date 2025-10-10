package org.oar.gymlog.ui.common.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import org.oar.gymlog.R
import org.oar.gymlog.util.Constants.NANO_MILLI
import java.time.LocalTime
import java.util.function.Consumer

class SelectTimeDialogFragment(
    @StringRes private val title: Int = R.string.dialog_title_select_time,
    private val time: LocalTime,
    private val showSeconds: Boolean = true,
    private val showMillis: Boolean = true,
    private val confirm: Consumer<LocalTime>,
    private val cancel: Runnable = Runnable {}
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_select_time, null)

        val hourSelector = view.findViewById<NumberPicker>(R.id.hourSelector)
        val minuteSelector = view.findViewById<NumberPicker>(R.id.minuteSelector)
        val secondSelector = view.findViewById<NumberPicker>(R.id.secondSelector)
        val milliSelector = view.findViewById<NumberPicker>(R.id.milliSelector)

        if (!showSeconds) {
            secondSelector.visibility = View.GONE
        }
        if (!showMillis) {
            milliSelector.visibility = View.GONE
        }

        hourSelector.setStyle(23, 2, time.hour)
        minuteSelector.setStyle(59, 2, time.minute)
        secondSelector.setStyle(59, 2, time.second)
        milliSelector.setStyle(999, 3, time.nano / NANO_MILLI)

        val builder = AlertDialog.Builder(activity)
        builder.setMessage(title)
            .setView(view)
            .setPositiveButton(R.string.button_confirm) { _, _ ->
                val time = LocalTime.of(
                    hourSelector.value,
                    minuteSelector.value,
                    secondSelector.value,
                    milliSelector.value * NANO_MILLI
                )
                confirm.accept(time)
            }
            .setNegativeButton(R.string.button_cancel) { _, _ -> cancel.run() }

        return builder.create()
    }

    private fun NumberPicker.setStyle(max: Int, digits: Int, value: Int) {
        minValue = 0
        maxValue = max
        displayedValues = Array(max+1) { String.format("%0${digits}d", it) }
        this.value = value
    }
}