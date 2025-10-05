package org.oar.gymlog.ui.common.dialogs

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
import org.oar.gymlog.R
import org.oar.gymlog.model.Variation
import org.oar.gymlog.service.NotificationService
import org.oar.gymlog.ui.main.preferences.PreferencesDefinition
import org.oar.gymlog.util.DateUtils.diffSeconds
import org.oar.gymlog.util.DateUtils.isSystemTimePast
import org.oar.gymlog.util.FormatUtils.integer
import org.oar.gymlog.util.SecondTickThread
import org.oar.gymlog.util.extensions.PreferencesExts.loadString
import java.util.Locale
import java.util.function.BiConsumer
import java.util.function.Consumer

class EditTimerDialogFragment(
    private val ctx: Context,
    @StringRes title: Int,
    variation: Variation,
    confirm: Consumer<Int>
) : CustomDialogFragment<Int>(title, confirm, Runnable {}) {

    override var initialValue: Int = 60

    private var countdownThread: CountdownThread? = null
    private lateinit var currentTimer: TextView
    private lateinit var secondLabel: TextView
    private lateinit var stopButton: ImageView
    private lateinit var plusButton: ImageView
    private lateinit var minusButton: ImageView
    private val defaultValue: Int
    private var isDefaultValue = false

    private var onStopListener: Runnable? = null
    private var onPlayListener: BiConsumer<Long, Int>? = null
    private var onAddTimeListener: Consumer<Int>? = null

    fun setOnStopListener(onStopListener: Runnable) {
        this.onStopListener = onStopListener
    }

    fun setOnPlayListener(onPlayListener: BiConsumer<Long, Int>) {
        this.onPlayListener = onPlayListener
    }

    fun setOnAddTimeListener(onAddTimeListener: Consumer<Int>) {
        this.onAddTimeListener = onAddTimeListener
    }

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

        if (NotificationService.lastEndTime > 0) {
            countdownThread = CountdownThread(activity as Activity)
                .also(Thread::start)
        } else {
            uiStopCounter()
            showCurrentCountdownButtons(false)
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
            countdownThread?.interrupt()
            onStopListener?.run()
            uiStopCounter()
            showCurrentCountdownButtons(false)
        }

        view.findViewById<View>(R.id.playButton).setOnClickListener {
            val seconds = editNotes.integer
            val endingCountdown = System.currentTimeMillis() + seconds * 1000L
            showCurrentCountdownButtons()

            onPlayListener?.accept(endingCountdown, seconds)

            if (countdownThread == null) {
                countdownThread = CountdownThread(activity as Activity)
                    .also(Thread::start)
            }
        }

        minusButton.setOnClickListener {
            onAddTimeListener?.accept(-10)
            countdownThread?.onTick()
        }

        plusButton.setOnClickListener {
            onAddTimeListener?.accept(10)
            countdownThread?.onTick()
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
        secondLabel.visibility = View.GONE
        currentTimer.text = ctx.resources.getString(R.string.text_none).lowercase(Locale.getDefault())
    }

    private fun showCurrentCountdownButtons(show: Boolean = true) {
        listOf(stopButton, plusButton, minusButton)
            .forEach { it.visibility = if (show) View.VISIBLE else View.GONE}
    }

    private inner class CountdownThread(val activity: Activity) : SecondTickThread() {
        override fun onTick(): Boolean {
            if (NotificationService.lastEndTime <= 0)
                return false

            val seconds = NotificationService.lastEndTime.diffSeconds()

            activity.runOnUiThread {
                currentTimer.integer = if (NotificationService.lastEndTime.isSystemTimePast) -seconds else seconds
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