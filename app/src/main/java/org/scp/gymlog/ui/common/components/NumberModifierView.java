package org.scp.gymlog.ui.common.components;

import static org.scp.gymlog.util.FormatUtils.toBigDecimal;
import static org.scp.gymlog.util.FormatUtils.toDp;
import static org.scp.gymlog.util.FormatUtils.toDpFloat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import org.scp.gymlog.R;
import org.scp.gymlog.util.FormatUtils;

import java.math.BigDecimal;

public class NumberModifierView extends LinearLayout {
    private int targetId = -1;
    private BigDecimal step = BigDecimal.ONE;
    private boolean allowNegatives = false;

    private boolean autoExecuted = false;
    private int autoCount = 0;
    private Runnable autoAdd;
    private Runnable autoSub;

    public NumberModifierView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if (attrs != null) {
            targetId = attrs.getAttributeResourceValue(
                    "http://schemas.android.com/apk/res-auto",
                    "target",
                    -1);

            allowNegatives = attrs.getAttributeBooleanValue(
                    "http://schemas.android.com/apk/res-auto",
                    "negatives",
                    false);

            step = BigDecimal.valueOf(attrs.getAttributeFloatValue(
                    "http://schemas.android.com/apk/res-auto",
                    "step",
                    1f));
        }

        autoAdd = () -> {
            autoExecuted = true;
            onAction(true);
            postDelayed(autoAdd, ++autoCount > 30? 25 : 55);
        };

        autoSub = () -> {
            autoExecuted = true;
            onAction(false);
            postDelayed(autoSub, ++autoCount > 30? 25 : 55);
        };

        addView();
    }

    public NumberModifierView setStep(BigDecimal step) {
        this.step = step;
        return this;
    }

    public NumberModifierView allowNegatives() {
        this.allowNegatives = true;
        return this;
    }

    private void addView() {
        CardView subCv = createCardView();
        LayoutParams params = (LayoutParams) subCv.getLayoutParams();
        params.setMarginEnd(toDp(getResources().getDisplayMetrics(), 3));
        subCv.addView(createImageView(R.drawable.ic_substract_24dp));
        modifyEditText(subCv, false);
        addView(subCv);

        CardView addCv = createCardView();
        addCv.addView(createImageView(R.drawable.ic_add_24dp));
        modifyEditText(addCv, true);
        addView(addCv);
    }

    private CardView createCardView() {
        CardView cardView = new CardView(getContext());
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        int size = toDp(displayMetrics, 50);
        LayoutParams layoutParams = new LayoutParams(size, size);
        cardView.setLayoutParams(layoutParams);

        int color = getContext().getResources().getColor(
                R.color.dark,
                getContext().getTheme());

        cardView.setCardBackgroundColor(color);
        cardView.setRadius(toDpFloat(displayMetrics, 3));
        cardView.setCardElevation(0);

        return cardView;
    }

    private ImageView createImageView(@DrawableRes int drawable) {
        ImageView imageView = new ImageView(getContext());

        int size = toDp(getResources().getDisplayMetrics(), 22);
        CardView.LayoutParams layoutParams = new CardView.LayoutParams(size, size);
        layoutParams.gravity = Gravity.CENTER;
        imageView.setLayoutParams(layoutParams);

        imageView.setImageResource(drawable);

        int color = getContext().getResources().getColor(
                R.color.white,
                getContext().getTheme());
        imageView.setColorFilter(color);

        return imageView;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void modifyEditText(CardView cardView, boolean addition) {
        cardView.setOnTouchListener((view, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (addition) {
                        removeCallbacks(autoAdd);
                        autoCount = 0;
                        postDelayed(autoAdd, 300);
                    } else {
                        removeCallbacks(autoSub);
                        autoCount = 0;
                        postDelayed(autoSub, 300);
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    removeCallbacks(addition? autoAdd : autoSub);
                    if (autoExecuted) {
                        autoExecuted = false;
                    } else {
                        onAction(addition);
                    }
                    return true;
            }
            return false;
        });
    }


    private void onAction(boolean addition) {
        EditText editText = getRootView().findViewById(targetId);
        if (editText != null) {
            BigDecimal value = toBigDecimal(editText.getText().toString());
            BigDecimal step = addition? this.step : this.step.negate();

            value = value.add(step);
            if (!allowNegatives && value.compareTo(BigDecimal.ZERO) <= 0)
                value = BigDecimal.ZERO;

            editText.setText(FormatUtils.toString(value));
            editText.setSelection(editText.getText().length());
        }
    }
}
