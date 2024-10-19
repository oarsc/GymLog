package org.scp.gymlog.ui.top

import android.content.Intent
import android.os.Bundle
import org.scp.gymlog.model.Bit
import org.scp.gymlog.model.GymRelation
import org.scp.gymlog.model.Variation
import org.scp.gymlog.room.AppDatabase
import org.scp.gymlog.room.entities.BitEntity
import org.scp.gymlog.util.Constants
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.DateUtils.timeInMillis
import java.time.LocalDate

class TopSpecificActivity : TopActivity() {

    private var weight = 0
    private lateinit var variation: Variation

    override fun onLoad(savedInstanceState: Bundle?, db: AppDatabase): Int {
        weight = intent.extras!!.getInt("weight")
        val variationId = intent.extras!!.getInt("variationId")
        variation = Data.getVariation(variationId)
        return super.onLoad(savedInstanceState, db)
    }

    override fun getBits(db: AppDatabase, exerciseId: Int): MutableList<Bit> {
        val bits = if (variation.gymRelation == GymRelation.NO_RELATION)
                db.bitDao().findAllByExerciseAndWeight(variation.id, weight)
            else
                db.bitDao().findAllByExerciseAndWeight(Data.gym?.id ?: 0, variation.id, weight)

        return mutableMapOf<LocalDate, BitEntity>()
            .apply {
                bits.forEach { bit ->
                    val day = bit.timestamp.toLocalDate()

                    if (containsKey(day)) {
                        val max = this[day]!!

                        if (max.reps < bit.reps)
                            this[day] = bit

                        else if (max.reps == bit.reps)
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

    override fun onElementLongClicked(topBit: Bit) = onElementClicked(topBit)

    override fun onActivityResult(intentReference: Constants.IntentReference, data: Intent) {
        if (data.getBooleanExtra("refresh", false)) {
            if (intentReference === Constants.IntentReference.TRAINING) {
                Intent().apply {
                    putExtra("refresh", true)
                    setResult(RESULT_OK, this)
                }
            }
        }
        super.onActivityResult(intentReference, data)
    }
}