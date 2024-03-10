package org.scp.gymlog.ui.common.components

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.cardview.widget.CardView
import org.scp.gymlog.R
import org.scp.gymlog.util.FormatUtils
import org.scp.gymlog.util.FormatUtils.bigDecimal
import java.math.BigDecimal

class NumberModifierView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    companion object {
        private const val NAMESPACE = "http://schemas.android.com/apk/res-auto"
    }

    private val targetId: Int
    private var step: BigDecimal
    private var allowNegatives: Boolean
    private var autoExecuted = false
    private var autoCount = 0
    private val autoAdd: Runnable
    private val autoSub: Runnable

    init {
        targetId = attrs.getAttributeResourceValue(
            NAMESPACE, "target", -1)

        allowNegatives = attrs.getAttributeBooleanValue(
            NAMESPACE, "negatives", false)

        step = attrs.getAttributeFloatValue(
            NAMESPACE, "step", 1f)
            .toBigDecimal()

        autoAdd = autoStep()
        autoSub = autoStep(false)

        addView()
    }

    private fun autoStep(add: Boolean = true) : Runnable {
        var runnable: Runnable? = null
        runnable = Runnable {
            autoExecuted = true
            onAction(add)
            postDelayed(runnable, if (++autoCount > 30) 25 else 55.toLong())
        }
        return runnable
    }


    fun setStep(step: BigDecimal): NumberModifierView {
        this.step = step
        return this
    }

    fun allowNegatives(): NumberModifierView {
        allowNegatives = true
        return this
    }

    private fun addView() {
        val subCv = createCardView()
        val params = subCv.layoutParams as LayoutParams
        params.marginEnd = FormatUtils.toDp(resources.displayMetrics, 3)
        subCv.addView(createImageView(R.drawable.ic_substract_24dp))
        modifyEditText(subCv, false)
        addView(subCv)

        val addCv = createCardView()
        addCv.addView(createImageView(R.drawable.ic_add_24dp))
        modifyEditText(addCv, true)
        addView(addCv)
    }

    private fun createCardView(): CardView {
        val cardView = CardView(context)
        val displayMetrics = resources.displayMetrics

        val size = FormatUtils.toDp(displayMetrics, 50)
        val layoutParams = LayoutParams(size, size)
        cardView.layoutParams = layoutParams

        val color = context.resources.getColor(
            R.color.dark,
            context.theme)

        cardView.setCardBackgroundColor(color)
        cardView.radius = FormatUtils.toDpFloat(displayMetrics, 3)
        cardView.cardElevation = 0f

        return cardView
    }

    private fun createImageView(@DrawableRes drawable: Int): ImageView {
        val imageView = ImageView(context)

        val size = FormatUtils.toDp(resources.displayMetrics, 22)
        val layoutParams = FrameLayout.LayoutParams(size, size)
        layoutParams.gravity = Gravity.CENTER
        imageView.layoutParams = layoutParams

        imageView.setImageResource(drawable)

        val color = context.resources.getColor(
            R.color.white,
            context.theme)
        imageView.setColorFilter(color)

        return imageView
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun modifyEditText(cardView: CardView, addition: Boolean) {
        cardView.setOnTouchListener { _, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (addition) {
                        removeCallbacks(autoAdd)
                        autoCount = 0
                        postDelayed(autoAdd, 300)
                    } else {
                        removeCallbacks(autoSub)
                        autoCount = 0
                        postDelayed(autoSub, 300)
                    }
                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_UP -> {
                    removeCallbacks(if (addition) autoAdd else autoSub)
                    if (autoExecuted) {
                        autoExecuted = false
                    } else {
                        onAction(addition)
                    }
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    private fun onAction(addition: Boolean) {
        val editText: EditText? = rootView.findViewById(targetId)
        if (editText != null) {
            var value = editText.bigDecimal
            val step = if (addition) step else step.negate()

            value = value.add(step)
            if (!allowNegatives && value <= BigDecimal.ZERO)
                value = BigDecimal.ZERO

            editText.bigDecimal = value
            editText.setSelection(editText.text.length)
        }
    }
}