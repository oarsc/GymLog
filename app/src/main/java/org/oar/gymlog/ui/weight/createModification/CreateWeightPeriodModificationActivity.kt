package org.oar.gymlog.ui.weight.createModification

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
import org.oar.gymlog.util.FormatUtils.safeInt
import org.oar.gymlog.util.extensions.ComponentsExts.mustRefreshParent
import org.oar.gymlog.util.extensions.DatabaseExts.dbThread
import org.oar.gymlog.util.extensions.MessagingExts.snackbar
import java.time.LocalDate
import java.time.format.DateTimeParseException
import kotlin.reflect.KMutableProperty0

class CreateWeightPeriodModificationActivity : DatabaseAppCompatActivity<ActivityCreateBinding>(ActivityCreateBinding::inflate) {
	private lateinit var weightPeriodModification: WeightPeriodModification
	private val caller: IntentReference by lazy { getIntentCall() }

	override fun onLoad(savedInstanceState: Bundle?, db: AppDatabase): Int {
		val weightPeriodId = intent.extras!!.getInt("weightPeriodId")
		val weightPeriod = db.weightDao().getPeriod(weightPeriodId).let(::WeightPeriod)

		weightPeriodModification = if (caller === IntentReference.EDIT_WEIGHT_PERIOD_MODIFICATION_DETAILS) {
			val weightPeriodModificationId = intent.extras!!.getInt("weightPeriodModificationId")
			WeightPeriodModification(
                entity = db.weightDao().getModification(weightPeriodModificationId),
                weightPeriod = weightPeriod
            )
		} else {
			val initialDate = if (LocalDate.now() >= weightPeriod.endDate) weightPeriod.initialDate else maxOf(weightPeriod.initialDate, LocalDate.now())
			WeightPeriodModification(
				initialDate = initialDate,
				endDate = minOf(weightPeriod.endDate, initialDate.plusDays(7)),
				gramsPerWeek = 0,
				weightPeriod = weightPeriod
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
			property = weightPeriodModification::initialDate
		).also(::add)

		createDayFormElement(
			title = R.string.form_end_date,
			property = weightPeriodModification::endDate
		).also(::add)

		createGramFormElement(
			title = R.string.form_gain_per_week,
			property = weightPeriodModification::gramsPerWeek
		).also(::add)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		if (item.itemId != R.id.confirmButton) {
			return false
		}

		when {
            weightPeriodModification.initialDate < MIN_DATE -> snackbar(R.string.validation_too_old_date)
            weightPeriodModification.endDate < MIN_DATE -> snackbar(R.string.validation_too_old_date)
            weightPeriodModification.initialDate >= weightPeriodModification.endDate -> snackbar(R.string.validation_dates_incorrect)
            else -> {
                dbThread { db ->
                    val anyOverlap = db.weightDao().getPeriodWithModifications(weightPeriodModification.weightPeriod.id)
						.modifications
                        .filter { it.weightPeriodModificationId != weightPeriodModification.id }
                        .any { weightPeriodModification.initialDate < it.end && it.start < weightPeriodModification.endDate }

                    if (anyOverlap) {
                        snackbar(R.string.validation_dates_overlap)
                        return@dbThread
                    }

                    if (caller === IntentReference.EDIT_WEIGHT_PERIOD_MODIFICATION_DETAILS) {
                        db.weightDao().updateModification(weightPeriodModification.toEntity())
                    } else {
                        val id = db.weightDao().insertModification(weightPeriodModification.toEntity())
                        weightPeriodModification.id = id.toInt()
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
					inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED,
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

	private fun Int.toGramString() = toString() + "g"

	companion object {
		private val MIN_DATE = LocalDate.of(2015, 1, 1)
	}
}