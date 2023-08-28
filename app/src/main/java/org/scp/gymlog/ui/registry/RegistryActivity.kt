package org.scp.gymlog.ui.registry

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
import org.scp.gymlog.R
import org.scp.gymlog.exceptions.LoadException
import org.scp.gymlog.model.*
import org.scp.gymlog.room.AppDatabase
import org.scp.gymlog.room.entities.TrainingEntity
import org.scp.gymlog.service.NotificationService
import org.scp.gymlog.service.NotificationService.Companion.lastEndTime
import org.scp.gymlog.ui.common.DBAppCompatActivity
import org.scp.gymlog.ui.common.animations.ResizeHeightAnimation
import org.scp.gymlog.ui.common.animations.ResizeWidthAnimation
import org.scp.gymlog.ui.common.components.NumberModifierView
import org.scp.gymlog.ui.common.dialogs.*
import org.scp.gymlog.ui.common.dialogs.model.WeightFormData
import org.scp.gymlog.ui.exercises.LatestActivity
import org.scp.gymlog.ui.top.TopActivity
import org.scp.gymlog.ui.training.TrainingActivity
import org.scp.gymlog.util.Constants
import org.scp.gymlog.util.Constants.IntentReference
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.DateUtils.currentDateTime
import org.scp.gymlog.util.DateUtils.diffSeconds
import org.scp.gymlog.util.DateUtils.isPast
import org.scp.gymlog.util.FormatUtils.bigDecimal
import org.scp.gymlog.util.FormatUtils.integer
import org.scp.gymlog.util.FormatUtils.safeBigDecimal
import org.scp.gymlog.util.SecondTickThread
import org.scp.gymlog.util.WeightUtils
import org.scp.gymlog.util.WeightUtils.calculate
import org.scp.gymlog.util.WeightUtils.calculateTotal
import org.scp.gymlog.util.extensions.DatabaseExts.dbThread
import org.scp.gymlog.util.extensions.MessagingExts.snackbar
import java.io.IOException
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.function.BiConsumer


class RegistryActivity : DBAppCompatActivity() {

    companion object {
        private const val LOG_PAGES_SIZE = 20
    }

    private lateinit var exercise: Exercise
    private lateinit var variation: Variation
    private val weight by lazy { findViewById<EditText>(R.id.editWeight) }
    private var defaultTimeColor = 0
    private val timer by lazy { findViewById<TextView>(R.id.timerSeconds)
        .also { defaultTimeColor = it.textColors.defaultColor } }
    private val reps by lazy { findViewById<EditText>(R.id.editReps) }
    private val notes by lazy { findViewById<EditText>(R.id.editNotes) }
    private val superSet by lazy { findViewById<ImageView>(R.id.superSet) }
    private val superSetPanel by lazy { findViewById<TextView>(R.id.activeSuperset) }
    private val weightModifier by lazy { findViewById<NumberModifierView>(R.id.weightModifier) }
    private val weightSpecIcon by lazy { findViewById<ImageView>(R.id.weightSpecIcon) }
    private val confirmInstantButton by lazy { findViewById<ImageView>(R.id.confirm) }
    private lateinit var recyclerViewLayout: LinearLayoutManager
    private lateinit var recyclerViewAdapter: LogRecyclerViewAdapter
    private var internationalSystem = false
    private val log: MutableList<Bit> = ArrayList()
    private var locked = false
    private var hiddenInstantSetButton = true
    private var sendRefreshList = false
    private val notificationService: NotificationService by lazy { NotificationService(this) }
    private var defaultTimer = 0
    private var countdownThread: CountdownThread? = null

    override fun onLoad(savedInstanceState: Bundle?, db: AppDatabase): Int {
        val exerciseId = intent.extras!!.getInt("exerciseId")
        val variationId = intent.extras!!.getInt("variationId", 0)

        exercise = Data.getExercise(exerciseId)
        variation = if (variationId > 0) {
            Data.getVariation(exercise, variationId)
        } else {
            exercise.defaultVariation
        }

        val log = if (variation.gymRelation == GymRelation.NO_RELATION)
                db.bitDao().getHistory(variation.id, LOG_PAGES_SIZE)
            else
                db.bitDao().getHistory(Data.currentGym, variation.id, LOG_PAGES_SIZE)

        log.map { Bit(it) }
            .also { this.log.addAll(it) }

        Data.trainingId?.also { trainingId ->
            hiddenInstantSetButton = db.bitDao().getMostRecentByTrainingId(trainingId)
                ?.let { it.variationId != variationId }
                ?: true
        }

        return CONTINUE
    }

