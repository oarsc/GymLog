package org.scp.gymlog.ui.training.rows

import org.scp.gymlog.model.Bit
import org.scp.gymlog.model.Variation

class TrainingRowData(
    variation: Variation,
    val superSet: Int? = null,
    var expanded: Boolean = false
): MutableList<Bit> by mutableListOf()
{
    private val _variations: MutableSet<Variation> = mutableSetOf(variation)

    val variations get() = _variations.toList()
    val variation get() = _variations.single()

    fun addVariation(variation: Variation) = _variations.add(variation)
}