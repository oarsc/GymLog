package org.oar.gymlog.util.extensions.model

import org.oar.gymlog.exceptions.InternalException
import org.oar.gymlog.model.Exercise
import org.oar.gymlog.model.GymRelation
import org.oar.gymlog.model.Muscle
import org.oar.gymlog.model.Variation
import org.oar.gymlog.util.Data
import java.time.LocalDateTime

object ExerciseExts {
    val Exercise.gymVariations: List<Variation>
        get() = variations.filter {
            it.gymRelation != GymRelation.STRICT_RELATION || it.gymId == Data.gym?.id
        }

    val Exercise.defaultVariation: Variation
        get() = variations.find { it.default }
            ?: throw InternalException("Default variation not found for: $id")

    fun Exercise.copy(
        id: Int = this.id,
        name: String = this.name,
        image: String = this.image,
        lastTrained: LocalDateTime = this.lastTrained,
        primaryMuscles: MutableList<Muscle> = this.primaryMuscles,
        secondaryMuscles: MutableList<Muscle> = this.secondaryMuscles,
        variations: MutableList<Variation> = this.variations
    ) = Exercise(
        id = id,
        name = name,
        image = image,
        lastTrained = lastTrained,
        primaryMuscles = primaryMuscles,
        secondaryMuscles = secondaryMuscles,
        variations = variations
    )
}