    override fun onDelayedCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_registry)
        setTitle(R.string.title_registry)

        prepareExerciseListToRefreshWhenFinish()

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        internationalSystem = preferences.getBoolean("internationalSystem", true)
        defaultTimer = preferences.getString("restTime", "90")!!.toInt()

        setHeaderInfo()

        // Timer button:
        val timerButton = findViewById<View>(R.id.timerButton)
        timerButton.setOnClickListener {
            val dialog = EditTimerDialogFragment(this, R.string.text_notes, variation) { result ->
                if (variation.restTime != result) {
                    variation.restTime = result
                    dbThread { db -> db.variationDao().update(variation.toEntity()) }
                }
                //if (countdownThread == null) {
                    updateTimer(result)
                //}
            }
            dialog.onPlayListener = BiConsumer { endDate, seconds ->
                if (lastEndTime.isPast)
                    startTimer(endDate, seconds)
                else
                    editTimer(endDate, seconds)
            }
            dialog.onStopListener = Runnable { stopTimer() }
            dialog.show(supportFragmentManager, null)
        }

        if (!lastEndTime.isPast) {
            updateRunningTimer()

        } else {
            timer.setTextColor(defaultTimeColor)
            updateTimer()
        }

        // Logs:
        recyclerViewLayout = LinearLayoutManager(this)
        recyclerViewAdapter = LogRecyclerViewAdapter(log, Data.trainingId, internationalSystem)

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

        if (hiddenInstantSetButton) {
            confirmInstantButton.layoutParams.width = 0
        }

        // Notes
        notes.setOnClickListener {
            val dialog = EditNotesDialogFragment(R.string.text_notes, variation, notes.text.toString())
                { result: String -> notes.setText(result) }
            dialog.show(supportFragmentManager, null)
        }

        val clearNote = findViewById<ImageView>(R.id.clearNote)
        val lockView = findViewById<ImageView>(R.id.lock)
        clearNote.setOnClickListener {
            notes.text.clear()
        }

        lockView.setOnClickListener {
            locked = !locked
            if (locked)
                lockView.setImageResource(R.drawable.ic_lock_24dp)
            else
                lockView.setImageResource(R.drawable.ic_unlock_24dp)
        }

        // Super set button
        superSet.setOnClickListener {
            requireActiveTraining(false) { trainingId ->
                if (Data.superSet == null) {
                    dbThread { db ->
                        Data.superSet = (db.bitDao().getMaxSuperSet(trainingId) ?: 0) +1
                        runOnUiThread { updateSuperSetIcon() }
                    }
                } else {
                    Data.superSet = null
                    updateSuperSetIcon()
                }
            }
        }
        superSetPanel.setOnClickListener {
            if (Data.superSet != null) {
                Data.superSet = null
                updateSuperSetIcon()
            }
        }
        updateSuperSetIcon(true)

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

        val unitTextView = findViewById<TextView>(R.id.unit)
        unitTextView.setText(WeightUtils.unit(internationalSystem))

        weightModifier.setStep(variation.step)

        val warningColor = if ((variation.type === ExerciseType.BARBELL) == (variation.bar == null))
                resources.getColor(R.color.orange_light, theme)
            else
                resources.getColor(R.color.themedIcon, theme)

        weightSpecIcon.setColorFilter(warningColor)
        weightSpecIcon.setImageResource(variation.weightSpec.icon)

        precalculateWeight()
    }

    private fun setHeaderInfo() {
        val fragment = findViewById<View>(R.id.fragmentExercise)
        val title = findViewById<TextView>(R.id.exerciseName)
        val subtitle = findViewById<TextView>(R.id.variationName)
        val time = findViewById<TextView>(R.id.time)
        val image = findViewById<ImageView>(R.id.image)

        fragment.isClickable = false
        title.text = exercise.name
        time.visibility = View.GONE

        if (variation.default) {
            subtitle.visibility = View.GONE
        } else {
            subtitle.text = variation.name
        }

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
        when (item.itemId) {
            R.id.latestButton -> {
                val intent = Intent(this, LatestActivity::class.java)
                startActivity(intent)
            }
            R.id.topRanking -> {
                val intent = Intent(this, TopActivity::class.java)
                intent.putExtra("exerciseId", exercise.id)
                startActivityForResult(intent, IntentReference.TOP_RECORDS)
                return true
            }
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
                    dbThread { db ->
                        val log = if (variation.gymRelation == GymRelation.NO_RELATION)
                            db.bitDao().getHistory(variation.id, LOG_PAGES_SIZE)
                        else
                            db.bitDao().getHistory(Data.currentGym, variation.id, LOG_PAGES_SIZE)

                        this.log.clear()
                        log.map { Bit(it) }
                            .also { this.log.addAll(it) }
                        runOnUiThread { recyclerViewAdapter.notifyDataSetChanged() }
                    }
                    updateForms()
                }
                else -> {}
            }
        }
    }

    private fun showWeightDialog(weightEditText: EditText) {
        val weightFormData = WeightFormData()

        val weight = Weight(weightEditText.bigDecimal, internationalSystem)
        weightFormData.weight = weight
        weightFormData.step = variation.step
        weightFormData.bar = variation.bar
        weightFormData.type = variation.type
        weightFormData.weightSpec = variation.weightSpec

        val dialog = EditWeightFormDialogFragment(weightFormData, R.string.text_weight, { result: WeightFormData ->
                weightEditText.bigDecimal = result.weight!!.value
                if (result.exerciseUpdated) {
                    variation.bar = result.bar
                    variation.step = result.step!!
                    variation.weightSpec = result.weightSpec
                    recyclerViewAdapter.notifyItemRangeChanged(0, log.size)

                    updateForms()
                    dbThread { db -> db.variationDao().update(variation.toEntity()) }
                }
            }
        )
        dialog.show(supportFragmentManager, null)
    }

    private fun updateForms() {
        weightModifier.setStep(variation.step)

        val warningColor = if ((variation.type === ExerciseType.BARBELL) == (variation.bar == null))
                resources.getColor(R.color.orange_light, theme)
            else
                resources.getColor(R.color.themedIcon, theme)

        weightSpecIcon.setColorFilter(warningColor)
        weightSpecIcon.setImageResource(variation.weightSpec.icon)
    }

    private fun updateSuperSetIcon(onInit: Boolean = false) {
        if (Data.superSet == null) {
            superSet.setImageResource(R.drawable.ic_super_set_24dp)

            if (onInit) {
                superSetPanel.visibility = View.GONE
            } else {
                val anim = ResizeHeightAnimation(superSetPanel, 0, 250, true)
                superSetPanel.startAnimation(anim)
            }
        } else {
            superSet.setImageResource(R.drawable.ic_super_set_on_24dp)
            superSetPanel.text = String.format(getString(R.string.text_active_superset), Data.superSet)

            if (!onInit) {
                val anim = ResizeHeightAnimation(superSetPanel, 48, 250, true)
                superSetPanel.startAnimation(anim)
            }
        }
    }

    private fun updateTimer(value: Int = variation.restTime) {
        if (lastEndTime.isPast) {
            timer.text = if (value < 0)
                defaultTimer.toString()
            else
                value.toString()
        }
    }

    private fun precalculateWeight() {
        if (log.isNotEmpty()) {
            var currentSet = 1
            var lastTraining = -1
            var lastSet = 0
            var bit: Bit = log[0]

            for (b in log) {
                if (b.trainingId == Data.trainingId) {
                    if (!b.instant) {
                        bit = b
                        currentSet++
                    }

                } else if (lastTraining < 0 || lastTraining == b.trainingId){
                    lastTraining = b.trainingId
                    if (!b.instant) {
                        lastSet++
                        if (lastSet == currentSet) {
                            bit = b
                            break
                        }
                    }
                } else {
                    break
                }
            }

            reps.integer = bit.reps

            val partialWeight = bit.weight.calculate(
                variation.weightSpec,
                variation.bar)

            weight.bigDecimal = partialWeight.getValue(internationalSystem)
        } else {
            reps.setText("10")
        }
    }

    private fun loadMoreHistory() {
        val initialSize = log.size
        dbThread { db ->
            val bit = log[initialSize - 1]
            val date = bit.timestamp

            val log = if (variation.gymRelation == GymRelation.NO_RELATION)
                db.bitDao().getHistory(variation.id, bit.trainingId, date, LOG_PAGES_SIZE)
            else
                db.bitDao().getHistory(Data.currentGym, variation.id, bit.trainingId, date, LOG_PAGES_SIZE)

            log.map { Bit(it) }
                .also { this.log.addAll(it) }

            runOnUiThread {
                recyclerViewAdapter.notifyItemRangeInserted(initialSize, log.size)
                if (log.size < LOG_PAGES_SIZE - 1) {
                    recyclerViewAdapter.setFullyLoaded(true)
                }
            }
        }
    }

    private fun saveBitLog(instant: Boolean) {
        requireActiveTraining { trainingId ->

            dbThread { db ->
                val bit = Bit(variation)

                val totalWeight = Weight(weight.bigDecimal, internationalSystem).calculateTotal(
                    variation.weightSpec,
                    variation.bar)

                bit.weight = totalWeight
                bit.note = notes.text.toString()
                bit.reps = reps.integer
                bit.trainingId = trainingId
                bit.instant = instant
                bit.gymId = Data.currentGym
                bit.superSet = Data.superSet ?: 0

                exercise.lastTrained = currentDateTime()

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

                    if (!locked) {
                        notes.setText(R.string.symbol_empty)
                    }

                    if (hiddenInstantSetButton) {
                        val anim = ResizeWidthAnimation(confirmInstantButton, 90, 250, true)
                        confirmInstantButton.startAnimation(anim)
                    }
                    startTimer()
                    if (!locked)
                        precalculateWeight()
                }
            }
        }
    }

    private fun removeBitLog(bit: Bit) {
        dbThread { db ->
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

            if (log.none { it.trainingId == trainingId }) {
                Data.trainingId
                    ?.also { db.trainingDao().deleteEmptyTrainingExcept(it) }
                    ?: db.trainingDao().deleteEmptyTraining()
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
        dbThread { db ->
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

    private fun requireActiveTraining(createDialog: Boolean = true, block: (trainingId: Int) -> Unit) {
        Data.trainingId
            ?.also { block(it) }
            ?: run {
                if (!createDialog) {
                    snackbar(R.string.validation_training_not_started)
                } else {
                    dbThread { db ->
                        val maxId = (db.trainingDao().getMaxTrainingId() ?: 0) + 1

                        val dialogText = String.format(
                            getString(R.string.dialog_confirm_init_training_text),
                            maxId
                        )

                        runOnUiThread {
                            val dialog = TextDialogFragment(
                                R.string.dialog_confirm_init_training_title,
                                dialogText
                            ) { confirmed ->
                                if (confirmed) {
                                    dbThread { db ->
                                        val training = TrainingEntity()
                                        training.trainingId = maxId
                                        training.start = currentDateTime()
                                        Data.trainingId = db.trainingDao().insert(training).toInt()
                                        runOnUiThread { block(training.trainingId) }
                                    }
                                }
                            }
                            dialog.show(supportFragmentManager, null)
                        }
                    }
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
                    dbThread { db ->
                        val enableInstantSwitch = db.bitDao().getPreviousByTraining(bit.trainingId, bit.timestamp)
                            ?.let { it.variationId == variation.id}
                            ?: false

                        val initialInstant = enableInstantSwitch && bit.instant

                        val editDialog = EditBitLogDialogFragment(
                            R.string.title_registry,
                            enableInstantSwitch,
                            internationalSystem,
                            bit,
                            { b -> updateBitLog(b, initialInstant == b.instant) })

                        runOnUiThread {
                            editDialog.show(supportFragmentManager, null)
                        }
                    }
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
        val seconds = if (variation.restTime < 0) defaultTimer else variation.restTime
        val endDate = currentDateTime().plusSeconds(seconds.toLong())
        startTimer(endDate, seconds)
    }

    private fun startTimer(endDate: LocalDateTime, seconds: Int) {
        if (seconds > 0) {
            notificationService.showNotification(endDate, seconds, exercise.name)
            updateRunningTimer()
        }
    }

    private fun editTimer(endDate: LocalDateTime, seconds: Int) {
        if (!lastEndTime.isPast) {
            notificationService.editNotification(endDate, seconds)
            updateRunningTimer()
        }
    }

    private fun updateRunningTimer() {
        countdownThread?.onTick() ?: run {
            countdownThread = CountdownThread().also(Thread::start)

            val color = resources.getColor(R.color.orange_light, theme)
            timer.setTextColor(color)
            findViewById<TextView>(R.id.secondsText).setTextColor(color)
        }
    }

    private fun stopTimer() {
        notificationService.hideNotification()
        countdownThread?.interrupt()
    }

    private inner class CountdownThread : SecondTickThread() {

        override fun onTick(): Boolean {
            if (lastEndTime.isPast)
                return false

            val seconds = lastEndTime.diffSeconds()
            runOnUiThread { timer.text = seconds.toString() }
            return true
        }

        override fun onFinish() {
            countdownThread = null
            runOnUiThread {
                updateTimer()
                timer.setTextColor(defaultTimeColor)
                findViewById<TextView>(R.id.secondsText).setTextColor(defaultTimeColor)
            }
        }
    }
}
