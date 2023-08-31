package org.scp.gymlog.ui.preferences

import org.scp.gymlog.model.Order
import org.scp.gymlog.model.TrainingOrder

enum class PreferencesDefinition(
	val key: String,
	val defaultString: String? = null,
	val defaultInteger: Int? = null,
	val defaultBoolean: Boolean? = null
) {
	DEFAULT_REST_TIME("restTime", defaultString = "90"),

	EXERCISES_ORDER("exercisesSortLastUsed", defaultString = Order.ALPHABETICALLY.code),
	@Deprecated("Not in use")
	TRAINING_ORDER("trainingBitsSort", defaultString = TrainingOrder.CHRONOLOGICALLY.code),

	UNIT_CONVERSION_STEP("conversionStep", defaultString = "1"),
	UNIT_CONVERSION_EXACT_VALUE("conversionExactValue", defaultBoolean = false),
	UNIT_INTERNATIONAL_SYSTEM("internationalSystem", defaultBoolean = true),

	THEME("nightTheme", defaultBoolean = false),
	CURRENT_GYM("gym", defaultInteger = 1),

	BITS_DELETION("logDeletion", defaultString = "prompt"),
	EXERCISES_DELETION("exerciseDeletion", defaultString = "prompt"),
}