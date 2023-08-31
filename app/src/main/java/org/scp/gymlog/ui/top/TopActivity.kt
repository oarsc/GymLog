package org.scp.gymlog.ui.top

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.viewbinding.ViewBinding
import org.scp.gymlog.R
import org.scp.gymlog.databinding.ListitemTopBitBinding
import org.scp.gymlog.databinding.ListitemTopHeadersBinding
import org.scp.gymlog.databinding.ListitemTopSpaceBinding
import org.scp.gymlog.databinding.ListitemTopVariationBinding
import org.scp.gymlog.exceptions.InternalException
import org.scp.gymlog.exceptions.LoadException
import org.scp.gymlog.model.Bit
import org.scp.gymlog.model.Exercise
import org.scp.gymlog.room.AppDatabase
import org.scp.gymlog.ui.common.DBAppCompatActivity
import org.scp.gymlog.ui.common.components.listView.MultipleListHandler
import org.scp.gymlog.ui.common.components.listView.MultipleListView
import org.scp.gymlog.ui.common.components.listView.SimpleListView
import org.scp.gymlog.ui.preferences.PreferencesDefinition
import org.scp.gymlog.ui.top.rows.*
import org.scp.gymlog.ui.training.TrainingActivity
import org.scp.gymlog.util.Constants
import org.scp.gymlog.util.Constants.IntentReference
import org.scp.gymlog.util.Constants.TODAY
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.DateUtils
import org.scp.gymlog.util.DateUtils.getDateString
import org.scp.gymlog.util.DateUtils.getLetterFrom
import org.scp.gymlog.util.FormatUtils.bigDecimal
import org.scp.gymlog.util.FormatUtils.integer
import org.scp.gymlog.util.WeightUtils.calculate
import org.scp.gymlog.util.extensions.DatabaseExts.dbThread
import org.scp.gymlog.util.extensions.PreferencesExts.loadBoolean
import java.io.IOException

open class TopActivity : DBAppCompatActivity() {

    private lateinit var exercise: Exercise
    private val listData: MutableList<ITopRow> = ArrayList()
    private var internationalSystem = false
    private lateinit var topListView: MultipleListView<ITopRow>

    override fun onLoad(savedInstanceState: Bundle?, db: AppDatabase): Int {
        val exerciseId = intent.extras!!.getInt("exerciseId")
        exercise = Data.exercises
            .filter { exercise -> exercise.id == exerciseId }
            .getOrElse(0) { throw InternalException("Exercise id not found") }

        transformBitsToRows(getBits(db, exerciseId))
        return CONTINUE
    }

    override fun onDelayedCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_tops)
        setTitle(R.string.title_top_records)

        internationalSystem = loadBoolean(PreferencesDefinition.UNIT_INTERNATIONAL_SYSTEM)

        setHeaderInfo()

        topListView = findViewById(R.id.variantTopList)
        topListView.init(listData, TopListHandler())
    }

    protected open fun getBits(db: AppDatabase, exerciseId: Int): List<Bit> {
        return db.bitDao().findTops(Data.currentGym, exerciseId)
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
                    topListView.setListData(listData)
                    runOnUiThread { topListView.notifyDataSetChanged() }
                }
            }
        }
    }

    private fun setHeaderInfo() {
        val image = findViewById<ImageView>(R.id.image)
        val time = findViewById<TextView>(R.id.time)
        val title = findViewById<TextView>(R.id.exerciseName)

        findViewById<TextView>(R.id.variationName).visibility = View.GONE

        title.text = exercise.name
        time.visibility = View.GONE
        val fileName = "previews/" + exercise.image + ".png"
        try {
            val ims = assets.open(fileName)
            val d = Drawable.createFromStream(ims, null)
            image.setImageDrawable(d)
        } catch (e: IOException) {
            throw LoadException("Could not read \"$fileName\"", e)
        }
    }

    inner class TopListHandler : MultipleListHandler<ITopRow> {
        override val useListState = false

        override fun generateListItemInflater(item: ITopRow): (LayoutInflater, ViewGroup?, Boolean) -> ViewBinding {
            return when(item.type) {
                ITopRow.Type.BIT -> ListitemTopBitBinding::inflate
                ITopRow.Type.VARIATION -> ListitemTopVariationBinding::inflate
                ITopRow.Type.HEADER -> ListitemTopHeadersBinding::inflate
                else -> ListitemTopSpaceBinding::inflate
            }
        }

        override fun buildListView(
            binding: ViewBinding,
            item: ITopRow,
            index: Int,
            state: SimpleListView.ListElementState?
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

                    @SuppressLint("SetTextI18n")
                    binding.time.text = bit.timestamp.getDateString() + " (" +
                        TODAY.getLetterFrom(bit.timestamp) + ")"

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