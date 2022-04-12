package org.scp.gymlog.util

import org.scp.gymlog.R
import org.scp.gymlog.exceptions.LoadException
import org.scp.gymlog.model.Bar
import org.scp.gymlog.model.Exercise
import org.scp.gymlog.model.Muscle
import org.scp.gymlog.model.Variation

object Data {
	val STEPS_KG = intArrayOf(50, 100, 125, 200, 250, 500, 1000, 1500, 2000, 2500)

	val exercises: MutableList<Exercise> = ArrayList()
	val muscles: MutableList<Muscle> = ArrayList()
	val bars: MutableList<Bar> = ArrayList()
	var trainingId = -1

	init {
		var muscleId = 0
		muscles.addAll(
			listOf(
				Muscle(++muscleId, R.string.group_pectoral, R.drawable.muscle_pectoral, R.color.pectoral),
				Muscle(++muscleId, R.string.group_upper_back, R.drawable.muscle_upper_back, R.color.upper_back),
				Muscle(++muscleId, R.string.group_lower_back, R.drawable.muscle_lower_back, R.color.lower_back),
				Muscle(++muscleId, R.string.group_deltoid, R.drawable.muscle_deltoid, R.color.deltoid),
				Muscle(++muscleId, R.string.group_trapezius, R.drawable.muscle_trapezius, R.color.trapezius),
				Muscle(++muscleId, R.string.group_biceps, R.drawable.muscle_biceps, R.color.biceps),
				Muscle(++muscleId, R.string.group_triceps, R.drawable.muscle_triceps, R.color.triceps),
				Muscle(++muscleId, R.string.group_forearm, R.drawable.muscle_forearm, R.color.forearm),
				Muscle(++muscleId, R.string.group_quadriceps, R.drawable.muscle_quadriceps, R.color.quadriceps),
				Muscle(++muscleId, R.string.group_hamstrings, R.drawable.muscle_hamstring, R.color.hamstrings),
				Muscle(++muscleId, R.string.group_calves, R.drawable.muscle_calves, R.color.calves),
				Muscle(++muscleId, R.string.group_glutes, R.drawable.muscle_glutes, R.color.glutes),
				Muscle(++muscleId, R.string.group_abdominals, R.drawable.muscle_abdominals, R.color.abdominals),
				Muscle(++muscleId, R.string.group_cardio, R.drawable.muscle_cardio, R.color.cardio),
			)
		)
	}

	fun getBar(barId: Int): Bar {
		return bars
			.filter { bar: Bar -> bar.id == barId }
			.getOrElse(0) { throw LoadException("NO BAR FOUND id:$barId") }
	}

	fun getExercise(exerciseId: Int): Exercise {
		return exercises
			.filter { exercise: Exercise -> exercise.id == exerciseId }
			.getOrElse(0) { throw LoadException("NO EXERCISE FOUND id:$exerciseId") }
	}

	fun getMuscle(muscleId: Int): Muscle {
		return muscles
			.filter { muscle: Muscle -> muscle.id == muscleId }
			.getOrElse(0) { throw LoadException("NO MUSCLE FOUND id:$muscleId") }
	}

	fun getVariation(exercise: Exercise, variationId: Int): Variation {
		return exercise.variations
			.filter { variation: Variation -> variation.id == variationId }
			.getOrElse(0) { throw
				LoadException("NO VARIATION $variationId FOUND FOR EXERCISE: ${exercise.id}") }
	}
}