package org.scp.gymlog.ui.top

import android.os.Bundle
import org.scp.gymlog.model.Bit
import org.scp.gymlog.room.AppDatabase
import org.scp.gymlog.util.DateUtils.timeInMillis

class TopSpecificActivity : TopActivity() {

    private var weight = 0
    private var variationId = 0

    override fun onLoad(savedInstanceState: Bundle?, db: AppDatabase): Int {
        weight = intent.extras!!.getInt("weight")
        variationId = intent.extras?.getInt("variationId") ?: 0
        return super.onLoad(savedInstanceState, db)
    }

    override fun getBits(db: AppDatabase, exerciseId: Int): MutableList<Bit> {
        return if (variationId == 0)
                db.bitDao().findAllByExerciseAndWeight(exerciseId, weight)
                    .map { bitEntity -> Bit(bitEntity) }
                    .toMutableList()
            else
                db.bitDao().findAllByExerciseAndWeight(exerciseId, variationId, weight)
                    .map { bitEntity -> Bit(bitEntity) }
                    .toMutableList()
    }

    override fun order(): Comparator<in Bit> {
        return Comparator.comparingLong { bit: Bit -> -bit.timestamp.timeInMillis }
    }

    override fun onElementLongClicked(topBit: Bit) {
        // do nothing
    }
}