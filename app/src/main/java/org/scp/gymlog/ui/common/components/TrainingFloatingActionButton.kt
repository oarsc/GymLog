package org.scp.gymlog.ui.common.components

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.scp.gymlog.R
import org.scp.gymlog.exceptions.LoadException
import org.scp.gymlog.room.entities.TrainingEntity
import org.scp.gymlog.service.NotificationService
import org.scp.gymlog.ui.common.dialogs.TextDialogFragment
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.DateUtils.currentDateTime
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
            val trainingId = Data.trainingId
            if (trainingId != null) {
                val dialog = TextDialogFragment(
                        R.string.dialog_confirm_training_title,
                        R.string.dialog_confirm_training_text
                    ) { confirmed ->
                        if (confirmed) {
                            notificationService.hideNotification()
                            context.dbThread { db ->
                                val trainingDao = db.trainingDao()
                                val training = trainingDao.getTraining(trainingId)
                                    ?: throw  LoadException("Can't find trainingId $trainingId")

                                if (training.end != null) {
                                    throw LoadException("TrainingId $trainingId already ended")
                                }
                                val endDate = db.bitDao()
                                    .getMostRecentByTrainingId(trainingId)
                                    ?.timestamp

                                if (endDate != null) {
                                    val startDate = db.bitDao()
                                        .getFirstTimestampByTrainingId(trainingId)
                                    training.start = startDate!!
                                    training.end = endDate
                                    trainingDao.update(training)
                                } else {
                                    trainingDao.delete(training)
                                }
                                Data.trainingId = null
                                Data.superSet = null
                                updateFloatingActionButton()
                            }
                        }
                    }
                val activity: FragmentActivity = getContext() as AppCompatActivity
                dialog.show(activity.supportFragmentManager, null)
            } else {
                context.dbThread { db ->
                    val maxId = db.trainingDao().getMaxTrainingId() ?: 0
                    val training = TrainingEntity()
                    training.trainingId = maxId + 1
                    training.start = currentDateTime()
                    Data.trainingId = db.trainingDao().insert(training).toInt()
                    updateFloatingActionButton()
                }
            }
        }
    }

    fun updateFloatingActionButton() {
        backgroundTintList = if (Data.trainingId == null) {
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