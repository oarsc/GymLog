package org.scp.gymlog.ui.registry

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import org.scp.gymlog.R
import org.scp.gymlog.databinding.ListitemLogBinding
import org.scp.gymlog.model.Bit
import org.scp.gymlog.model.Exercise
import org.scp.gymlog.model.ExerciseType
import org.scp.gymlog.model.GymRelation
import org.scp.gymlog.model.Training
import org.scp.gymlog.model.Variation
import org.scp.gymlog.model.Weight
import org.scp.gymlog.model.WeightSpecification
import org.scp.gymlog.room.AppDatabase
import org.scp.gymlog.room.entities.TrainingEntity
import org.scp.gymlog.service.NotificationService
import org.scp.gymlog.service.NotificationService.Companion.lastEndTime
import org.scp.gymlog.ui.common.DBAppCompatActivity
import org.scp.gymlog.ui.common.animations.ResizeHeightAnimation
import org.scp.gymlog.ui.common.components.ExerciseImageView
import org.scp.gymlog.ui.common.components.NumberModifierView
import org.scp.gymlog.ui.common.components.SideButtonsLinearLayoutView
import org.scp.gymlog.ui.common.components.listView.SimpleListView
import org.scp.gymlog.ui.common.dialogs.EditBitLogDialogFragment
import org.scp.gymlog.ui.common.dialogs.EditNotesDialogFragment
import org.scp.gymlog.ui.common.dialogs.EditNumberDialogFragment
import org.scp.gymlog.ui.common.dialogs.EditTimerDialogFragment
import org.scp.gymlog.ui.common.dialogs.EditWeightFormDialogFragment
import org.scp.gymlog.ui.common.dialogs.MenuDialogFragment
import org.scp.gymlog.ui.common.dialogs.TextDialogFragment
import org.scp.gymlog.ui.common.dialogs.TextSelectDialogFragment
import org.scp.gymlog.ui.common.dialogs.model.WeightFormData
import org.scp.gymlog.ui.exercises.ExercisesActivity
import org.scp.gymlog.ui.exercises.LatestActivity
import org.scp.gymlog.ui.main.MainActivity
import org.scp.gymlog.ui.preferences.PreferencesDefinition
import org.scp.gymlog.ui.top.TopActivity
import org.scp.gymlog.ui.training.TrainingActivity
import org.scp.gymlog.util.Constants
import org.scp.gymlog.util.Constants.IntentReference
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.DateUtils.NOW
import org.scp.gymlog.util.DateUtils.currentDateTime
import org.scp.gymlog.util.DateUtils.diffSeconds
import org.scp.gymlog.util.DateUtils.isPast
import org.scp.gymlog.util.DateUtils.isSet
import org.scp.gymlog.util.FormatUtils.bigDecimal
import org.scp.gymlog.util.FormatUtils.integer
import org.scp.gymlog.util.FormatUtils.safeBigDecimal
import org.scp.gymlog.util.SecondTickThread
import org.scp.gymlog.util.WeightUtils
import org.scp.gymlog.util.WeightUtils.calculate
import org.scp.gymlog.util.WeightUtils.calculateTotal
import org.scp.gymlog.util.extensions.ComponentsExts.overridePendingSideTransition
import org.scp.gymlog.util.extensions.ComponentsExts.startResizeWidthAnimation
import org.scp.gymlog.util.extensions.DatabaseExts.dbThread
import org.scp.gymlog.util.extensions.DatabaseExts.getDbConnection
import org.scp.gymlog.util.extensions.MessagingExts.snackbar
import org.scp.gymlog.util.extensions.PreferencesExts.loadBoolean
import org.scp.gymlog.util.extensions.PreferencesExts.loadString
import java.math.BigDecimal
import java.time.LocalDateTime


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
    private val bottomButtonPanel by lazy { findViewById<SideButtonsLinearLayoutView>(R.id.bottomButtonPanel) }
    private val confirmInstantButton by lazy { findViewById<ImageView>(R.id.confirmInstantButton) }
    private val mainButton by lazy { findViewById<ImageView>(R.id.mainButton) }
    private var initInstantSetButtonHidden = true
    private var enableSuperSetNavigation = false
    private var lastSuperSet = 0
    private lateinit var logListHandler: LogListHandler
    private lateinit var logListView: SimpleListView<Bit, ListitemLogBinding>
    private var internationalSystem = false
    private val log: MutableList<Bit> = ArrayList()
    private var locked = false
    private var sendRefreshList = false
    private val notificationService: NotificationService by lazy { NotificationService(this) }
    private var defaultTimer = 0
    private var countdownThread: CountdownThread? = null
    private var reloadOnBack = false

    override fun onLoad(savedInstanceState: Bundle?, db: AppDatabase): Int {
        val exerciseId = intent.extras!!.getInt("exerciseId")
        val variationId = intent.extras!!.getInt("variationId", 0)
        reloadOnBack = intent.extras!!.getBoolean("reloadOnBack", false)

        exercise = Data.getExercise(exerciseId)
        variation = if (variationId > 0) {
            Data.getVariation(exercise, variationId)
        } else {
            exercise.defaultVariation
        }

        val log = if (variation.gymRelation == GymRelation.NO_RELATION)
                db.bitDao().getHistory(variation.id, LOG_PAGES_SIZE)
            else
                db.bitDao().getHistory(Data.gym?.id ?: 0, variation.id, LOG_PAGES_SIZE)

        log.map { Bit(it) }
            .also { this.log.addAll(it) }

        Data.training?.also { training ->
            val bitDao = db.bitDao()

            val mostRecentTraining = bitDao.getMostRecentByTrainingId(training.id) ?: return@also
            initInstantSetButtonHidden = mostRecentTraining.variationId != variationId ||
                mostRecentTraining.superSet != (Data.superSet ?: 0)

            lastSuperSet = mostRecentTraining.superSet
            if (lastSuperSet > 0 && Data.superSet == lastSuperSet) {
                enableSuperSetNavigation = bitDao.getHistoryByTrainingId(training.id)
                    .filter { it.superSet == lastSuperSet }
                    .map { it.variationId }
                    .distinct()
                    .size > 1
            }
        }

        return CONTINUE
    }

    override fun onDelayedCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_registry)
        setTitle(R.string.title_registry)

        prepareExerciseListToRefreshWhenFinish()

        internationalSystem = loadBoolean(PreferencesDefinition.UNIT_INTERNATIONAL_SYSTEM)
        defaultTimer = loadString(PreferencesDefinition.DEFAULT_REST_TIME).toInt()

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
            dialog.setOnPlayListener { endDate, seconds ->
                if (lastEndTime.isSet)
                    editTimer(endDate)
                else
                    startTimer(endDate, seconds)
            }
            dialog.setOnStopListener { stopTimer() }
            dialog.setOnAddTimeListener { seconds ->
                val endingCountdown = lastEndTime.plusSeconds(seconds.toLong())
                editTimer(endingCountdown)
            }

            dialog.show(supportFragmentManager, null)
        }

        if (!lastEndTime.isPast) {
            updateRunningTimer()

        } else {
            timer.setTextColor(defaultTimeColor)
            updateTimer()
        }

        // Logs:
        logListView = findViewById(R.id.log_list)
        logListView.isNestedScrollingEnabled = true

        logListHandler = LogListHandler(log, internationalSystem)
            .apply { fullyLoaded = log.size < LOG_PAGES_SIZE - 1 }

        logListView.init(log, logListHandler)

        logListHandler.setOnLoadMoreListener(this::loadMoreHistory)
        logListHandler.setOnBitClickListener(this::onClickBit)

        // Bottom button panel (main): Save bit log
        bottomButtonPanel.clickListen(mainButton) { saveBitLog(false) }

        // Bottom button panel (left): Instant button
        bottomButtonPanel.clickListen(confirmInstantButton) { saveBitLog(true) }
        if (initInstantSetButtonHidden) {
            confirmInstantButton.layoutParams.width = 0
        }

        mainButton.isEnabled = false
        confirmInstantButton.isEnabled = false
        Thread {
            Thread.sleep(200)
            runOnUiThread {
                mainButton.isEnabled = true
                confirmInstantButton.isEnabled = true
            }
        }.start()

        // Bottom button panel (swipe)
        bottomButtonPanel.setOnSwipeLeftListener { // forward
            if (goToNextVariation())
                true
            else if (enableSuperSetNavigation)
                goToNextSuperSetVariation()
            else false
        }
        bottomButtonPanel.setOnSwipeRightListener { // back
            goToNextVariation(true)
        }

        // Super set button
        superSet.setOnClickListener { toggleSuperSet() }
        superSetPanel.setOnClickListener { toggleSuperSet(false) }
        updateSuperSetIcon(true)


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
            lockView.setImageResource(
                if (locked) R.drawable.ic_lock_24dp
                else R.drawable.ic_unlock_24dp
            )
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

        val unitTextView = findViewById<TextView>(R.id.unit)
        unitTextView.setText(WeightUtils.unit(internationalSystem))

        weightModifier.setStep(variation.step)

        val warningColor = if ((variation.type === ExerciseType.BARBELL) == (variation.bar == null))
                resources.getColor(R.color.orange_light, theme)
            else
                resources.getColor(R.color.themedIcon, theme)

        weightSpecIcon.setColorFilter(warningColor)

        if (variation.weightSpec === WeightSpecification.TOTAL_WEIGHT) {
            weightSpecIcon.visibility = View.GONE
        } else {
            weightSpecIcon.setImageResource(variation.weightSpec.icon)
        }

        precalculateWeight()
    }

    override fun onBackPressed() {
        if (reloadOnBack) {
            Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(this)
            }

            val muscleId = exercise.primaryMuscles[0].id
            Intent(this, ExercisesActivity::class.java).apply {
                putExtra("muscleId", muscleId)
                putExtra("expandExerciseId", exercise.id)
                startActivity(this)
            }
            overridePendingTransition(R.anim.back_in, 0)
            finish()
        } else {
            super.onBackPressed()
        }
    }

    private fun toggleSuperSet(value: Boolean = Data.superSet == null) {
        requireActiveTraining(false) { trainingId ->
            if (value) {
                if (Data.superSet != null) return@requireActiveTraining

                dbThread { db ->
                    Data.superSet = (db.bitDao().getMaxSuperSet(trainingId) ?: 0) +1
                    runOnUiThread { updateSuperSetIcon() }
                }

                toggleInstantButton(false)

            } else if (Data.superSet != null) {

                Data.superSet = null
                updateSuperSetIcon()

                if (!leftButtonVisible) {
                    updateInstantButtonStatus()
                } else {
                    toggleInstantButton(false)
                }
            }
        }
    }

    private fun setHeaderInfo() {
        val fragment = findViewById<View>(R.id.fragmentExercise)
        val title = findViewById<TextView>(R.id.exerciseName)
        val subtitle = findViewById<TextView>(R.id.variationName)
        val image = findViewById<ExerciseImageView>(R.id.image)

        title.text = exercise.name

        if (variation.default) {
            subtitle.visibility = View.GONE
        } else {
            subtitle.text = variation.name
        }

        val variations = exercise.gymVariations
        if (variations.size <= 1) {
            fragment.isClickable = false

        } else {
            fragment.setOnClickListener {
                TextSelectDialogFragment(variations.map { it.name }) { pos, _ ->
                    if (pos != TextSelectDialogFragment.DIALOG_CLOSED) {
                        val variation = variations[pos]
                        if (variation != this.variation) {
                            switchToVariation(variation, left=variation.id > this.variation.id)
                        }
                    }
                }.apply { show(supportFragmentManager, null) }
            }
        }

        image.setImage(exercise.image, exercise.primaryMuscles[0].color)
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
                    logListView.notifyAllSizeChanged()
                    updateForms()
                }
                IntentReference.TRAINING -> {
                    dbThread { db ->
                        val log = if (variation.gymRelation == GymRelation.NO_RELATION)
                            db.bitDao().getHistory(variation.id, LOG_PAGES_SIZE)
                        else
                            db.bitDao().getHistory(Data.gym?.id ?: 0, variation.id, LOG_PAGES_SIZE)

                        this.log.clear()
                        val newBits = log.map { Bit(it) }
                            .also { this.log.addAll(it) }

                        runOnUiThread {
                            logListHandler.fullyLoaded = false
                            logListView.setListData(newBits)
                            logListView.notifyDataSetChanged()
                        }
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
                    logListView.notifyAllSizeChanged()

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

        if (variation.weightSpec === WeightSpecification.TOTAL_WEIGHT) {
            weightSpecIcon.visibility = View.GONE
        } else {
            weightSpecIcon.visibility = View.VISIBLE
            weightSpecIcon.setImageResource(variation.weightSpec.icon)
        }
    }

    private fun updateSuperSetIcon(noAnimate: Boolean = false) {
        if (Data.superSet == null) {
            superSet.setImageResource(R.drawable.ic_super_set_24dp)

            if (noAnimate) {
                superSetPanel.visibility = View.GONE
            } else {
                val anim = ResizeHeightAnimation(superSetPanel, 0, 250, true)
                superSetPanel.startAnimation(anim)
            }
        } else {
            superSet.setImageResource(R.drawable.ic_super_set_on_24dp)
            superSetPanel.text = String.format(getString(R.string.text_active_superset), Data.superSet)

            if (!noAnimate) {
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
                if (b.trainingId == Data.training?.id) {
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
                db.bitDao().getHistory(Data.gym?.id ?: 0, variation.id, bit.trainingId, date, LOG_PAGES_SIZE)

            val newBits = log.map { Bit(it) }
                .also { this.log.addAll(it) }

            if (log.size < LOG_PAGES_SIZE - 1) {
                logListHandler.fullyLoaded = true
            }

            runOnUiThread {
                logListView.add(newBits)
            }
        }
    }

    private fun goToNextSuperSetVariation(goBack: Boolean = false): Boolean {
        if (!enableSuperSetNavigation) return false
        Data.superSet ?: return false
        val trainingId = Data.training?.id ?: return false

        confirmInstantButton.isEnabled = false
        mainButton.isEnabled = false

        val db = getDbConnection()

        val variations = db.bitDao().getHistoryByTrainingId(trainingId)
            .filter { it.superSet == lastSuperSet }
            .map { it.variationId }
            .distinct()
            .let { if (goBack) it.reversed() else it }

        val idx = variations.indexOf(variation.id) + 1
        val nextVariationId = if (idx >= variations.size) variations[0]
            else variations[idx]
        val nextVariation = Data.getVariation(nextVariationId)
        switchToVariation(nextVariation, left=!goBack, reloadOnBack=true)

        return true
    }

    private fun goToNextVariation(goBack: Boolean = false): Boolean {
        val trainingId = Data.training?.id ?: return false

        val db = getDbConnection()

        val variations = db.bitDao().getHistoryByTrainingId(trainingId)
            .map { it.variationId }
            .reversed()
            .distinct()
            .let { if (goBack) it else it.reversed() }
            .ifEmpty { return false }

        val idx = variations.indexOf(variation.id) + 1
        if (idx >= variations.size || idx == 0 && !goBack) return false
        confirmInstantButton.isEnabled = false
        mainButton.isEnabled = false
        val nextVariation = Data.getVariation(variations[idx])
        switchToVariation(nextVariation, left=!goBack, reloadOnBack=true)

        return true
    }

    private fun switchToVariation(variation: Variation, left: Boolean = true, reloadOnBack: Boolean = false) {
        runOnUiThread {
            Intent(this, RegistryActivity::class.java).apply {
                putExtra("exerciseId", variation.exercise.id)
                putExtra("variationId", variation.id)
                if (reloadOnBack) {
                    putExtra("reloadOnBack", true)
                    startActivity(this)
                } else {
                    startActivityForResult(this, IntentReference.REGISTRY)
                }
            }
            finish()
            overridePendingSideTransition(left)
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
                bit.superSet = Data.superSet ?: 0

                exercise.lastTrained = NOW

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
                            logListView.insert(idx, bit)
                            logListView.scrollToPosition(0)
                            added = true
                            break
                        }
                    }

                    if (!added) {
                        log.add(bit)
                        logListView.add(bit)
                    }

                    if (!locked) {
                        notes.setText(R.string.symbol_empty)
                    }

                    toggleInstantButton(true)
                    if (Data.superSet == lastSuperSet) {
                        enableSuperSetNavigation = true
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
                Data.training
                    ?.also { db.trainingDao().deleteEmptyTrainingExcept(it.id) }
                    ?: db.trainingDao().deleteEmptyTraining()
            }

            runOnUiThread {
                logListView.removeAt(index)
                if (log.isNotEmpty()) {
                    if (index == 0) {
                        if (log[0].trainingId != trainingId) {
                            logListView.notifyItemChanged(0)
                        } else {
                            notifyTrainingIdChanged(trainingId, 0)
                        }
                    } else {
                        notifyTrainingIdChanged(trainingId, index)
                    }
                    if (trainingId == Data.training?.id && leftButtonVisible) {
                        updateInstantButtonStatus()
                    }
                } else {
                    toggleInstantButton(false)
                }
            }
        }
    }

    private fun updateInstantButtonStatus(databaseConnection: AppDatabase? = null) {
        val training = Data.training ?: return
        val currentSuperSet = Data.superSet ?: 0

        fun action(db: AppDatabase) {
            val shouldBeEnabled = db.bitDao().getMostRecentByTrainingId(training.id)
                ?.let { it.variationId == variation.id && it.superSet == currentSuperSet }
                ?: false

            toggleInstantButton(shouldBeEnabled)
        }

        databaseConnection?.also { action(it) }
            ?: dbThread { action(it) }
    }

    private val leftButtonVisible get() = confirmInstantButton.layoutParams.width > 0

    private fun toggleInstantButton(value: Boolean = !leftButtonVisible) {
        if (value) {
            if (leftButtonVisible) return
            confirmInstantButton.startResizeWidthAnimation(90, toDp = true, animLauncher = bottomButtonPanel)
        } else if (leftButtonVisible) {
            confirmInstantButton.startResizeWidthAnimation(0, animLauncher = bottomButtonPanel)
        }
    }

    private fun updateBitLog(bit: Bit, updateTrainingId: Boolean) {
        dbThread { db ->
            db.bitDao().update(bit.toEntity())
            val index = log.indexOf(bit)
            runOnUiThread {
                if (updateTrainingId)
                    logListView.notifyItemChanged(index)
                else
                    notifyTrainingIdChanged(bit.trainingId, index)
            }
        }
    }

    private fun notifyTrainingIdChanged(trainingId: Int, preIndex: Int) {
        var startIndex = 0
        var numberOfElements = 0

        var found = false
        for ((idx, bitLog) in log.withIndex()) {
            if (bitLog.trainingId == trainingId) {
                if (!found) {
                    startIndex = idx
                    found = true
                }
                numberOfElements++
            } else if (found) break
        }

        if (numberOfElements > 0) {
            if (preIndex > startIndex && preIndex < startIndex + numberOfElements)
                logListView.notifyItemRangeChanged(preIndex, numberOfElements + startIndex - preIndex)
            else
                logListView.notifyItemRangeChanged(startIndex, numberOfElements)
        }
    }


    private fun requireActiveTraining(createDialog: Boolean = true, block: (trainingId: Int) -> Unit) {
        Data.training
            ?.also { block(it.id) }
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
                                        training.start = NOW
                                        training.gymId = Data.gym?.id ?: 0
                                        training.trainingId = db.trainingDao().insert(training).toInt()
                                        Data.training = Training(training)
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

        val deletionPref = loadString(PreferencesDefinition.BITS_DELETION)
        val removedItems = if (deletionPref == "0") listOf(R.id.removeBit) else listOf()

        val dialog = MenuDialogFragment(R.menu.bit_menu, removedItems) { result: Int ->
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
                R.id.removeBit -> {
                    if (deletionPref == "prompt") {
                        val dialog = TextDialogFragment(
                            R.string.dialog_confirm_remove_log_title,
                            R.string.dialog_confirm_remove_log_text
                        ) { confirmed ->
                            if (confirmed) {
                                removeBitLog(bit)
                            }
                        }
                        dialog.show(supportFragmentManager, null)

                    } else if (deletionPref == "1") {
                        removeBitLog(bit)
                    }
                }
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
        if (lastEndTime < endDate) {
            startTimer(endDate, seconds)
        }
    }

    private fun startTimer(endDate: LocalDateTime, seconds: Int) {
        notificationService.startNewNotification(endDate, seconds, variation)
        updateRunningTimer()
    }

    private fun editTimer(endDate: LocalDateTime) {
        if (lastEndTime.isSet) {
            notificationService.editNotification(endDate)
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
