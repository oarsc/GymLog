package org.oar.gymlog.util.extensions.model

import org.oar.gymlog.model.Bar
import org.oar.gymlog.model.Exercise
import org.oar.gymlog.model.ExerciseType
import org.oar.gymlog.model.GymRelation
import org.oar.gymlog.model.Variation
import org.oar.gymlog.model.WeightSpecification
import java.math.BigDecimal

object VariationExts {
    fun Variation.copy(
        name: String = this.name,
        id: Int = this.id,
        default: Boolean = this.default,
        type: ExerciseType = this.type,
        gymId: Int? = this.gymId,
        gymRelation: GymRelation = this.gymRelation,
        step: BigDecimal = this.step,
        bar: Bar? = this.bar,
        weightSpec: WeightSpecification = this.weightSpec,
        restTime: Int = this.restTime,
        exercise: Exercise = this.exercise
    ) = Variation(
        name = name,
        id = id,
        default = default,
        type = type,
        gymId = gymId,
        gymRelation = gymRelation,
        step = step,
        bar = bar,
        weightSpec = weightSpec,
        restTime = restTime,
        exercise = exercise
    )
}