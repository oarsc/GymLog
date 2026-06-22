package org.oar.gymlog.ui.weight.create

import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import org.oar.gymlog.R
import org.oar.gymlog.databinding.ActivityCreateBinding
import org.oar.gymlog.model.WeightPeriod
import org.oar.gymlog.model.WeightPeriodModification
import org.oar.gymlog.room.AppDatabase
import org.oar.gymlog.ui.common.DatabaseAppCompatActivity
import org.oar.gymlog.ui.common.createForm.CreateFormElement
import org.oar.gymlog.ui.common.createForm.CreateListHandler
import org.oar.gymlog.ui.common.dialogs.EditTextDialogFragment
import org.oar.gymlog.util.Constants.IntentReference
import org.oar.gymlog.util.DateUtils.getDateString
import org.oar.gymlog.util.FormatUtils.safeBigDecimal
import org.oar.gymlog.util.FormatUtils.safeInt
import org.oar.gymlog.util.extensions.ComponentsExts.mustRefreshParent
import org.oar.gymlog.util.extensions.DatabaseExts.dbThread
import org.oar.gymlog.util.extensions.MessagingExts.snackbar
import java.math.BigDecimal
import java.math.BigDecimal.ONE
import java.math.BigDecimal.ZERO
import java.math.RoundingMode.HALF_UP
import java.time.LocalDate
import java.time.format.DateTimeParseException
import kotlin.reflect.KMutableProperty0

class CreateWeightPeriodActivity : DatabaseAppCompatActivity<ActivityCreateBinding>(ActivityCreateBinding::inflate) {
	private lateinit var weightPeriod: WeightPeriod
	private val caller: IntentReference by lazy { getIntentCall() }

	override fun onLoad(savedInstanceState: Bundle?, db: AppDatabase): Int {
		weightPeriod = if (caller === IntentReference.EDIT_WEIGHT_PERIOD_DETAILS) {
			val entities = intent.extras!!.getInt("weightPeriodId").let(db.weightDao()::getPeriodWithModifications)
			WeightPeriod(entities.weightPeriod!!).apply {
				entities.modifications.forEach {
					modifications.add(WeightPeriodModification(it, this))
				}
			}
		} else {
			WeightPeriod(
				initialDate = LocalDate.now(),
				endDate = LocalDate.now().plusYears(1),
				initialWeight = ZERO,
				gainGramsPerWeek = 100,
				lossGramsPerWeek = 100,
				expectedMuscleGain = ONE,
				toleranceGrams = 1000
			)
		}
		return CONTINUE
	}

	override fun onDelayedCreate(savedInstanceState: Bundle?) {
		binding.toolbar.apply {
			setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
			setTitle(R.string.title_create_weight_period)
			setOnMenuItemClickListener(::onOptionsItemSelected)
		}

		binding.createFormList.init(createForm(), CreateListHandler)
	}

