package org.oar.gymlog.ui.common.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.annotation.StringRes
import org.oar.gymlog.R
import org.oar.gymlog.databinding.DialogEditTimerBinding
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
    @StringRes title: Int,
    val variation: Variation,
    confirm: Consumer<Int>
) : CustomDialogFragment<Int>(title, confirm, Runnable {}) {

    override var initialValue: Int = 60

    private var countdownThread: CountdownThread? = null
    private lateinit var binding: DialogEditTimerBinding

    private var isDefaultValue = false
    private var defaultValue = 60

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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogEditTimerBinding.inflate(layoutInflater)

        defaultValue = requireContext().loadString(PreferencesDefinition.DEFAULT_REST_TIME).toInt()
        val restTime = variation.restTime
        if (restTime < 0) {
            isDefaultValue = true
            initialValue = defaultValue
        } else {
            isDefaultValue = false
            initialValue = restTime
        }

        if (NotificationService.lastEndTime > 0) {
            countdownThread = CountdownThread(activity as Activity)
                .also(Thread::start)
        } else {
            uiStopCounter()
            showCurrentCountdownButtons(false)
        }

        binding.editTimer.apply {
            setText(initialValue.toString())
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {}
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (isDefaultValue) {
                        isDefaultValue = false
                        setInputAlpha(1f)
                    }
                }
            })
        }

        if (isDefaultValue) setInputAlpha(0.4f)

        binding.stopButton.setOnClickListener {
            countdownThread?.interrupt()
            onStopListener?.run()
            uiStopCounter()
            showCurrentCountdownButtons(false)
        }

        binding.playButton.setOnClickListener {
            val seconds = binding.editTimer.integer
            val endingCountdown = System.currentTimeMillis() + seconds * 1000L
            showCurrentCountdownButtons()

            onPlayListener?.accept(endingCountdown, seconds)

            if (countdownThread == null) {
                countdownThread = CountdownThread(activity as Activity)
                    .also(Thread::start)
            }
        }

        binding.minusTenButton.setOnClickListener {
            onAddTimeListener?.accept(-10)
            countdownThread?.onTick()
        }

        binding.plusTenButton.setOnClickListener {
            onAddTimeListener?.accept(10)
            countdownThread?.onTick()
        }

        return AlertDialog.Builder(activity)
            .setTitle(title)
            .setView(binding.root)
            .setPositiveButton(R.string.button_confirm) { _,_ ->
                val seconds = binding.editTimer.integer
                if (seconds == defaultValue && isDefaultValue) {
                    confirm.accept(-1)
                } else {
                    confirm.accept(seconds)
                }
            }
            .setNeutralButton(R.string.text_default) { _,_ -> confirm.accept(-1) }
            .setNegativeButton(R.string.button_cancel) { _,_ -> cancel.run() }
            .create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        countdownThread?.interrupt()
    }

    private fun setInputAlpha(alpha: Float) {
        listOf(binding.editTimer, binding.inputSecondsLabel)
            .forEach { v -> v.alpha = alpha }
    }

    private fun uiStopCounter() {
        context?.apply {
            binding.secondLabel.visibility = View.GONE
            binding.currentTimer.text = resources.getString(R.string.text_none).lowercase(Locale.getDefault())
        }
    }

    private fun showCurrentCountdownButtons(show: Boolean = true) {
        listOf(binding.stopButton, binding.plusTenButton, binding.minusTenButton)
            .forEach { it.visibility = if (show) View.VISIBLE else View.GONE }
    }

    private inner class CountdownThread(val activity: Activity) : SecondTickThread() {
        override fun onTick(): Boolean {
            if (NotificationService.lastEndTime <= 0)
                return false

            val seconds = NotificationService.lastEndTime.diffSeconds()

            activity.runOnUiThread {
                binding.currentTimer.integer = if (NotificationService.lastEndTime.isSystemTimePast) -seconds else seconds
                binding.secondLabel.visibility = View.VISIBLE
            }
            return true
        }

        override fun onFinish() {
            activity.runOnUiThread { uiStopCounter() }
            countdownThread = null
        }
    }
}