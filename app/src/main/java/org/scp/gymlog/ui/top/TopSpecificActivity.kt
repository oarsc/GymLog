package org.scp.gymlog.ui.top

import android.os.Bundle
import org.scp.gymlog.model.Bit
import org.scp.gymlog.room.AppDatabase
import org.scp.gymlog.room.entities.BitEntity
import org.scp.gymlog.util.DateUtils.timeInMillis
import java.time.LocalDate

class TopSpecificActivity : TopActivity() {

    private var weight = 0
    private var variationId = 0

    override fun onLoad(savedInstanceState: Bundle?, db: AppDatabase): Int {
        weight = intent.extras!!.getInt("weight")
        variationId = intent.extras?.getInt("variationId") ?: 0
        return super.onLoad(savedInstanceState, db)
    }

    override fun getBits(db: AppDatabase, exerciseId: Int): MutableList<Bit> {
        val bits = if (variationId == 0)
            db.bitDao().findAllByExerciseAndWeight(exerciseId, weight)
        else
            db.bitDao().findAllByExerciseAndWeight(exerciseId, variationId, weight)

        return mutableMapOf<LocalDate, BitEntity>()
            .apply {
                bits.forEach { bit ->
                    val day = bit.timestamp.toLocalDate()

                    if (containsKey(day)) {
                        val max = this[day]!!

                        if (max.reps < bit.reps)
                            this[day] = bit

                        else if (max.reps == bit.reps && max.note.isBlank())
                            this[day] = bit

                    } else {
                        this[day] = bit
                    }
                }
            }.values
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