	private fun createForm(): List<CreateFormElement> = buildList {
		createDayFormElement(
			title = R.string.form_start_date,
			property = weightPeriod::initialDate
		).also(::add)

		createDayFormElement(
			title = R.string.form_end_date,
			property = weightPeriod::endDate
		).also(::add)

		createKgFormElement(
			title = R.string.form_start_weight,
			property = weightPeriod::initialWeight
		).also(::add)

		createGramFormElement(
			title = R.string.form_gain_per_week,
			property = weightPeriod::gainGramsPerWeek
		).also(::add)

		createGramFormElement(
			title = R.string.form_loss_per_week,
			property = weightPeriod::lossGramsPerWeek
		).also(::add)

		createKgFormElement(
			title = R.string.form_expected_muscle_gain,
			property = weightPeriod::expectedMuscleGain
		).also(::add)

		createGramFormElement(
			title = R.string.form_tolerance,
			property = weightPeriod::toleranceGrams
		).also(::add)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		if (item.itemId != R.id.confirmButton) {
			return false
		}

		when {
			weightPeriod.initialDate < MIN_DATE -> snackbar(R.string.validation_too_old_date)
			weightPeriod.endDate < MIN_DATE -> snackbar(R.string.validation_too_old_date)
            weightPeriod.initialWeight <= ZERO -> snackbar(R.string.validation_initial_weight_zero)
            weightPeriod.gainGramsPerWeek <= 0 -> snackbar(R.string.validation_gains_per_week_zero)
            weightPeriod.lossGramsPerWeek <= 0 -> snackbar(R.string.validation_loss_per_week_zero)
            weightPeriod.expectedMuscleGain <= ZERO -> snackbar(R.string.validation_expected_muscle_gain_zero)
            weightPeriod.initialDate >= weightPeriod.endDate -> snackbar(R.string.validation_dates_incorrect)
            else -> {
                dbThread { db ->
                    val anyOverlap = db.weightDao().getAllPeriods()
                        .filter { it.weightPeriodId != weightPeriod.id }
                        .any { weightPeriod.initialDate < it.end && it.start < weightPeriod.endDate }

                    if (anyOverlap) {
                        snackbar(R.string.validation_dates_overlap)
                        return@dbThread
                    }

                    if (caller === IntentReference.EDIT_WEIGHT_PERIOD_DETAILS) {
                        db.weightDao().updatePeriod(weightPeriod.toEntity())
                    } else {
                        val id = db.weightDao().insertPeriod(weightPeriod.toEntity())
                        weightPeriod.id = id.toInt()
                    }

                    runOnUiThread {
						mustRefreshParent()
                        finish()
                    }
                }
            }
        }
		return true
	}

	private fun createDayFormElement(title: Int, property: KMutableProperty0<LocalDate>): CreateFormElement {
		lateinit var form: CreateFormElement
		form = CreateFormElement(
			title = title,
			valueStr = property.get().getDateString(),
			onClickListener = {
				fun showDialog() {
					EditTextDialogFragment(
						title = title,
						initialValue = property.get().getDateString(),
						confirm = { result ->
							try {
								val date = LocalDate.parse(result)
								property.set(date)
								form.valueStr = date.getDateString()
								form.update()
							} catch (_: DateTimeParseException) {
								showDialog()
							}
						}
					).show(supportFragmentManager, null)
				}
				showDialog()
			}
		)
		return form
	}

	private fun createGramFormElement(title: Int, property: KMutableProperty0<Int>): CreateFormElement {
		lateinit var form: CreateFormElement
		form = CreateFormElement(
			title = title,
			valueStr = property.get().toGramString(),
			onClickListener = {
				EditTextDialogFragment(
					title = title,
					initialValue = property.get().toString(),
					inputType = InputType.TYPE_CLASS_NUMBER,
					confirm = { result ->
						val value = result.safeInt()
						property.set(value)
						form.valueStr = value.toGramString()
						form.update()
					}
				).show(supportFragmentManager, null)
			}
		)
		return form
	}

	private fun createKgFormElement(title: Int, property: KMutableProperty0<BigDecimal>): CreateFormElement {
		lateinit var form: CreateFormElement
		form = CreateFormElement(
            title = title,
            valueStr = property.get().toKgString(),
            onClickListener = {
				EditTextDialogFragment(
					title = title,
					initialValue = property.get().setScale(2, HALF_UP).toPlainString(),
					inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL,
					confirm = { result ->
						val value = result.safeBigDecimal().setScale(2, HALF_UP)
						property.set(value)
						form.valueStr = value.toKgString()
						form.update()
					}
				).show(supportFragmentManager, null)
			}
        )
		return form
    }

	private fun BigDecimal.toKgString() = setScale(2, HALF_UP).toPlainString() + "kg"
	private fun Int.toGramString() = toString() + "g"

	companion object {
		private val MIN_DATE = LocalDate.of(2015, 1, 1)
	}
}