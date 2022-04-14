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
import org.scp.gymlog.ui.common.dialogs.EditExercisesLastsDialogFragment
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

        val historyRecyclerView: RecyclerView = findViewById(R.id.variantTopList)
        historyRecyclerView.layoutManager = LinearLayoutManager(this)

        adapter = TopRecyclerViewAdapter(listData, exercise, internationalSystem)
        historyRecyclerView.adapter = adapter

        adapter.onClickListener = Consumer { topBit -> onElementClicked(topBit) }
        adapter.onLongClickListener = Consumer { topBit -> onElementLongClicked(topBit) }
    }

    protected open fun getBits(db: AppDatabase, exerciseId: Int): MutableList<Bit> {
        return db.bitDao().findTops(exerciseId)
            .map { bitEntity -> Bit(bitEntity) }
            .toMutableList()
    }

    protected open fun order(): Comparator<in Bit> {
        return Comparator.comparing { bit -> bit.weight.value.negate() }
    }

    private fun transformBitsToRows(bits: MutableList<Bit>) {
        val variations: MutableSet<Int> = HashSet()
        listData.clear()
        bits.sortWith(Comparator.comparingInt(Bit::variationId).thenComparing(order()))
        bits.forEach { bit ->
                val variationId = bit.variationId
                if (!variations.contains(variationId)) {
                    variations.add(variationId)
                    if (variationId != 0) {
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
        intent.putExtra("exerciseId", topBit.exerciseId)
        intent.putExtra("weight", topBit.weight.value.multiply(Constants.ONE_HUNDRED).toInt())
        intent.putExtra("variationId", topBit.variationId)
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
        val fragment: View = findViewById(R.id.fragmentExercise)
        val image: ImageView = findViewById(R.id.image)
        val time: TextView = findViewById(R.id.time)
        val title: TextView = findViewById(R.id.content)

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

        fragment.setOnClickListener {
            val dialog = EditExercisesLastsDialogFragment(R.string.title_exercises,
                exercise, internationalSystem,
                {
                    val data = Intent()
                    data.putExtra("refresh", true)
                    setResult(RESULT_OK, data)
                    runOnUiThread { adapter.notifyItemRangeChanged(0, listData.size) }
                }
            )

            dialog.show(supportFragmentManager, null)
        }
    }
}