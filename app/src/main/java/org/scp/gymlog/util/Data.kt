package org.scp.gymlog.util

import org.scp.gymlog.exceptions.LoadException
import org.scp.gymlog.model.Bar
import org.scp.gymlog.model.Exercise
import org.scp.gymlog.model.Gym
import org.scp.gymlog.model.Muscle
import org.scp.gymlog.model.Note
import org.scp.gymlog.model.Training
import org.scp.gymlog.model.Variation

object Data {
	val STEPS_KG = intArrayOf(50, 100, 125, 200, 250, 500, 1000, 1500, 2000, 2500)

	val exercises =  mutableListOf<Exercise>()
	val muscles: List<Muscle> = Muscle.values().toList()
	val bars = mutableListOf<Bar>()
	val gyms =  mutableListOf<Gym>()
	val notes =  mutableListOf<Note>()
	var gym: Gym? = null
	var training: Training? = null
	var superSet: Int? = null

	fun getGym(gymId: Int): Gym =
		gyms
			.firstOrNull { it.id == gymId }
			?: throw LoadException("NO GYM FOUND id:$gymId")

	fun getBar(barId: Int): Bar =
		bars
			.firstOrNull { it.id == barId }
			?: throw LoadException("NO BAR FOUND id:$barId")

	fun getExercise(exerciseId: Int): Exercise =
		exercises
			.firstOrNull { it.id == exerciseId }
			?: throw LoadException("NO EXERCISE FOUND id:$exerciseId")

	fun getMuscle(muscleId: Int): Muscle =
		muscles
			.firstOrNull { it.id == muscleId }
			?: throw LoadException("NO MUSCLE FOUND id:$muscleId")

	fun getVariation(exercise: Exercise, variationId: Int): Variation =
		exercise.variations
			.firstOrNull { it.id == variationId }
			?: throw LoadException("NO VARIATION $variationId FOUND FOR EXERCISE: ${exercise.id}")

	fun getVariation(variationId: Int): Variation =
		exercises
			.flatMap { it.variations }
			.firstOrNull { it.id == variationId }
			?: throw LoadException("NO VARIATION $variationId FOUND")

	fun getNoteOrNull(content: String): Note? =
		notes
			.firstOrNull { it.content == content }

	fun getNoteOrCreate(content: String): Note = getNoteOrNull(content)
		?: Note(content = content)

	fun getNote(content: String): Note = getNoteOrNull(content)
		?: throw LoadException("NO NOTE \"$content\" FOUND")
}