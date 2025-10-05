package org.oar.gymlog.util

class WeightFormatter {

    companion object {
        val DEFAULT_WEIGHT_FORMATTER = WeightFormatter(false)
        val EXACT_FORMATTER = WeightFormatter(true)
        val TWO_DECS_FORMATTER = WeightFormatter(2)
    }

    var forceScale = false
    var scale = 0
    var exactScale = false

    constructor(exactScale: Boolean) {
        this.exactScale = exactScale
    }

    constructor(scale: Int) {
        forceScale = true
        this.scale = scale
    }
}