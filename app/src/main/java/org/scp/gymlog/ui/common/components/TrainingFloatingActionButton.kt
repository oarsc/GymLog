package org.scp.gymlog.ui.common.components

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.scp.gymlog.R
import org.scp.gymlog.exceptions.LoadException
import org.scp.gymlog.model.Training
import org.scp.gymlog.room.entities.TrainingEntity
import org.scp.gymlog.service.NotificationService
import org.scp.gymlog.ui.common.dialogs.EditTrainingDialogFragment
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.DateUtils.NOW
import org.scp.gymlog.util.extensions.DatabaseExts.dbThread

class TrainingFloatingActionButton : FloatingActionButton {

    private lateinit var notificationService: NotificationService

    constructor(context: Context) : super(context) {
        onCreate(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        onCreate(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        onCreate(context)
    }

    private fun onCreate(context: Context) {
        notificationService = NotificationService(context)

        setOnClickListener {
            val trainingId = Data.training?.id
            if (trainingId != null) {

                context.dbThread { db ->
                    val trainingEntity = db.trainingDao().getTraining(trainingId)
                        ?: throw LoadException("Can't find trainingId $trainingId")

                    if (trainingEntity.end != null) {
                        throw LoadException("TrainingId $trainingId already ended")
                    }

                    val bitEntities = db.bitDao().getHistoryByTrainingId(trainingId)

                    if (bitEntities.isEmpty()) {
                        db.trainingDao().delete(trainingEntity)
                        Data.training = null
                        Data.superSet = null
                        updateFloatingActionButton()
                        return@dbThread
                    }

                    val dialog = EditTrainingDialogFragment(
                        R.string.form_end_training,
                        Training(trainingEntity),
                        false,
                        { result ->
                            notificationService.hideNotification()
                            context.dbThread { db ->
                                result.start = bitEntities.map { it.bit.timestamp }.minOf { it }
                                result.end = bitEntities.map { it.bit.timestamp }.maxOf { it }

                                db.trainingDao().update(result.toEntity())

                                Data.training = null
                                Data.superSet = null
                                updateFloatingActionButton()
                            }
                        }
                    )
                    val activity: FragmentActivity = getContext() as AppCompatActivity
                    dialog.show(activity.supportFragmentManager, null)
                }
            } else {
                context.dbThread { db ->
                    val maxId = db.trainingDao().getMaxTrainingId() ?: 0
                    val training = TrainingEntity().apply {
                        this.trainingId = maxId + 1
                        start = NOW
                        gymId = Data.gym?.id ?: 0
                    }
                    training.trainingId = db.trainingDao().insert(training).toInt()
                    Data.training = Training(training)
                    updateFloatingActionButton()
                }
            }
        }
    }

    fun updateFloatingActionButton() {
        backgroundTintList = if (Data.training == null) {
            setImageResource(R.drawable.ic_play_24dp)
            ColorStateList.valueOf(
                resources.getColor(R.color.green, context.theme)
            )
        } else {
            setImageResource(R.drawable.ic_stop_24dp)
            ColorStateList.valueOf(
                resources.getColor(R.color.red, context.theme)
            )
        }
    }
}