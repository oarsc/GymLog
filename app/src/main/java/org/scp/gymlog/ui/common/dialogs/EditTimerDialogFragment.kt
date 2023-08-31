package org.scp.gymlog.ui.common.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager
import org.scp.gymlog.R
import org.scp.gymlog.model.Variation
import org.scp.gymlog.service.NotificationService.Companion.lastEndTime
import org.scp.gymlog.ui.preferences.PreferencesDefinition
import org.scp.gymlog.util.Constants.DATE_ZERO
import org.scp.gymlog.util.DateUtils.currentDateTime
import org.scp.gymlog.util.DateUtils.diffSeconds
import org.scp.gymlog.util.DateUtils.isPast
import org.scp.gymlog.util.FormatUtils.integer
import org.scp.gymlog.util.SecondTickThread
import org.scp.gymlog.util.extensions.PreferencesExts.loadString
import java.time.LocalDateTime
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Consumer

class EditTimerDialogFragment(
    private val ctx: Context,
    @StringRes title: Int,
    variation: Variation,
    confirm: Consumer<Int>
) : CustomDialogFragment<Int>(title, confirm, Runnable {}) {

    override var initialValue: Int = 60

    var onStopListener: Runnable? = null
    var onPlayListener: BiConsumer<LocalDateTime, Int>? = null
    private var countdownThread: CountdownThread? = null
    private lateinit var currentTimer: TextView
    private lateinit var secondLabel: TextView
    private lateinit var stopButton: ImageView
    private lateinit var plusButton: ImageView
    private lateinit var minusButton: ImageView
    private val defaultValue: Int
    private var isDefaultValue = false

    init {
        defaultValue = ctx.loadString(PreferencesDefinition.DEFAULT_REST_TIME).toInt()

        val restTime = variation.restTime
        if (restTime < 0) {
            isDefaultValue = true
            initialValue = defaultValue
        } else {
            isDefaultValue = false
            initialValue = restTime
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_edit_timer, null)

        currentTimer = view.findViewById(R.id.currentTimer)
        secondLabel = view.findViewById(R.id.secondLabel)
        stopButton = view.findViewById(R.id.stopButton)
        minusButton = view.findViewById(R.id.minusTenButton)
        plusButton = view.findViewById(R.id.plusTenButton)

        if (lastEndTime.isPast) {
            uiStopCounter()

        } else {
            countdownThread = CountdownThread(activity as Activity)
                .also(Thread::start)
        }

        val editNotes = view.findViewById<EditText>(R.id.editTimer)
        editNotes.setText(initialValue.toString())
        editNotes.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (isDefaultValue) {
                    isDefaultValue = false
                    setInputAlpha(view, 1f)
                }
            }
        })
        if (isDefaultValue) setInputAlpha(view, 0.4f)

        stopButton.setOnClickListener {
            countdownThread?.interrupt() ?: uiStopCounter()
            onStopListener?.run()
        }

        view.findViewById<View>(R.id.playButton).setOnClickListener {
            val seconds = editNotes.integer
            val endingCountdown = currentDateTime().plusSeconds(seconds.toLong())
            showCurrentCountdownButtons()

            onPlayListener?.accept(endingCountdown, seconds)

            if (countdownThread == null) {
                countdownThread = CountdownThread(activity as Activity)
                    .also(Thread::start)
            }
        }

        minusButton.setOnClickListener {
            if (countdownThread != null) {
                onPlayListener?.accept(DATE_ZERO, -10)
                countdownThread?.onTick()
            }
        }

        plusButton.setOnClickListener {
            if (countdownThread != null) {
                onPlayListener?.accept(DATE_ZERO, 10)
                countdownThread?.onTick()
            }
        }

        val builder = AlertDialog.Builder(activity)
        builder.setMessage(title)
            .setView(view)
            .setPositiveButton(R.string.button_confirm) { _,_ ->
                val seconds = editNotes.integer
                if (seconds == defaultValue && isDefaultValue) {
                    confirm.accept(-1)
                } else {
                    confirm.accept(seconds)
                }
            }
            .setNeutralButton(R.string.text_default) { _,_ -> confirm.accept(-1) }
            .setNegativeButton(R.string.button_cancel) { _,_ -> cancel.run() }

        return builder.create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        countdownThread?.interrupt()
    }

    private fun setInputAlpha(view: View, alpha: Float) {
        listOf(R.id.editTimer, R.id.inputSecondsLabel)
            .map { id -> view.findViewById<View>(id) }
            .forEach { v -> v.alpha = alpha }
    }

    private fun uiStopCounter() {
        showCurrentCountdownButtons(false)
        secondLabel.visibility = View.GONE
        currentTimer.text = ctx.resources.getString(R.string.text_none).lowercase(Locale.getDefault())
    }

    private fun showCurrentCountdownButtons(show: Boolean = true) {
        listOf(stopButton, plusButton, minusButton)
            .forEach { it.visibility = if (show) View.VISIBLE else View.GONE}
    }

    private inner class CountdownThread(val activity: Activity) : SecondTickThread() {

        override fun onTick(): Boolean {
            if (lastEndTime.isPast)
                return false

            val seconds = lastEndTime.diffSeconds()
            activity.runOnUiThread {
                currentTimer.integer = seconds
                secondLabel.visibility = View.VISIBLE
            }
            return true
        }

        override fun onFinish() {
            activity.runOnUiThread { uiStopCounter() }
            countdownThread = null
        }
    }
}