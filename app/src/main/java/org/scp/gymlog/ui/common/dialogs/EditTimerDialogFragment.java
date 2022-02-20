package org.scp.gymlog.ui.common.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.preference.PreferenceManager;

import org.scp.gymlog.R;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.util.DateUtils;
import org.scp.gymlog.util.FormatUtils;
import org.scp.gymlog.util.SecondTickThread;

import java.util.Calendar;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class EditTimerDialogFragment extends CustomDialogFragment<Integer> {

    private final Exercise exercise;
    private Calendar endingCountdown;

    private Runnable onStopListener;
    private BiConsumer<Calendar, Integer> onPlayListener;

    private Thread countdownThread;
    private TextView currentTimer;
    private TextView secondLabel;
    private ImageView stopButton;

    private final int defaultValue;
    private boolean isDefaultValue;

    public EditTimerDialogFragment(@StringRes int title, Context context, Exercise exercise, Calendar endingCountdown,
                                   Consumer<Integer> confirm) {
        super(title, confirm, () -> {});
        this.endingCountdown = endingCountdown;
        this.exercise = exercise;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        defaultValue = Integer.parseInt(preferences.getString("restTime", "90"));

        int restTime = exercise.getRestTime();
        if (restTime < 0) {
            isDefaultValue = true;
            initialValue = defaultValue;
        } else {
            isDefaultValue = false;
            initialValue = restTime;
        }
    }

    public void setOnStopListener(Runnable onStopListener) {
        this.onStopListener = onStopListener;
    }

    public void setOnPlayListener(BiConsumer<Calendar, Integer> onPlayListener) {
        this.onPlayListener = onPlayListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_timer, null);

        stopButton = view.findViewById(R.id.stopButton);
        secondLabel = view.findViewById(R.id.secondLabel);
        currentTimer = view.findViewById(R.id.currentTimer);

        if (endingCountdown == null) {
            uiStopCounter();

        } else {
            countdownThread = new CountdownThread(getActivity());
            countdownThread.start();
        }

        EditText editNotes = view.findViewById(R.id.editTimer);
        editNotes.setText(String.valueOf(initialValue));
        editNotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isDefaultValue) {
                    isDefaultValue = false;
                    setInputAlpha(view, 1f);
                }
            }
        });
        if (isDefaultValue) setInputAlpha(view, 0.4f);

        view.findViewById(R.id.stopButton).setOnClickListener(v -> {
            if (countdownThread != null) {
                countdownThread.interrupt();
            } else {
                uiStopCounter();
            }
            if (onStopListener != null) {
                onStopListener.run();
            }
        });

        view.findViewById(R.id.playButton).setOnClickListener(v -> {
            int seconds = FormatUtils.toInt(editNotes.getText().toString());
            Calendar endingCountdown = Calendar.getInstance();
            endingCountdown.add(Calendar.SECOND, seconds);
            stopButton.setVisibility(View.VISIBLE);
            this.endingCountdown = endingCountdown;
            if (countdownThread == null) {
                countdownThread = new CountdownThread(getActivity());
                countdownThread.start();
            }
            if (onPlayListener != null) {
                onPlayListener.accept(endingCountdown, seconds);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(title)
                .setView(view)
                .setPositiveButton(R.string.button_confirm, (dialog, id) -> {
                    int seconds = FormatUtils.toInt(editNotes.getText().toString());
                    if (seconds == defaultValue && isDefaultValue) {
                        confirm.accept(-1);
                    } else {
                        confirm.accept(seconds);
                    }
                })
                .setNeutralButton(R.string.text_default, (dialog, id) -> confirm.accept(-1))
                .setNegativeButton(R.string.button_cancel, (dialog, id) -> cancel.run());

        return builder.create();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (countdownThread != null) {
            countdownThread.interrupt();
        }
    }

    private class CountdownThread extends SecondTickThread {
        public CountdownThread(@NonNull Activity activity) {
            super(() -> {
                int seconds = DateUtils.secondsDiff(Calendar.getInstance(), endingCountdown);
                if (seconds > 0) {
                    activity.runOnUiThread(() -> {
                        currentTimer.setText(String.valueOf(seconds));
                        secondLabel.setVisibility(View.VISIBLE);
                    });
                    return true;
                }
                return false;
            });

            onFinishListener = () -> {
                activity.runOnUiThread(EditTimerDialogFragment.this::uiStopCounter);
                countdownThread = null;
            };
        }
    }

    private void setInputAlpha(View view, float alpha) {
        Stream.of(R.id.editTimer, R.id.inputSecondsLabel)
                .<View>map(view::findViewById)
                .forEach(v -> v.setAlpha(alpha));
    }

    private void uiStopCounter() {
        stopButton.setVisibility(View.GONE);
        secondLabel.setVisibility(View.GONE);
        Context context = getContext();
        if (context != null) {
            currentTimer.setText(context.getResources().getString(R.string.text_none).toLowerCase());
        }
    }
}
