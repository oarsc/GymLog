package org.oar.gymlog.util

import org.oar.gymlog.exceptions.LoadException
import org.oar.gymlog.model.Bar
import org.oar.gymlog.model.Exercise
import org.oar.gymlog.model.Gym
import org.oar.gymlog.model.Muscle
import org.oar.gymlog.model.Training
import org.oar.gymlog.model.Variation
import org.oar.gymlog.model.Weight
import org.oar.gymlog.model.WeightPeriod
import org.oar.gymlog.model.Workout
import java.time.LocalDate

object Data {
	val STEPS_KG = intArrayOf(50, 100, 125, 200, 250, 500, 1000, 1500, 2000, 2500)

	val exercises =  mutableListOf<Exercise>()
	val workouts =  mutableListOf<Workout>()
	val muscles: List<Muscle> = Muscle.entries
	val bars = mutableListOf<Bar>()
	val gyms =  mutableListOf<Gym>()
	val weights = mutableMapOf<LocalDate, Weight>()
	var gym: Gym? = null
	var training: Training? = null
	var superSet: Int? = null
	var weightPeriod: WeightPeriod? = null

	fun getGym(gymId: Int): Gym = gyms
		.filter { it.id == gymId }
		.getOrElse(0) { throw LoadException("NO GYM FOUND id:$gymId") }

	fun getBar(barId: Int): Bar = bars
		.filter { it.id == barId }
		.getOrElse(0) { throw LoadException("NO BAR FOUND id:$barId") }

	fun getExercise(exerciseId: Int): Exercise = exercises
		.filter { it.id == exerciseId }
		.getOrElse(0) { throw LoadException("NO EXERCISE FOUND id:$exerciseId") }

	fun getMuscle(muscleId: Int): Muscle = muscles
		.filter { it.id == muscleId }
		.getOrElse(0) { throw LoadException("NO MUSCLE FOUND id:$muscleId") }

	fun getVariation(exercise: Exercise, variationId: Int): Variation = exercise.variations
		.filter { it.id == variationId }
		.getOrElse(0) { throw LoadException("NO VARIATION $variationId FOUND FOR EXERCISE: ${exercise.id}") }

	fun getVariation(variationId: Int): Variation = exercises
		.flatMap { it.variations }
		.filter { it.id == variationId }
		.getOrElse(0) { throw LoadException("NO VARIATION $variationId FOUND") }

	fun getWorkout(workoutId: Int): Workout = workouts
		.filter { it.id == workoutId }
		.getOrElse(0) { throw LoadException("NO WORKOUT FOUND id:$workoutId") }
}