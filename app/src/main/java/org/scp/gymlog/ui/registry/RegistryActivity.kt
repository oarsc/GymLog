package org.scp.gymlog.ui.registry

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import org.scp.gymlog.R
import org.scp.gymlog.exceptions.InternalException
import org.scp.gymlog.exceptions.LoadException
import org.scp.gymlog.model.Bit
import org.scp.gymlog.model.Exercise
import org.scp.gymlog.model.Variation
import org.scp.gymlog.model.Weight
import org.scp.gymlog.room.AppDatabase
import org.scp.gymlog.room.DBThread
import org.scp.gymlog.room.entities.BitEntity
import org.scp.gymlog.service.NotificationService
import org.scp.gymlog.service.NotificationService.Companion.lastEndTime
import org.scp.gymlog.ui.common.DBAppCompatActivity
import org.scp.gymlog.ui.common.animations.ResizeWidthAnimation
import org.scp.gymlog.ui.common.components.NumberModifierView
import org.scp.gymlog.ui.common.dialogs.*
import org.scp.gymlog.ui.common.dialogs.model.WeightFormData
import org.scp.gymlog.ui.top.TopActivity
import org.scp.gymlog.ui.training.TrainingActivity
import org.scp.gymlog.util.Constants
import org.scp.gymlog.util.Constants.IntentReference
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.DateUtils.diffSeconds
import org.scp.gymlog.util.FormatUtils.bigDecimal
import org.scp.gymlog.util.FormatUtils.integer
import org.scp.gymlog.util.FormatUtils.safeBigDecimal
import org.scp.gymlog.util.SecondTickThread
import org.scp.gymlog.util.WeightUtils
import java.io.IOException
import java.math.BigDecimal
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Supplier

class RegistryActivity : DBAppCompatActivity() {

    companion object {
        private const val LOG_PAGES_SIZE = 20
    }

    private lateinit var exercise: Exercise
    private var variation: Variation? = null
    private val weight: EditText by lazy { findViewById(R.id.editWeight) }
    private val timer: TextView by lazy { findViewById(R.id.timerSeconds) }
    private val reps: EditText by lazy { findViewById(R.id.editReps) }
    private val notes: EditText by lazy { findViewById(R.id.editNotes) }
    private val weightModifier: NumberModifierView by lazy { findViewById(R.id.weightModifier) }
    private val weightSpecIcon: ImageView by lazy { findViewById(R.id.weightSpecIcon) }
    private val warningIcon: ImageView by lazy { findViewById(R.id.warning) }
    private val confirmInstantButton: ImageView by lazy { findViewById(R.id.confirm) }
    private lateinit var recyclerViewLayout: LinearLayoutManager
    private lateinit var recyclerViewAdapter: LogRecyclerViewAdapter
    private var internationalSystem = false
    private val log: MutableList<Bit> = ArrayList()
    private var trainingId = 0
    private var notesLocked = false
    private var hiddenInstantSetButton = false
    private var sendRefreshList = false
    private lateinit var notificationService: NotificationService
    private var defaultTimer = 0
    private var countdownThread: Thread? = null
    private var activeCountdown: Calendar? = null
    private val defaultTimeColor by lazy { timer.textColors.defaultColor }

    override fun onLoad(savedInstanceState: Bundle?, db: AppDatabase): Int {
        db.trainingDao().getCurrentTraining()
            .ifPresent { trainingEntity -> trainingId = trainingEntity.trainingId }

        val exerciseId = intent.extras!!.getInt("exerciseId")
        val variationId = intent.extras!!.getInt("variationId", 0)

        exercise = Data.exercises
            .filter { ex: Exercise -> ex.id == exerciseId }
            .getOrElse(0) { throw InternalException("Exercise id not found: $exerciseId") }

        if (variationId > 0) {
            variation = exercise.variations
                .filter { v: Variation -> v.id == variationId }
                .getOrElse(0) { throw InternalException("Filter not found: $exerciseId-$variationId") }
        }

        val log: List<BitEntity> = if (variationId > 0)
                db.bitDao().getHistory(exerciseId, variationId, LOG_PAGES_SIZE)
            else
                db.bitDao().getHistory(exerciseId, LOG_PAGES_SIZE)

        log.map { bitEntity: BitEntity -> Bit(bitEntity) }
            .forEach { bit -> this.log.add(bit) }
        return CONTINUE
    }

