package org.scp.gymlog.ui.common.components

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.scp.gymlog.R
import org.scp.gymlog.exceptions.LoadException
import org.scp.gymlog.room.AppDatabase
import org.scp.gymlog.room.DBThread
import org.scp.gymlog.room.entities.TrainingEntity
import org.scp.gymlog.service.NotificationService
import org.scp.gymlog.ui.common.dialogs.TextDialogFragment
import org.scp.gymlog.util.Data
import java.util.*

class TrainingFloatingActionButton : FloatingActionButton {
    private var notificationService: NotificationService? = null

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
            if (trainingId >= 0) {
                val dialog = TextDialogFragment(
                        R.string.dialog_confirm_training_title,
                        R.string.dialog_confirm_training_text
                    ) { confirmed ->
                        if (confirmed) {
                            notificationService!!.hideNotification()
                            DBThread.run(context) { db: AppDatabase ->
                                val trainingDao = db.trainingDao()
                                val training = trainingDao.getTraining(trainingId)
                                    .orElseThrow { LoadException("Can't find trainingId $trainingId") }

                                if (training.end != null) {
                                    throw LoadException("TrainingId $trainingId already ended")
                                }
                                val endDate = db.bitDao()
                                    .getMostRecentTimestampByTrainingId(trainingId)

                                if (endDate.isPresent) {
                                    val startDate =db.bitDao()
                                        .getFirstTimestampByTrainingId(trainingId)
                                    training.start = startDate.get()
                                    training.end = endDate.get()
                                    trainingDao.update(training)
                                } else {
                                    trainingDao.delete(training)
                                }
                                Data.trainingId = -1
                                updateFloatingActionButton()
                            }
                        }
                    }
                val activity: FragmentActivity = getContext() as AppCompatActivity
                dialog.show(activity.supportFragmentManager, null)
            } else {
                DBThread.run(context) { db: AppDatabase ->
                    val training = TrainingEntity()
                    training.start = Calendar.getInstance()
                    training.trainingId = db.trainingDao().insert(training).toInt()
                    Data.trainingId = training.trainingId
                    updateFloatingActionButton()
                }
            }
        }
    }

    fun updateFloatingActionButton() {
        val context = context
        backgroundTintList = if (Data.trainingId >= 0) {
            setImageResource(R.drawable.ic_stop_24dp)
            ColorStateList.valueOf(
                resources.getColor(R.color.red, context.theme)
            )
        } else {
            setImageResource(R.drawable.ic_play_24dp)
            ColorStateList.valueOf(
                resources.getColor(R.color.green, context.theme)
            )
        }
    }
}