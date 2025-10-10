package org.oar.gymlog.ui.registry

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import org.oar.gymlog.R
import org.oar.gymlog.databinding.ActivityRegistryBinding
import org.oar.gymlog.model.Bit
import org.oar.gymlog.model.Exercise
import org.oar.gymlog.model.ExerciseType
import org.oar.gymlog.model.GymRelation
import org.oar.gymlog.model.Training
import org.oar.gymlog.model.Variation
import org.oar.gymlog.model.Weight
import org.oar.gymlog.model.WeightSpecification
import org.oar.gymlog.room.AppDatabase
import org.oar.gymlog.room.entities.TrainingEntity
import org.oar.gymlog.service.NotificationService
import org.oar.gymlog.ui.common.DatabaseAppCompatActivity
import org.oar.gymlog.ui.common.animations.ResizeHeightAnimation
import org.oar.gymlog.ui.common.dialogs.EditBitLogDialogFragment
import org.oar.gymlog.ui.common.dialogs.EditNotesDialogFragment
import org.oar.gymlog.ui.common.dialogs.EditNumberDialogFragment
import org.oar.gymlog.ui.common.dialogs.EditTimerDialogFragment
import org.oar.gymlog.ui.common.dialogs.EditWeightFormDialogFragment
import org.oar.gymlog.ui.common.dialogs.MenuDialogFragment
import org.oar.gymlog.ui.common.dialogs.TextDialogFragment
import org.oar.gymlog.ui.common.dialogs.TextSelectDialogFragment
import org.oar.gymlog.ui.common.dialogs.model.WeightFormData
import org.oar.gymlog.ui.exercises.ExercisesActivity
import org.oar.gymlog.ui.exercises.LatestActivity
import org.oar.gymlog.ui.main.MainActivity
import org.oar.gymlog.ui.main.preferences.PreferencesDefinition
import org.oar.gymlog.ui.top.TopActivity
import org.oar.gymlog.ui.training.TrainingActivity
import org.oar.gymlog.util.Constants
import org.oar.gymlog.util.Constants.IntentReference
import org.oar.gymlog.util.Data
import org.oar.gymlog.util.DateUtils.NOW
import org.oar.gymlog.util.DateUtils.diffSeconds
import org.oar.gymlog.util.DateUtils.isSystemTimePast
import org.oar.gymlog.util.FormatUtils.bigDecimal
import org.oar.gymlog.util.FormatUtils.integer
import org.oar.gymlog.util.FormatUtils.safeBigDecimal
import org.oar.gymlog.util.SecondTickThread
import org.oar.gymlog.util.WeightUtils
import org.oar.gymlog.util.WeightUtils.calculate
import org.oar.gymlog.util.WeightUtils.calculateTotal
import org.oar.gymlog.util.extensions.ComponentsExts.startActivityForResultWithSideTransaction
import org.oar.gymlog.util.extensions.ComponentsExts.startActivityWithSideTransaction
import org.oar.gymlog.util.extensions.ComponentsExts.startResizeWidthAnimation
import org.oar.gymlog.util.extensions.DatabaseExts.dbThread
import org.oar.gymlog.util.extensions.MessagingExts.snackbar
import org.oar.gymlog.util.extensions.MessagingExts.toast
import org.oar.gymlog.util.extensions.PreferencesExts.loadBoolean
import org.oar.gymlog.util.extensions.PreferencesExts.loadString
import java.math.BigDecimal

class RegistryActivity : DatabaseAppCompatActivity<ActivityRegistryBinding>(ActivityRegistryBinding::inflate) {

    companion object {
        private const val LOG_PAGES_SIZE = 20
    }

    private lateinit var exercise: Exercise
    private lateinit var variation: Variation
    private var defaultTimeColor = 0
    private var initInstantSetButtonHidden = true
    private var enableSuperSetNavigation = false
    private var lastSuperSet = 0
    private lateinit var logListHandler: LogListHandler
    private var internationalSystem = false
    private val log: MutableList<Bit> = ArrayList()
    private var locked = false
    private var sendRefreshList = false
    private val notificationService: NotificationService by lazy { NotificationService(this) }
    private var defaultTimer = 0
    private var countdownThread: CountdownThread? = null
    private var reloadOnBack = false

