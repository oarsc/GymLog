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
import org.scp.gymlog.model.Exercise
import org.scp.gymlog.util.DateUtils
import org.scp.gymlog.util.FormatUtils
import org.scp.gymlog.util.SecondTickThread
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.Supplier

class EditTimerDialogFragment(
    private val ctx: Context,
    @StringRes title: Int,
    exercise: Exercise,
    private var endingCountdown: Calendar?,
    confirm: Consumer<Int>
) : CustomDialogFragment<Int>(title, confirm, Runnable {}) {

    override var initialValue: Int = 60

    var onStopListener: Runnable? = null
    var onPlayListener: BiConsumer<Calendar, Int>? = null
    private var countdownThread: Thread? = null
    private var currentTimer: TextView? = null
    private var secondLabel: TextView? = null
    private var stopButton: ImageView? = null
    private val defaultValue: Int
    private var isDefaultValue = false

    init {
        val preferences = PreferenceManager.getDefaultSharedPreferences(ctx)
        defaultValue = preferences.getString("restTime", "90")!!.toInt()

        val restTime = exercise.restTime
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

        stopButton = view.findViewById(R.id.stopButton)
        secondLabel = view.findViewById(R.id.secondLabel)
        currentTimer = view.findViewById(R.id.currentTimer)

        if (endingCountdown == null) {
            uiStopCounter()

        } else {
            countdownThread = CountdownThread(activity as Activity)
            countdownThread!!.start()
        }

        val editNotes: EditText = view.findViewById(R.id.editTimer)
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

        view.findViewById<View>(R.id.stopButton).setOnClickListener {
            countdownThread?.interrupt() ?: uiStopCounter()
            onStopListener?.run()
        }

        view.findViewById<View>(R.id.playButton).setOnClickListener {
            val seconds = FormatUtils.toInt(editNotes.text.toString())
            val endingCountdown = Calendar.getInstance()

            endingCountdown.add(Calendar.SECOND, seconds)
            stopButton!!.visibility = View.VISIBLE

            this.endingCountdown = endingCountdown
            if (countdownThread == null) {
                countdownThread = CountdownThread(activity as Activity)
                countdownThread!!.start()
            }

            onPlayListener?.accept(endingCountdown, seconds)
        }

        val builder = AlertDialog.Builder(activity)
        builder.setMessage(title)
            .setView(view)
            .setPositiveButton(R.string.button_confirm) { _,_ ->
                val seconds = FormatUtils.toInt(editNotes.text.toString())
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
        stopButton?.visibility = View.GONE
        secondLabel?.visibility = View.GONE
        currentTimer?.text = ctx.resources.getString(R.string.text_none)
            .lowercase(Locale.getDefault())
    }

    private inner class CountdownThread(activity: Activity) : SecondTickThread(Supplier {
        val seconds = DateUtils.secondsDiff(Calendar.getInstance(), endingCountdown!!)
        if (seconds > 0) {
            activity.runOnUiThread {
                currentTimer!!.text = seconds.toString()
                secondLabel!!.visibility = View.VISIBLE
            }
            true
        } else false
    }) {

        init {
            onFinishListener = Runnable {
                activity.runOnUiThread { uiStopCounter() }
                countdownThread = null
            }
        }
    }
}