package org.oar.gymlog.ui.top

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import org.oar.gymlog.R
import org.oar.gymlog.model.Bit
import org.oar.gymlog.model.GymRelation
import org.oar.gymlog.model.Variation
import org.oar.gymlog.room.AppDatabase
import org.oar.gymlog.room.entities.BitEntity
import org.oar.gymlog.util.Constants.IntentReference
import org.oar.gymlog.util.Data
import org.oar.gymlog.util.DateUtils.timeInMillis
import org.oar.gymlog.util.extensions.ComponentsExts.mustRefreshParent
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

    override fun onDelayedCreate(savedInstanceState: Bundle?) {
        super.onDelayedCreate(savedInstanceState)
        binding.toolbar.menu.findItem(R.id.allGyms).apply {
            isEnabled = variation.gymRelation != GymRelation.NO_RELATION
            isChecked = showAllGymsData || !isEnabled
        }
    }

    override fun getBits(db: AppDatabase, exerciseId: Int): List<Bit> {
        val bits = if (showAllGymsData || variation.gymRelation == GymRelation.NO_RELATION)
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

                        else if (max.reps == bit.reps && max.note.isBlank())
                            this[day] = bit

                    } else {
                        this[day] = bit
                    }
                }
            }.values
            .map { bitEntity -> Bit(bitEntity) }
    }

    override fun order(): Comparator<in Bit> = Comparator.comparingLong { bit: Bit -> -bit.timestamp.timeInMillis }

    override fun onElementLongClicked(topBit: Bit) = onElementClicked(topBit)

    override fun onActivityResult(intentReference: IntentReference, data: Intent) {
        if (data.getBooleanExtra("refresh", false)) {
            if (intentReference === IntentReference.TRAINING) {
                mustRefreshParent()
            }
        }
        super.onActivityResult(intentReference, data)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.allGyms -> {
                item.isChecked = !item.isChecked
                showAllGymsData = item.isChecked

                val intent = Intent(this, TopSpecificActivity::class.java).apply {
                    putExtra("exerciseId", variation.exercise.id)
                    putExtra("weight", weight)
                    putExtra("variationId", variation.id)
                    putExtra("allGyms", showAllGymsData)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                startActivityForResult(intent, IntentReference.TOP_RECORDS)
            }
        }
        return false
    }
}