package org.scp.gymlog.ui.top

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.R
import org.scp.gymlog.exceptions.InternalException
import org.scp.gymlog.exceptions.LoadException
import org.scp.gymlog.model.Bit
import org.scp.gymlog.model.Exercise
import org.scp.gymlog.room.AppDatabase
import org.scp.gymlog.room.DBThread
import org.scp.gymlog.ui.common.DBAppCompatActivity
import org.scp.gymlog.ui.top.rows.*
import org.scp.gymlog.ui.training.TrainingActivity
import org.scp.gymlog.util.Constants
import org.scp.gymlog.util.Constants.IntentReference
import org.scp.gymlog.util.Data
import java.io.IOException
import java.util.function.Consumer

open class TopActivity : DBAppCompatActivity() {

    private lateinit var exercise: Exercise
    private val listData: MutableList<ITopRow> = ArrayList()
    private var internationalSystem = false
    private lateinit var adapter: TopRecyclerViewAdapter

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

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        internationalSystem = preferences.getBoolean("internationalSystem", true)

        setHeaderInfo()

        val historyRecyclerView = findViewById<RecyclerView>(R.id.variantTopList)
        historyRecyclerView.layoutManager = LinearLayoutManager(this)

        adapter = TopRecyclerViewAdapter(listData, internationalSystem)
        historyRecyclerView.adapter = adapter

        adapter.onClickListener = Consumer { topBit -> onElementClicked(topBit) }
        adapter.onLongClickListener = Consumer { topBit -> onElementLongClicked(topBit) }
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

    private fun onElementClicked(topBit: Bit) {
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
            if (intentReference === IntentReference.TOP_RECORDS) {
                adapter.notifyItemRangeChanged(0, listData.size)

            } else if (intentReference === IntentReference.TRAINING) {
                DBThread.run(this) { db ->
                    transformBitsToRows(getBits(db, exercise.id))
                    runOnUiThread { adapter.notifyDataSetChanged() }
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
}