package org.scp.gymlog.ui.common.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import org.scp.gymlog.R
import org.scp.gymlog.R.styleable.SideButtonsLinearLayoutView
import org.scp.gymlog.R.styleable.SideButtonsLinearLayoutView_activationWidth
import org.scp.gymlog.R.styleable.SideButtonsLinearLayoutView_iconWidth
import org.scp.gymlog.R.styleable.SideButtonsLinearLayoutView_leftIcon
import org.scp.gymlog.R.styleable.SideButtonsLinearLayoutView_maxWidth
import org.scp.gymlog.R.styleable.SideButtonsLinearLayoutView_minWidth
import org.scp.gymlog.R.styleable.SideButtonsLinearLayoutView_rightIcon
import org.scp.gymlog.R.styleable.SideButtonsLinearLayoutView_swipeSensibility
import org.scp.gymlog.ui.common.animations.BackgroundColorAnimation
import org.scp.gymlog.ui.common.animations.ResizeWidthAnimation
import org.scp.gymlog.util.extensions.ComponentsExts.runOnUiThread
import java.util.function.Supplier
import kotlin.math.abs

class SideButtonsLinearLayoutView(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val minWidth: Int
    private val maxWidth: Int
    private val activationWidth: Int
    private val iconWidth: Int
    private val swipeSensibility: Int
    private val leftIcon: Drawable?
    private val rightIcon: Drawable?

    private val swipeLeft: ExpandablePanel
    private val swipeRight: ExpandablePanel
    private val linearLayout: LinearLayout

    private var onSwipeLeftListener: Supplier<Boolean>? = null
    private var onSwipeRightListener: Supplier<Boolean>? = null

    @SuppressLint("DiscouragedPrivateApi")
    private var gestureDetector = GestureDetector(context, GestureListener()).apply {
        setIsLongpressEnabled(false)
        GestureDetector::class.java.getDeclaredField("mTouchSlopSquare").also {
            it.isAccessible = true
            it.setInt(this, 0)
        }
    }

    var leftSwipeDisable = true
        set(value) {
            field = value && leftIcon != null
        }
    var rightSwipeDisable = true
        set(value) {
            field = value && rightIcon != null
        }

    init {

        val styledAttributes = context.obtainStyledAttributes(attrs, SideButtonsLinearLayoutView)

        minWidth = styledAttributes.getDimension(SideButtonsLinearLayoutView_minWidth, 0f).toInt()
        maxWidth = styledAttributes.getDimension(SideButtonsLinearLayoutView_maxWidth, 270f).toInt()
        activationWidth = styledAttributes.getDimension(SideButtonsLinearLayoutView_activationWidth, 210f).toInt()
        iconWidth = styledAttributes.getDimension(SideButtonsLinearLayoutView_iconWidth, 90f).toInt()
        swipeSensibility = styledAttributes.getDimension(SideButtonsLinearLayoutView_swipeSensibility, 30f).toInt()

        leftIcon = styledAttributes.getDrawable(SideButtonsLinearLayoutView_leftIcon)
            ?.also { leftSwipeDisable = false }
        rightIcon = styledAttributes.getDrawable(SideButtonsLinearLayoutView_rightIcon)
            ?.also { rightSwipeDisable = false }

        styledAttributes.recycle()

        linearLayout = LinearLayout(context).also {
            it.orientation = orientation
            it.isClickable = true
            it.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
            it.setOnTouchListener { view, motionEvent ->
                onMainTouch(view, motionEvent) {
                    performClick()
                }
            }
        }

        swipeRight = createSideImage(leftIcon) { onSwipeRightListener }
        swipeLeft = createSideImage(rightIcon) { onSwipeLeftListener }

        addView(swipeRight, swipeRight.layoutParams)

        LayoutParams(0, MATCH_PARENT).apply {
            weight = 1f
            addView(linearLayout, this)
        }

        addView(swipeLeft, swipeLeft.layoutParams)

        setPadding(0, 0, 0, 0)
        orientation = HORIZONTAL
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        repeat(childCount - 3)  {
            val child = getChildAt(3)
            removeView(child)
            linearLayout.addView(child, child.layoutParams)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun clickListen(view: View, onClick: (View) -> Unit) {
        view.setOnTouchListener { _, motionEvent ->
            this.onMainTouch(view, motionEvent) {
                onClick(view)
            }
        }
    }

    private fun createSideImage(
        imageDrawable: Drawable?,
        listener: () -> Supplier<Boolean>?
    ): ExpandablePanel {
        val drawable = imageDrawable
        return ExpandablePanel(context, drawable).apply {
            orTriggerListener = {
                Thread {
                    if (listener()?.get() == true) animateSuccess()
                    else animateWrong()
                }.start()
            }
        }
    }

    // TOUCH LOGIC
    private var lockSwipe = true
    private fun onMainTouch(view: View, motionEvent: MotionEvent, onClick: () -> Unit): Boolean {
        if (leftSwipeDisable && rightSwipeDisable) {
            if (motionEvent.action == ACTION_UP) {
                onClick()
            }
            return false
        }

        gestureDetector.onTouchEvent(motionEvent)

        if (!lockSwipe) {
            if (motionEvent.action == ACTION_UP) onClick()
        } else {
            if (motionEvent.action == ACTION_MOVE) view.background?.setState(intArrayOf())
        }

        if (motionEvent.action == ACTION_UP) {
            swipeLeft.reset()
            swipeRight.reset()
        }

        return false
    }

    inner class GestureListener : GestureDetector.OnGestureListener {
        private var baseX = Float.MIN_VALUE

        override fun onDown(e: MotionEvent): Boolean {
            lockSwipe = false
            baseX = e.x
            return false
        }

        override fun onShowPress(e: MotionEvent?) = Unit
        override fun onSingleTapUp(e: MotionEvent?) = lockSwipe
        override fun onLongPress(e: MotionEvent?) = Unit
        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float) =
            lockSwipe

        override fun onScroll(
            originEvent: MotionEvent?,
            currentEvent: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (originEvent == null) return false
            val totalDistX = currentEvent.x + swipeRight.width - baseX

            if (swipeLeft.open) {
                swipeLeft.setWidth(-totalDistX.toInt())
            } else if (swipeRight.open) {
                swipeRight.setWidth(totalDistX.toInt())
            } else {
                if (!leftSwipeDisable) {
                    swipeLeft.setWidth(-totalDistX.toInt())
                }
                if (!rightSwipeDisable) {
                    swipeRight.setWidth(totalDistX.toInt())
                }
            }
            if (swipeRight.maximized) {
                baseX = currentEvent.x
            } else if (swipeLeft.maximized) {
                baseX = currentEvent.x + swipeLeft.width
            }

            if (!lockSwipe && abs(totalDistX) >= swipeSensibility) {
                lockSwipe = true
            }
            return false
        }
    }

    fun setOnSwipeLeftListener(supplier: Supplier<Boolean>) {
        this.onSwipeLeftListener = supplier
    }

    fun setOnSwipeRightListener(supplier: Supplier<Boolean>) {
        this.onSwipeRightListener = supplier
    }



    // EXPANDABLE PANEL
    private inner class ExpandablePanel(
        context: Context,
        drawable: Drawable?
    ) : LinearLayout(context) {

        var orTriggerListener: (() -> Unit)? = null

        private val defaultBackground = background

        var active = false
            private set
        var open = false
            private set
        var maximized = false
            private set

        init {
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(0, MATCH_PARENT)

            AppCompatImageView(context).apply {
                setImageDrawable(drawable)
                layoutParams = LayoutParams(WRAP_CONTENT, MATCH_PARENT)
                maxWidth = iconWidth
                adjustViewBounds = true
                this@ExpandablePanel.addView(this, layoutParams)
            }
        }

        fun setWidth(width: Int): Boolean {
            val currentWidth = layoutParams.width
            val newWidth = width.coerceIn(minWidth, maxWidth)

            if (currentWidth != newWidth) {
                if (currentWidth == 0) restoreBackground()

                layoutParams.width = newWidth
                requestLayout()

                val wasActive = active
                active = newWidth >= activationWidth
                maximized = newWidth >= maxWidth
                open = newWidth > 0

                if (!wasActive && active) {
                    orTriggerListener?.invoke()
                }
                return true
            }
            return false
        }

        fun animateWrong() {
            val color = context.getColor(R.color.pulseWrongAnimation)
            startAnimation(
                BackgroundColorAnimation(context, this, color,
                    startBackground = defaultBackground, durationFadeIn = 35, durationFadeOut = 465)
            )
        }

        fun animateSuccess() {
            val color = context.getColor(R.color.pulseSuccessAnimation)
            startAnimation(
                BackgroundColorAnimation(context, this, color,
                    startBackground = defaultBackground, durationFadeIn = 0, durationFadeOut = 200)
            )
        }

        fun reset() {
            if (open) {
                startAnimation(
                    ResizeWidthAnimation(this, 0, 100)
                )

                active = false
                maximized = false
                open = false
            }
        }

        private fun restoreBackground() {
            runOnUiThread {
                background = defaultBackground
            }
        }
    }
}