    override fun onDelayedCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_registry)
        setTitle(R.string.title_registry)

        prepareExerciseListToRefreshWhenFinish()
        notificationService = NotificationService(this)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        internationalSystem = preferences.getBoolean("internationalSystem", true)
        defaultTimer = preferences.getString("restTime", "90")!!.toInt()

        setHeaderInfo()

        // Timer button:
        val timerButton: View = findViewById(R.id.timerButton)
        timerButton.setOnClickListener {
            val dialog = EditTimerDialogFragment(this, R.string.text_notes,
                exercise, activeCountdown) { result ->
                if (exercise.restTime != result) {
                    exercise.restTime = result
                    DBThread.run(this) { db -> db.exerciseDao().update(exercise.toEntity()) }
                }
                if (countdownThread == null) {
                    if (result < 0)
                        timer.text = defaultTimer.toString()
                    else
                        timer.text = result.toString()
                }
            }
            dialog.onPlayListener = BiConsumer { endDate, seconds ->
                this.startTimer(endDate, seconds, false) }
            dialog.onStopListener = Runnable { stopTimer() }
            dialog.show(supportFragmentManager, null)
        }

        val lastEndTime = lastEndTime
        if (lastEndTime != Constants.DATE_ZERO && Calendar.getInstance() < lastEndTime) {
            startTimer(lastEndTime)

        } else {
            timer.setTextColor(defaultTimeColor)
            if (exercise.restTime < 0)
                timer.text = defaultTimer.toString()
            else
                timer.text = exercise.restTime.toString()
        }

        // Variations
        if (exercise.variations.isEmpty()) {
            findViewById<View>(R.id.variationBox).visibility = View.GONE
        } else if (variation != null) {
            val text: TextView = findViewById(R.id.variationText)
            text.text = variation!!.name
        }
        findViewById<View>(R.id.variationBox).setOnClickListener {
            val names = exercise.variations.map(Variation::name).toMutableList()
            names.add(0, resources.getString(R.string.text_default))

            val dialog = TextSelectDialogFragment(names) { idx,_ ->
                if (idx != TextSelectDialogFragment.DIALOG_CLOSED) {
                    if (idx == 0) {
                        switchVariation(0)
                    } else {
                        val id = exercise.variations[idx - 1].id
                        switchVariation(id)
                    }
                }
            }
            dialog.show(supportFragmentManager, null)
        }

        // Logs:
        recyclerViewLayout = LinearLayoutManager(this)
        recyclerViewAdapter = LogRecyclerViewAdapter(log, exercise, trainingId, internationalSystem)

        recyclerViewAdapter.onClickElementListener = BiConsumer { view, bit -> onClickBit(view, bit) }
        recyclerViewAdapter.onLoadMoreListener = Runnable { loadMoreHistory() }
        if (log.size < LOG_PAGES_SIZE - 1) {
            recyclerViewAdapter.setFullyLoaded(true)
        }

        findViewById<RecyclerView>(R.id.log_list).apply {
            layoutManager = recyclerViewLayout
            adapter = recyclerViewAdapter
            isNestedScrollingEnabled = true
        }

        // Save bit log
        findViewById<View>(R.id.confirmSet).setOnClickListener { saveBitLog(false) }
        confirmInstantButton.setOnClickListener { saveBitLog(true) }

        hiddenInstantSetButton = log.map(Bit::trainingId).none { id -> id == trainingId }

        if (hiddenInstantSetButton) {
            confirmInstantButton.layoutParams.width = 0
        }

        // Notes
        notes.setOnClickListener {
            val dialog = EditNotesDialogFragment(R.string.text_notes, exercise.id, notes.text.toString())
                { result: String -> notes.setText(result) }
            dialog.show(supportFragmentManager, null)
        }

        val clearNote: ImageView = findViewById(R.id.clearNote)
        val lockNote: ImageView = findViewById(R.id.lockNote)
        clearNote.setOnClickListener {
            notes.text.clear()
            if (notesLocked) {
                notesLocked = false
                lockNote.setImageResource(R.drawable.ic_unlock_24dp)
            }
        }

        lockNote.setOnClickListener {
            if (notes.text.toString().isNotEmpty()) {
                notesLocked = !notesLocked
                if (notesLocked) {
                    lockNote.setImageResource(R.drawable.ic_lock_24dp)
                } else {
                    lockNote.setImageResource(R.drawable.ic_unlock_24dp)
                }
            }
        }

        // Weight and Reps Input fields:
        weight.filters = arrayOf(InputFilter { source: CharSequence, _, _, dest: Spanned, _, _ ->
            val input = (dest.toString() + source.toString()).safeBigDecimal()
            if (input < Constants.ONE_THOUSAND && input.scale() < 3)
                null
            else
                ""
        })
        weight.setOnClickListener { showWeightDialog(weight) }

        reps.setOnClickListener {
            val dialog = EditNumberDialogFragment(R.string.text_reps, reps.text.toString(),
                { result: BigDecimal -> reps.bigDecimal = result })
            dialog.show(supportFragmentManager, null)
        }

        val unitTextView: TextView = findViewById(R.id.unit)
        unitTextView.setText(WeightUtils.unit(internationalSystem))

        weightModifier.setStep(exercise.step)

        weightSpecIcon.setImageResource(exercise.weightSpec.icon)

        if (exercise.requiresBar == (exercise.bar == null)) {
            warningIcon.visibility = View.VISIBLE
        } else {
            warningIcon.visibility = View.INVISIBLE
        }

        loadHistory()
    }

    private fun setHeaderInfo() {
        val fragment: View = findViewById(R.id.fragmentExercise)
        val title: TextView = findViewById(R.id.content)
        val time: TextView = findViewById(R.id.time)
        val image: ImageView = findViewById(R.id.image)

        fragment.isClickable = false
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.registry_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.topRanking) {
            val intent = Intent(this, TopActivity::class.java)
            intent.putExtra("exerciseId", exercise.id)
            startActivityForResult(intent, IntentReference.TOP_RECORDS)
            return true
        }
        return false
    }

    override fun onActivityResult(intentReference: IntentReference, data: Intent) {
        if (data.getBooleanExtra("refresh", false)) {
            when (intentReference) {
                IntentReference.TOP_RECORDS -> {
                    recyclerViewAdapter.notifyItemRangeChanged(0, log.size)
                    updateForms()
                }
                IntentReference.TRAINING -> {
                    DBThread.run(this) { db ->
                        val log = db.bitDao().getHistory(exercise.id, LOG_PAGES_SIZE)
                        this.log.clear()
                        log.map { entity: BitEntity -> Bit(entity) }
                            .forEach { e -> this.log.add(e) }
                        runOnUiThread { recyclerViewAdapter.notifyDataSetChanged() }
                    }
                    updateForms()
                }
                else -> {}
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun switchVariation(variationId: Int) {
        DBThread.run(this) { db ->
            val log: List<BitEntity>
            val exerciseId = exercise.id

            if (variationId == 0) {
                log = db.bitDao().getHistory(exerciseId, LOG_PAGES_SIZE)
                variation = null
            } else {
                log = db.bitDao().getHistory(exerciseId, variationId, LOG_PAGES_SIZE)
                variation = exercise.variations
                    .filter { v: Variation -> v.id == variationId }
                    .getOrElse(0) { throw InternalException("Variation not found: $exerciseId-$variationId") }
            }

            this.log.clear()
            log.map { bitEntity -> Bit(bitEntity) }
                .forEach { e -> this.log.add(e) }

            runOnUiThread {
                recyclerViewAdapter.notifyDataSetChanged()
                recyclerViewAdapter.setFullyLoaded(log.size < LOG_PAGES_SIZE - 1)

                val text: TextView = findViewById(R.id.variationText)
                if (variation == null)
                    text.setText(R.string.text_default)
                else
                    text.text = variation!!.name
            }
        }
    }

    private fun showWeightDialog(weightEditText: EditText?) {
        val weightFormData = WeightFormData()

        val weight = Weight(weightEditText!!.bigDecimal, internationalSystem)
        weightFormData.weight = weight
        weightFormData.step = exercise.step
        weightFormData.bar = exercise.bar
        weightFormData.requiresBar = exercise.requiresBar
        weightFormData.weightSpec = exercise.weightSpec

        val dialog = EditWeightFormDialogFragment(weightFormData, R.string.text_weight, { result: WeightFormData ->
                weightEditText.bigDecimal = result.weight!!.value
                if (result.exerciseUpdated) {
                    exercise.bar = result.bar
                    exercise.step = result.step!!
                    exercise.weightSpec = result.weightSpec!!
                    recyclerViewAdapter.notifyItemRangeChanged(0, log.size)

                    updateForms()
                    DBThread.run(this) { db -> db.exerciseDao().update(exercise.toEntity()) }
                }
            }
        )
        dialog.show(supportFragmentManager, null)
    }

    private fun updateForms() {
        weightModifier.setStep(exercise.step)
        weightSpecIcon.setImageResource(exercise.weightSpec.icon)
        if (exercise.requiresBar == (exercise.bar == null)) {
            warningIcon.visibility = View.VISIBLE
        } else {
            warningIcon.visibility = View.INVISIBLE
        }
    }

    private fun loadHistory() {
        if (log.isNotEmpty()) {
            val bit = log[0]
            reps.integer = bit.reps

            val partialWeight = WeightUtils.getWeightFromTotal(
                bit.weight,
                exercise.weightSpec,
                exercise.bar,
                internationalSystem)

            weight.bigDecimal = partialWeight
        } else {
            reps.setText("10")
        }
    }

    private fun loadMoreHistory() {
        val initialSize = log.size
        DBThread.run(this) { db ->
            val bit = log[initialSize - 1]
            val date = bit.timestamp
            val log: List<BitEntity> = if (variation == null)
                    db.bitDao().getHistory(exercise.id, bit.trainingId,
                        date, LOG_PAGES_SIZE)
                else
                    db.bitDao().getHistory(exercise.id, variation!!.id, bit.trainingId,
                        date, LOG_PAGES_SIZE)

            log.map { bitEntity -> Bit(bitEntity) }
                .forEach { b -> this.log.add(b) }

            runOnUiThread {
                recyclerViewAdapter.notifyItemRangeInserted(initialSize, log.size)
                if (log.size < LOG_PAGES_SIZE - 1) {
                    recyclerViewAdapter.setFullyLoaded(true)
                }
            }
        }
    }

    private fun saveBitLog(instant: Boolean) {
        if (trainingId <= 0) {
            Snackbar.make(findViewById(android.R.id.content),
                R.string.validation_training_not_started, Snackbar.LENGTH_LONG).show()
            return
        }
        DBThread.run(this) { db ->
            val bit = Bit(exercise.id, if (variation == null) 0 else variation!!.id)

            val totalWeight = WeightUtils.getTotalWeight(
                weight.bigDecimal,
                exercise.weightSpec,
                exercise.bar,
                internationalSystem)

            bit.weight = Weight(totalWeight, internationalSystem)
            bit.note = notes.text.toString()
            bit.reps = reps.integer
            bit.trainingId = trainingId
            bit.instant = instant

            exercise.lastTrained = Calendar.getInstance()

            // SAVE TO DB:
            bit.id = db.bitDao().insert(bit.toEntity()).toInt()
            db.exerciseDao().update(exercise.toEntity())
            prepareExerciseListToRefreshWhenFinish()

            runOnUiThread {
                var added = false
                var idx = 0
                for (logBit in log) {
                    if (logBit.trainingId == trainingId) {
                        idx++
                    } else {
                        log.add(idx, bit)
                        recyclerViewAdapter.notifyItemInserted(idx)
                        recyclerViewLayout.scrollToPosition(0)
                        added = true
                        break
                    }
                }

                if (!added) {
                    log.add(bit)
                    recyclerViewAdapter.notifyItemInserted(log.size - 1)
                }

                if (!notesLocked) {
                    notes.setText(R.string.symbol_empty)
                }

                if (hiddenInstantSetButton) {
                    hiddenInstantSetButton = false
                    val anim = ResizeWidthAnimation(confirmInstantButton, 90, 250)
                    confirmInstantButton.startAnimation(anim)
                }
                startTimer()
            }
        }
    }

    private fun removeBitLog(bit: Bit) {
        DBThread.run(this) { db ->
            db.bitDao().delete(bit.toEntity())

            val index = log.indexOf(bit)
            val trainingId = bit.trainingId
            log.removeAt(index)

            if (log.size > index && !bit.instant && log[index].instant &&
                    log[index].trainingId == trainingId) {
                val updateBit = log[index]
                updateBit.instant = false
                db.bitDao().update(updateBit.toEntity())
            }

            if (log.map(Bit::trainingId).none { id -> id == trainingId }) {
                db.trainingDao().deleteEmptyTraining()
            }

            runOnUiThread {
                recyclerViewAdapter.notifyItemRemoved(index)
                if (index == 0) {
                    if (log.isNotEmpty()) {
                        if (log[0].trainingId != trainingId) {
                            recyclerViewAdapter.notifyItemChanged(0)
                        } else {
                            recyclerViewAdapter.notifyTrainingIdChanged(trainingId, 0)
                        }
                    }
                } else {
                    recyclerViewAdapter.notifyTrainingIdChanged(trainingId, index)
                }
            }
        }
    }

    private fun updateBitLog(bit: Bit, updateTrainingId: Boolean) {
        DBThread.run(this) { db ->
            db.bitDao().update(bit.toEntity())
            val index = log.indexOf(bit)
            runOnUiThread {
                if (updateTrainingId)
                    recyclerViewAdapter.notifyItemChanged(index)
                else
                    recyclerViewAdapter.notifyTrainingIdChanged(bit.trainingId, index)
            }
        }
    }

    private fun onClickBit(view: View, bit: Bit) {
        view.setBackgroundColor(resources.getColor(R.color.backgroundAccent, theme))
        val dialog = MenuDialogFragment(R.menu.bit_menu) { result: Int ->
            when (result) {
                R.id.showTraining -> {
                    val intent = Intent(this, TrainingActivity::class.java)
                    intent.putExtra("trainingId", bit.trainingId)
                    intent.putExtra("focusBit", bit.id)
                    startActivityForResult(intent, IntentReference.TRAINING)
                }
                R.id.editBit -> {
                    val enableInstantSwitch = log
                        .filter { b -> b.trainingId == bit.trainingId }
                        .getOrNull(0) !== bit
                    val initialInstant = enableInstantSwitch && bit.instant

                    val editDialog = EditBitLogDialogFragment(
                        R.string.title_registry,
                        exercise,
                        enableInstantSwitch,
                        internationalSystem,
                        bit,
                        { b -> updateBitLog(b, initialInstant == b.instant) })

                    editDialog.show(supportFragmentManager, null)
                }
                R.id.removeBit -> removeBitLog(bit)
            }
            view.setBackgroundColor(0x00000000)
        }
        dialog.show(supportFragmentManager, null)
    }

    private fun prepareExerciseListToRefreshWhenFinish() {
        if (!sendRefreshList) {
            sendRefreshList = true
            val data = Intent()
            data.putExtra("refresh", true)
            data.putExtra("exerciseId", exercise.id)
            setResult(RESULT_OK, data)
        }
    }

    private fun startTimer() {
        val seconds = if (exercise.restTime < 0) defaultTimer else exercise.restTime
        val endDate = Calendar.getInstance()
        endDate.add(Calendar.SECOND, seconds)
        startTimer(endDate, seconds)
    }

    private fun startTimer(endDate: Calendar, seconds: Int, restart: Boolean = true) {
        if (seconds > 0) {
            notificationService.showNotification(endDate, seconds, exercise.name, restart)
            startTimer(endDate)
        }
    }

    private fun startTimer(endDate: Calendar) {
        activeCountdown = endDate
        if (countdownThread == null) {
            countdownThread = CountdownThread().also(Thread::start)

            val color = resources.getColor(R.color.orange_light, theme)
            timer.setTextColor(color)
            findViewById<TextView>(R.id.secondsText).setTextColor(color)
        }
    }

    private fun stopTimer() {
        activeCountdown = null
        notificationService.hideNotification()
        countdownThread?.interrupt()
    }

    private inner class CountdownThread :
        SecondTickThread(Supplier {
            val seconds = activeCountdown!!.diffSeconds()
            if (seconds > 0) {
                runOnUiThread { timer.text = seconds.toString() }
                true
            } else false
        }) {

        init {
            onFinishListener = Runnable {
                countdownThread = null
                runOnUiThread {
                    if (exercise.restTime < 0)
                        timer.text = defaultTimer.toString()
                    else
                        timer.text = exercise.restTime.toString()

                    timer.setTextColor(defaultTimeColor)
                    findViewById<TextView>(R.id.secondsText).setTextColor(defaultTimeColor)
                }
            }
        }
    }
}
