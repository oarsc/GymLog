package org.oar.gymlog.ui.main.preferences

import org.oar.gymlog.model.Order

enum class PreferencesDefinition(
	val key: String,
	val defaultString: String? = null,
	val defaultInteger: Int? = null,
	val defaultBoolean: Boolean? = null
) {
	DEFAULT_REST_TIME("restTime", defaultString = "90"),

	EXERCISES_ORDER("exercisesSortLastUsed", defaultString = Order.ALPHABETICALLY.code),

	UNIT_CONVERSION_STEP("conversionStep", defaultString = "1"),
	UNIT_CONVERSION_EXACT_VALUE("conversionExactValue", defaultBoolean = false),
	UNIT_INTERNATIONAL_SYSTEM("internationalSystem", defaultBoolean = true),

	THEME("nightTheme", defaultBoolean = false),
	CURRENT_GYM("gym", defaultInteger = 0),

	BITS_DELETION("logDeletion", defaultString = "prompt"),
	EXERCISES_DELETION("exerciseDeletion", defaultString = "prompt"),

	DROPBOX_CREDENTIAL("dbCredential"),
}