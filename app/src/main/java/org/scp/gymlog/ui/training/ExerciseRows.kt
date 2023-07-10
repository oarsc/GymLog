package org.scp.gymlog.ui.training

import org.scp.gymlog.model.Variation
import org.scp.gymlog.ui.training.rows.ITrainingRow

class ExerciseRows(
    variation: Variation,
    val superSet: Int? = null
): MutableList<ITrainingRow> by mutableListOf()
{
    private val _variations: MutableSet<Variation> = mutableSetOf(variation)

    val variations get() = _variations.toList()
    val exercise get() = _variations.single().exercise

    fun addVariation(variation: Variation) = _variations.add(variation)
}