    override fun preLoad(savedInstanceState: Bundle?) {
        // Set initial size for hidden views
        binding.activeSuperset.layoutParams.height = 1
    }

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
        prepareExerciseListToRefreshWhenFinish()

        internationalSystem = loadBoolean(PreferencesDefinition.UNIT_INTERNATIONAL_SYSTEM)
        defaultTimer = loadString(PreferencesDefinition.DEFAULT_REST_TIME).toInt()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (reloadOnBack) {
                    val self = this@RegistryActivity

                    Intent(self, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        startActivity(this)
                    }

                    val muscleId = exercise.primaryMuscles[0].id
                    Intent(self, ExercisesActivity::class.java).apply {
                        putExtra("muscleId", muscleId)
                        putExtra("expandExerciseId", exercise.id)

                        ActivityOptions.makeCustomAnimation(self, R.anim.slide_in_left_back, R.anim.slide_out_right_back)
                            .toBundle()
                            .also { startActivity(this, it) }
                    }
                }
                finish()
            }
        })

        setHeaderInfo()

        // Toolbar
        binding.toolbar.apply {
            setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
            setOnMenuItemClickListener(::onOptionsItemSelected)
        }

        // Timer button:
        defaultTimeColor = binding.timerSeconds.textColors.defaultColor

        binding.timerButton.setOnClickListener {
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
                if (NotificationService.lastEndTime > 0)
                    editTimer(endDate)
                else
                    startTimer(endDate, seconds)
            }
            dialog.setOnStopListener { stopTimer() }
            dialog.setOnAddTimeListener { seconds ->
                val endingCountdown = NotificationService.lastEndTime + seconds * 1000L
                editTimer(endingCountdown)
            }

            dialog.show(supportFragmentManager, null)
        }

        if (!NotificationService.lastEndTime.isSystemTimePast) {
            updateRunningTimer()

        } else {
            binding.timerSeconds.setTextColor(defaultTimeColor)
            updateTimer()
        }

        // Logs:
        logListHandler = LogListHandler(this, log, internationalSystem).apply {
            fullyLoaded = log.size < LOG_PAGES_SIZE - 1
            setOnLoadMoreListener(this@RegistryActivity::loadMoreHistory)
            setOnBitClickListener(this@RegistryActivity::onClickBit)
        }

        binding.logList.apply {
            isNestedScrollingEnabled = true
            init(log, logListHandler)
        }


        // Bottom button panel (main): Save bit log
        binding.bottomButtonPanel.clickListen(binding.mainButton) { saveBitLog(false) }

        // Bottom button panel (left): Instant button
        binding.bottomButtonPanel.clickListen(binding.confirmInstantButton) { saveBitLog(true) }
        if (initInstantSetButtonHidden) {
            binding.confirmInstantButton.layoutParams.width = 0
            binding.confirmInstantButton.requestLayout()
        }

        binding.mainButton.isEnabled = false
        binding.confirmInstantButton.isEnabled = false
        Thread {
            Thread.sleep(200)
            runOnUiThread {
                binding.mainButton.isEnabled = true
                binding.confirmInstantButton.isEnabled = true
            }
        }.start()

        // Bottom button panel (swipe)
        binding.bottomButtonPanel.setOnSwipeLeftListener { buttonAnimate -> // forward
            dbThread { db ->
                buttonAnimate(goToNextVariation(db) || goToNextSuperSetVariation(db))
            }
        }
        binding.bottomButtonPanel.setOnSwipeRightListener { buttonAnimate -> // back
            dbThread { db ->
                buttonAnimate(goToNextVariation(db, true))
            }
        }

        // Super set button
        binding.superSet.setOnClickListener { toggleSuperSet() }
        binding.activeSuperset.setOnClickListener { toggleSuperSet(false) }
        updateSuperSetIcon(true)

        // Notes
        binding.editNotes.setOnClickListener {
            val dialog = EditNotesDialogFragment(R.string.text_notes, variation, binding.editNotes.text.toString())
                { result: String -> binding.editNotes.setText(result) }
            dialog.show(supportFragmentManager, null)
        }

        binding.clearNote.setOnClickListener {
            binding.editNotes.text.clear()
        }

        binding.lock.setOnClickListener {
            locked = !locked
            binding.lock.setImageResource(
                if (locked) R.drawable.ic_lock_24dp
                else R.drawable.ic_unlock_24dp
            )
        }

        // Weight and Reps Input fields:
        binding.editWeight.filters = arrayOf(InputFilter { source: CharSequence, _, _, dest: Spanned, _, _ ->
            val input = (dest.toString() + source.toString()).safeBigDecimal()
            if (input < Constants.ONE_THOUSAND && input.scale() < 3)
                null
            else
                ""
        })
        binding.editWeight.setOnClickListener { showWeightDialog(binding.editWeight) }

        binding.editReps.setOnClickListener {
            val dialog = EditNumberDialogFragment(R.string.text_reps, binding.editReps.text.toString(),
                { result: BigDecimal -> binding.editReps.bigDecimal = result })
            dialog.show(supportFragmentManager, null)
        }

        binding.unit.setText(WeightUtils.unit(internationalSystem))

        binding.weightModifier.setStep(variation.step)

        val warningColor = if ((variation.type === ExerciseType.BARBELL) == (variation.bar == null))
                resources.getColor(R.color.orange_light, theme)
            else
                resources.getColor(R.color.themedIcon, theme)

        binding.weightSpecIcon.setColorFilter(warningColor)

        if (variation.weightSpec === WeightSpecification.TOTAL_WEIGHT) {
            binding.weightSpecIcon.visibility = View.GONE
        } else {
            binding.weightSpecIcon.setImageResource(variation.weightSpec.icon)
        }

        precalculateWeight()
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
        binding.fragmentExercise.apply {
            exerciseName.text = exercise.name

            if (variation.default) {
                variationName.visibility = View.GONE
            } else {
                variationName.text = variation.name
            }

            val variations = exercise.gymVariations
            if (variations.size <= 1) {
                root.isClickable = false

            } else {
                root.setOnClickListener {
                    TextSelectDialogFragment(variations.map { it.name }) { pos, _ ->
                        if (pos != TextSelectDialogFragment.DIALOG_CLOSED) {
                            val variation = variations[pos]
                            if (variation != this@RegistryActivity.variation) {
                                switchToVariation(variation, left=variation.id > this@RegistryActivity.variation.id)
                            }
                        }
                    }.apply { show(supportFragmentManager, null) }
                }
            }

            image.setImage(exercise.image, exercise.primaryMuscles[0].color)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.latestButton -> {
                val trainingId = Data.training?.id
                    ?: run {
                        toast(R.string.validation_training_not_started)
                        return true
                    }
                dbThread { db ->
                    val variationIds = db.bitDao().getHistoryByTrainingIdDesc(trainingId)
                        .map { it.variationId }
                        .distinct()
                    when {
                        variationIds.isEmpty() ->
                            toast(R.string.validation_no_exercise_registered)

                        variationIds[0] == variation.id ->
                            Intent(this, LatestActivity::class.java)
                                .also { startActivity(it) }

                        else ->
//                            goToVariation(Data.getVariation(variationIds[0]))
                            switchToVariation(
                                variation = Data.getVariation(variationIds[0]),
                                left = variationIds.contains(variation.id),
                                true
                            )
                    }
                }
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
                    binding.logList.notifyAllSizeChanged()
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
                            binding.logList.setListData(newBits)
                            binding.logList.notifyDataSetChanged()
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
                    binding.logList.notifyAllSizeChanged()

                    updateForms()
                    dbThread { db -> db.variationDao().update(variation.toEntity()) }
                }
            }
        )
        dialog.show(supportFragmentManager, null)
    }

    private fun updateForms() {
        binding.weightModifier.setStep(variation.step)

        val warningColor = if ((variation.type === ExerciseType.BARBELL) == (variation.bar == null))
                resources.getColor(R.color.orange_light, theme)
            else
                resources.getColor(R.color.themedIcon, theme)

        binding.weightSpecIcon.setColorFilter(warningColor)

        if (variation.weightSpec === WeightSpecification.TOTAL_WEIGHT) {
            binding.weightSpecIcon.visibility = View.GONE
        } else {
            binding.weightSpecIcon.visibility = View.VISIBLE
            binding.weightSpecIcon.setImageResource(variation.weightSpec.icon)
        }
    }

    private fun updateSuperSetIcon(noAnimate: Boolean = false) {
        if (Data.superSet == null) {
            binding.superSet.setImageResource(R.drawable.ic_super_set_24dp)

            if (noAnimate) {
                binding.activeSuperset.visibility = View.GONE
            } else {
                val anim = ResizeHeightAnimation(binding.activeSuperset, 0, 250, true)
                binding.activeSuperset.startAnimation(anim)
            }
        } else {
            binding.superSet.setImageResource(R.drawable.ic_super_set_on_24dp)
            binding.activeSuperset.text = String.format(getString(R.string.text_active_superset), Data.superSet)

            val anim = if (!noAnimate) {
                ResizeHeightAnimation(binding.activeSuperset, 48, 250, true)
            } else {
                ResizeHeightAnimation(binding.activeSuperset, 48, 0, true)
            }
            binding.activeSuperset.startAnimation(anim)
        }
    }

    private fun updateTimer(value: Int = variation.restTime) {
        if (NotificationService.lastEndTime.isSystemTimePast) {
            binding.timerSeconds.text = if (value < 0)
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

            binding.editReps.integer = bit.reps

            val partialWeight = bit.weight.calculate(
                variation.weightSpec,
                variation.bar)

            binding.editWeight.bigDecimal = partialWeight.getValue(internationalSystem)
        } else {
            binding.editReps.setText("10")
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
                binding.logList.add(newBits)
            }
        }
    }

    private fun goToNextSuperSetVariation(db: AppDatabase, goBack: Boolean = false): Boolean {
        if (!enableSuperSetNavigation) return false
        Data.superSet ?: return false
        val trainingId = Data.training?.id ?: return false

        val variations = db.bitDao().getHistoryByTrainingId(trainingId)
            .filter { it.superSet == lastSuperSet }
            .map { it.variationId }
            .distinct()
            .let { if (goBack) it.reversed() else it }

        val idx = variations.indexOf(variation.id) + 1
        val nextVariationId =
            if (idx >= variations.size) variations[0]
            else variations[idx]

        runOnUiThread {
            binding.confirmInstantButton.isEnabled = false
            binding.mainButton.isEnabled = false
        }
        val nextVariation = Data.getVariation(nextVariationId)
        switchToVariation(nextVariation, left=!goBack, reloadOnBack=true)
        return true
    }

    private fun goToNextVariation(db:AppDatabase, goBack: Boolean = false): Boolean {
        val trainingId = Data.training?.id ?: return false

        val variations = db.bitDao().getHistoryByTrainingId(trainingId)
            .map { it.variationId }
            .reversed()
            .distinct()
            .let { if (goBack) it else it.reversed() }
            .ifEmpty { return false }

        val idx = variations.indexOf(variation.id) + 1
        if (idx >= variations.size || idx == 0 && !goBack) return false

        runOnUiThread {
            binding.confirmInstantButton.isEnabled = false
            binding.mainButton.isEnabled = false
        }
        val nextVariation = Data.getVariation(variations[idx])
        switchToVariation(nextVariation, left = !goBack, reloadOnBack = true)
        return true
    }

    private fun switchToVariation(variation: Variation, left: Boolean = true, reloadOnBack: Boolean = false) {
        runOnUiThread {
            Intent(this, RegistryActivity::class.java).apply {
                putExtra("exerciseId", variation.exercise.id)
                putExtra("variationId", variation.id)
                if (reloadOnBack) {
                    putExtra("reloadOnBack", true)
                    startActivityWithSideTransaction(this, left)
                } else {
                    startActivityForResultWithSideTransaction(this, IntentReference.REGISTRY, left)
                }
            }
            finish()
        }
    }

    private fun saveBitLog(instant: Boolean) {
        requireActiveTraining { trainingId ->

            dbThread { db ->
                val bit = Bit(variation)

                val totalWeight = Weight(binding.editWeight.bigDecimal, internationalSystem).calculateTotal(
                    variation.weightSpec,
                    variation.bar)

                bit.weight = totalWeight
                bit.note = binding.editNotes.text.toString()
                bit.reps = binding.editReps.integer
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
                            binding.logList.insert(idx, bit)
                            binding.logList.scrollToPosition(0)
                            added = true
                            break
                        }
                    }

                    if (!added) {
                        log.add(bit)
                        binding.logList.add(bit)
                    }

                    if (!locked) {
                        binding.editNotes.setText(R.string.symbol_empty)
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
                binding.logList.removeAt(index)
                if (log.isNotEmpty()) {
                    if (index == 0) {
                        if (log[0].trainingId != trainingId) {
                            binding.logList.notifyItemChanged(0)
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

    private val leftButtonVisible get() = binding.confirmInstantButton.layoutParams.width > 0

    private fun toggleInstantButton(value: Boolean = !leftButtonVisible) {
        if (value) {
            if (leftButtonVisible) return
            binding.confirmInstantButton.startResizeWidthAnimation(90, toDp = true, animLauncher = binding.bottomButtonPanel)
        } else if (leftButtonVisible) {
            binding.confirmInstantButton.startResizeWidthAnimation(0, animLauncher = binding.bottomButtonPanel)
        }
    }

    private fun updateBitLog(bit: Bit, updateTrainingId: Boolean) {
        val index = log.indexOf(bit)
        if (updateTrainingId) {
            binding.logList.notifyItemChanged(index)
        } else {
            notifyTrainingIdChanged(bit.trainingId, index)
        }
    }

    private fun cloneBitLog(originalBit: Bit, bit: Bit) {
        val index = log.indexOf(originalBit)
        log.add(index + 1, bit)
        binding.logList.insert(index + 1, bit)
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
                binding.logList.notifyItemRangeChanged(preIndex, numberOfElements + startIndex - preIndex)
            else
                binding.logList.notifyItemRangeChanged(startIndex, numberOfElements)
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
                            title = R.string.title_registry,
                            enableInstantSwitch = enableInstantSwitch,
                            internationalSystem = internationalSystem,
                            initialValue = bit,
                            confirmListener = { it, cloned ->
                                runOnUiThread {
                                    if (cloned) {
                                        cloneBitLog(bit, it)
                                    } else {
                                        updateBitLog(it, initialInstant == it.instant)
                                    }
                                }
                            })

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
        val endDate = System.currentTimeMillis() + seconds * 1000L
        if (NotificationService.lastEndTime < endDate) {
            startTimer(endDate, seconds)
        }
    }

    private fun startTimer(endDate: Long, seconds: Int) {
        notificationService.startNewNotification(endDate, seconds, variation)
        updateRunningTimer()
    }

    private fun editTimer(endDate: Long) {
        if (NotificationService.lastEndTime > 0) {
            notificationService.editNotification(endDate)
            updateRunningTimer()
        }
    }

    private fun updateRunningTimer() {
        countdownThread?.onTick() ?: run {
            countdownThread = CountdownThread().also(Thread::start)

            val color = resources.getColor(R.color.orange_light, theme)
            binding.timerSeconds.setTextColor(color)
            binding.secondsText.setTextColor(color)
        }
    }

    private fun stopTimer() {
        notificationService.hideNotification()
        countdownThread?.interrupt()
    }

    private inner class CountdownThread : SecondTickThread() {

        override fun onTick(): Boolean {
            if (NotificationService.lastEndTime.isSystemTimePast)
                return false

            val seconds = NotificationService.lastEndTime.diffSeconds()
            runOnUiThread { binding.timerSeconds.text = seconds.toString() }
            return true
        }

        override fun onFinish() {
            countdownThread = null
            runOnUiThread {
                updateTimer()
                binding.timerSeconds.setTextColor(defaultTimeColor)
                binding.secondsText.setTextColor(defaultTimeColor)
            }
        }
    }
}
