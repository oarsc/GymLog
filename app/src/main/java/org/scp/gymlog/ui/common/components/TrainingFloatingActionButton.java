package org.scp.gymlog.ui.common.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.scp.gymlog.R;
import org.scp.gymlog.exceptions.LoadException;
import org.scp.gymlog.room.DBThread;
import org.scp.gymlog.room.daos.TrainingDao;
import org.scp.gymlog.room.entities.BitEntity;
import org.scp.gymlog.room.entities.TrainingEntity;
import org.scp.gymlog.ui.common.dialogs.TextDialogFragment;
import org.scp.gymlog.util.Data;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

public class TrainingFloatingActionButton extends FloatingActionButton {

    public TrainingFloatingActionButton(@NonNull Context context) {
        super(context);
        onCreate();
    }

    public TrainingFloatingActionButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        onCreate();
    }

    public TrainingFloatingActionButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onCreate();
    }

    private void onCreate() {
        setOnClickListener(l -> {
            int trainingId = Data.getInstance().getTrainingId();

            if (trainingId >= 0) {
                TextDialogFragment dialog = new TextDialogFragment(R.string.dialog_confirm_training_title,
                        R.string.dialog_confirm_training_text,
                        confirmed -> {
                            if (confirmed) {
                                DBThread.run(getContext(), db -> {
                                    TrainingDao dao = db.trainingDao();
                                    TrainingEntity training = dao.getTraining(trainingId)
                                            .orElseThrow(() -> new LoadException("Can't find trainingId " + trainingId));
                                    if (training.end != null) {
                                        throw new LoadException("TrainingId " + trainingId + " already ended");
                                    }

                                    Optional<Calendar> endDate = db.bitDao().getMostRecentTimestampByTrainingId(trainingId);
                                    if (endDate.isPresent()) {
                                        Optional<Calendar> startDate = db.bitDao().getFirstTimestampByTrainingId(trainingId);
                                        training.start = startDate.get();
                                        training.end = endDate.get();
                                        dao.update(training);
                                    } else {
                                        dao.delete(training);
                                    }
                                    Data.getInstance().setTrainingId(-1);
                                    updateFloatingActionButton();
                                });
                            }
                        });

                FragmentActivity activity = (AppCompatActivity) getContext();
                dialog.show(activity.getSupportFragmentManager(), null);

            } else {
                DBThread.run(getContext(), db -> {
                    TrainingEntity training = new TrainingEntity();
                    training.start = Calendar.getInstance();
                    training.trainingId = (int) db.trainingDao().insert(training);
                    System.out.println("NEW TRAINING ID: "+training.trainingId);
                    Data.getInstance().setTrainingId(training.trainingId);
                    updateFloatingActionButton();
                });
            }
        });
    }

    public void updateFloatingActionButton() {
        Context context = getContext();
        if (Data.getInstance().getTrainingId() >= 0) {
            setImageResource(R.drawable.ic_stop_24dp);
            setBackgroundTintList(ColorStateList.valueOf(
                    getResources().getColor(R.color.red, context.getTheme())
            ));
        } else {
            setImageResource(R.drawable.ic_play_24dp);
            setBackgroundTintList(ColorStateList.valueOf(
                    getResources().getColor(R.color.green, context.getTheme())
            ));
        }
    }
}
