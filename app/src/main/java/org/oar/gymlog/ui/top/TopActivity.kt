package org.oar.gymlog.ui.top

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import org.oar.gymlog.databinding.ActivityTopsBinding
import org.oar.gymlog.databinding.ListitemTopBitBinding
import org.oar.gymlog.databinding.ListitemTopHeadersBinding
import org.oar.gymlog.databinding.ListitemTopSpaceBinding
import org.oar.gymlog.databinding.ListitemTopVariationBinding
import org.oar.gymlog.exceptions.InternalException
import org.oar.gymlog.model.Bit
import org.oar.gymlog.model.Exercise
import org.oar.gymlog.room.AppDatabase
import org.oar.gymlog.ui.common.DatabaseAppCompatActivity
import org.oar.gymlog.ui.common.components.listView.CommonListView
import org.oar.gymlog.ui.common.components.listView.MultipleListHandler
import org.oar.gymlog.ui.main.preferences.PreferencesDefinition
import org.oar.gymlog.ui.top.rows.ITopRow
import org.oar.gymlog.ui.top.rows.TopBitRow
import org.oar.gymlog.ui.top.rows.TopEmptySpaceRow
import org.oar.gymlog.ui.top.rows.TopHeaderRow
import org.oar.gymlog.ui.top.rows.TopVariationRow
import org.oar.gymlog.ui.training.TrainingActivity
import org.oar.gymlog.util.Constants
import org.oar.gymlog.util.Constants.IntentReference
import org.oar.gymlog.util.Constants.TODAY
import org.oar.gymlog.util.Data
import org.oar.gymlog.util.DateUtils.getDateString
import org.oar.gymlog.util.DateUtils.getLetterFrom
import org.oar.gymlog.util.FormatUtils.bigDecimal
import org.oar.gymlog.util.FormatUtils.integer
import org.oar.gymlog.util.WeightUtils.calculate
import org.oar.gymlog.util.extensions.DatabaseExts.dbThread
import org.oar.gymlog.util.extensions.PreferencesExts.loadBoolean

open class TopActivity : DatabaseAppCompatActivity<ActivityTopsBinding>(ActivityTopsBinding::inflate) {

    private lateinit var exercise: Exercise
    private val listData: MutableList<ITopRow> = ArrayList()
    private var internationalSystem = false

    override fun onLoad(savedInstanceState: Bundle?, db: AppDatabase): Int {
        val exerciseId = intent.extras!!.getInt("exerciseId")
        exercise = Data.exercises
            .filter { exercise -> exercise.id == exerciseId }
            .getOrElse(0) { throw InternalException("Exercise id not found") }

        transformBitsToRows(getBits(db, exerciseId))
        return CONTINUE
    }

    override fun onDelayedCreate(savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        internationalSystem = loadBoolean(PreferencesDefinition.UNIT_INTERNATIONAL_SYSTEM)
        setHeaderInfo()
        binding.variantTopList.init(listData, TopListHandler())
    }

    protected open fun getBits(db: AppDatabase, exerciseId: Int): List<Bit> {
        return db.bitDao().findTops(Data.gym?.id ?: 0, exerciseId)
            .map { bitEntity -> Bit(bitEntity) }
    }

    protected open fun order(): Comparator<in Bit> {
        return Comparator.comparing { bit -> bit.weight.getValue(internationalSystem).negate() }
    }

    private fun transformBitsToRows(bits: List<Bit>) {
        val variations: MutableSet<Int> = HashSet()
        listData.clear()
        bits.sortedWith (order())
            .sortedWith(compareBy { it.variation.id })
            .sortedWith(compareBy { !it.variation.default })
            .forEach { bit ->
                val variationId = bit.variation.id
                if (!variations.contains(variationId)) {
                    variations.add(variationId)
                    if (!bit.variation.default) {
                        val variation = Data.getVariation(exercise, variationId)
                        listData.add(TopVariationRow(variation))
                    }
                    listData.add(TopHeaderRow())
                }
                listData.add(TopBitRow(bit))
            }
        listData.add(TopEmptySpaceRow())
    }

    protected fun onElementClicked(topBit: Bit) {
        val intent = Intent(this, TrainingActivity::class.java)
        intent.putExtra("trainingId", topBit.trainingId)
        intent.putExtra("focusBit", topBit.id)
        startActivityForResult(intent, IntentReference.TRAINING)
    }

    protected open fun onElementLongClicked(topBit: Bit) {
        val intent = Intent(this, TopSpecificActivity::class.java)
        intent.putExtra("exerciseId", topBit.variation.exercise.id)
        intent.putExtra("weight", topBit.weight.value.multiply(Constants.ONE_HUNDRED).toInt())
        intent.putExtra("variationId", topBit.variation.id)
        startActivityForResult(intent, IntentReference.TOP_RECORDS)
    }

    override fun onActivityResult(intentReference: IntentReference, data: Intent) {
        if (data.getBooleanExtra("refresh", false)) {
            if (intentReference === IntentReference.TOP_RECORDS ||
                intentReference === IntentReference.TRAINING) {
                dbThread { db ->
                    transformBitsToRows(getBits(db, exercise.id))
                    binding.variantTopList.setListData(listData)
                    runOnUiThread { binding.variantTopList.notifyDataSetChanged() }
                }
            }
        }
    }

    private fun setHeaderInfo() {
        binding.fragmentExercise.apply {
            variationName.visibility = View.GONE
            exerciseName.text = exercise.name
            image.setImage(exercise.image, exercise.primaryMuscles[0].color)
        }
    }

    inner class TopListHandler : MultipleListHandler<ITopRow> {
        override val useListState = false
        override val itemInflaters = listOf<(LayoutInflater, ViewGroup?, Boolean) -> ViewBinding>(
            ListitemTopBitBinding::inflate,
            ListitemTopVariationBinding::inflate,
            ListitemTopHeadersBinding::inflate,
            ListitemTopSpaceBinding::inflate
        )

        override fun findItemInflaterIndex(item: ITopRow) = when(item.type) {
            ITopRow.Type.BIT -> 0
            ITopRow.Type.VARIATION -> 1
            ITopRow.Type.HEADER -> 2
            else -> 3
        }

        override fun buildListView(
            binding: ViewBinding,
            item: ITopRow,
            index: Int,
            state: CommonListView.ListElementState?
        ) {
            when (item.type) {
                ITopRow.Type.VARIATION -> {
                    binding as ListitemTopVariationBinding
                    item as TopVariationRow
                    binding.variationName.text = item.variation.name
                }
                ITopRow.Type.BIT -> {
                    binding as ListitemTopBitBinding

                    item as TopBitRow
                    val bit = item.bit

                    val weight = bit.weight.calculate(
                        bit.variation.weightSpec,
                        bit.variation.bar)

                    binding.weight.bigDecimal = weight.getValue(internationalSystem)
                    binding.reps.integer = bit.reps
                    binding.time.text = bit.timestamp.getDateString()
                    binding.days.text = TODAY.getLetterFrom(bit.timestamp)
                    binding.note.text = bit.note

                    binding.root.setOnClickListener {
                        onElementClicked(item.bit)
                    }

                    binding.root.setOnLongClickListener {
                        onElementLongClicked(item.bit)
                        true
                    }
                }
                else -> {}
            }

        }
    }
}