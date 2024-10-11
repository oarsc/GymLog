package org.scp.gymlog.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.scp.gymlog.R
import org.scp.gymlog.room.EntityMappable
import org.scp.gymlog.room.entities.MuscleEntity

enum class Muscle(
	@StringRes val text: Int,
	@StringRes val textShort: Int,
	@DrawableRes val icon: Int,
	@ColorRes val color: Int
) : EntityMappable<MuscleEntity> {

	PECTORAL (
		text = R.string.group_pectoral,
		textShort = R.string.group_short_pectoral,
		icon = R.drawable.muscle_pectoral,
		color = R.color.pectoral
	),
	UPPER_BACK (
		text = R.string.group_upper_back,
		textShort = R.string.group_short_upper_back,
		icon = R.drawable.muscle_upper_back,
		color = R.color.upper_back
	),
	LOWER_BACK (
		text = R.string.group_lower_back,
		textShort = R.string.group_short_lower_back,
		icon = R.drawable.muscle_lower_back,
		color = R.color.lower_back
	),
	DELTOID (
		text = R.string.group_deltoid,
		textShort = R.string.group_short_deltoid,
		icon = R.drawable.muscle_deltoid,
		color = R.color.deltoid
	),
	TRAPEZIUS (
		text = R.string.group_trapezius,
		textShort = R.string.group_short_trapezius,
		icon = R.drawable.muscle_trapezius,
		color = R.color.trapezius
	),
	BICEPS (
		text = R.string.group_biceps,
		textShort = R.string.group_short_biceps,
		icon = R.drawable.muscle_biceps,
		color = R.color.biceps
	),
	TRICEPS (
		text = R.string.group_triceps,
		textShort = R.string.group_short_triceps,
		icon = R.drawable.muscle_triceps,
		color = R.color.triceps
	),
	FOREARM (
		text = R.string.group_forearm,
		textShort = R.string.group_short_forearm,
		icon = R.drawable.muscle_forearm,
		color = R.color.forearm
	),
	QUADRICEPS (
		text = R.string.group_quadriceps,
		textShort = R.string.group_short_quadriceps,
		icon = R.drawable.muscle_quadriceps,
		color = R.color.quadriceps
	),
	HAMSTRINGS (
		text = R.string.group_hamstrings,
		textShort = R.string.group_short_hamstrings,
		icon = R.drawable.muscle_hamstring,
		color = R.color.hamstrings
	),
	GLUTES (
		text = R.string.group_glutes,
		textShort = R.string.group_short_glutes,
		icon = R.drawable.muscle_glutes,
		color = R.color.glutes
	),
	CALVES (
		text = R.string.group_calves,
		textShort = R.string.group_short_calves,
		icon = R.drawable.muscle_calves,
		color = R.color.calves
	),
	ABDOMINALS (
		text = R.string.group_abdominals,
		textShort = R.string.group_short_abdominals,
		icon = R.drawable.muscle_abdominals,
		color = R.color.abdominals
	),
	CARDIO (
		text = R.string.group_cardio,
		textShort = R.string.group_short_cardio,
		icon = R.drawable.muscle_cardio,
		color = R.color.cardio
	);

	val id = ordinal + 1

	override fun toEntity(): MuscleEntity {
		val entity = MuscleEntity()
		entity.muscleId = id
		return entity
	}
}