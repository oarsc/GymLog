package org.oar.gymlog.manager.ui._common

import org.oar.gymlog.manager.Style.BUTTON_STYLE
import org.oar.gymlog.manager.lib.HTMLBlock
import org.oar.gymlog.manager.lib.HTMLBlock.Companion.HTMLBodyBlock
import org.oar.gymlog.manager.lib.HTMLDefinitionConstants.BUTTON
import org.oar.gymlog.manager.lib.HTMLDefinitionConstants.INPUT
import org.oar.gymlog.manager.lib.HTMLDefinitionConstants.SPAN
import org.oar.gymlog.manager.lib.HTMLDefinitionConstants.TD
import org.oar.gymlog.manager.lib.HTMLDefinitionConstants.TR
import org.oar.gymlog.manager.lib.style
import org.oar.gymlog.manager.model.Output
import org.oar.gymlog.manager.model.OutputBit
import org.oar.gymlog.manager.ui._common.HTMLCommonExerciseHistory.Column
import org.oar.gymlog.manager.ui._common.HTMLCommonExerciseHistory.Column.ACTION
import org.oar.gymlog.manager.ui._common.HTMLCommonExerciseHistory.Column.INSTANT
import org.oar.gymlog.manager.ui._common.HTMLCommonExerciseHistory.Column.NOTE
import org.oar.gymlog.manager.ui._common.HTMLCommonExerciseHistory.Column.NUMBER
import org.oar.gymlog.manager.ui._common.HTMLCommonExerciseHistory.Column.REPS
import org.oar.gymlog.manager.ui._common.HTMLCommonExerciseHistory.Column.SUPER_SET
import org.oar.gymlog.manager.ui._common.HTMLCommonExerciseHistory.Column.TIMESTAMP
import org.oar.gymlog.manager.ui._common.HTMLCommonExerciseHistory.Column.VARIATION
import org.oar.gymlog.manager.ui._common.HTMLCommonExerciseHistory.Column.WEIGHT
import org.oar.gymlog.manager.ui.support.HTMLCheckboxEditBind
import org.oar.gymlog.manager.ui.support.HTMLInputEditBind
import org.oar.gymlog.manager.ui.support.HTMLSelectEditBind
import org.oar.gymlog.manager.ui.support.HTMLSelectVariationDialog
import org.oar.gymlog.manager.utils.Notifier
import org.oar.gymlog.manager.utils.Utils.confirm
import org.oar.gymlog.manager.utils.extensions.DateExt.compareTo
import org.oar.gymlog.manager.utils.extensions.DateExt.middleDate
import org.oar.gymlog.manager.utils.extensions.DateExt.toLocaleISODateString
import org.oar.gymlog.manager.utils.extensions.DateExt.toLocaleISOTimeString
import org.w3c.dom.HTMLTableCellElement
import org.w3c.dom.HTMLTableRowElement
import kotlin.js.Date

class HTMLCommonBitHistory(
    private val bit: OutputBit,
    private val output: Output,
    private val columns: List<Column>,
    private val onShiftVariationChange: (Int) -> Unit
): HTMLBlock<HTMLTableRowElement>(TR, className = CLASS_NAME) {

    private val indexElement = SPAN()
    var index: Int = -1
        set(value) {
            if (field != value) {
                field = value
                indexElement.element.textContent = if (value > 0) value.toString() else ""
            }
        }

    var onUpdateInstant: (() -> Unit)? = null

    init {
        apply {
            +rowFor(NUMBER) {
                +indexElement
            }
            +rowFor(INSTANT) {
                +HTMLCheckboxEditBind(
                    obj = bit,
                    accessor = OutputBit::instant,
                    default = false
                ).apply {
                    onchange = { onUpdateInstant?.invoke() }
                }
            }
            +rowFor(SUPER_SET) {
                +HTMLInputEditBind(
                    obj = bit,
                    accessor = OutputBit::superSet,
                    mapperBack = { block ->
                        block.toIntOrNull()
                            ?.takeIf { it > 0 }
                            .also { element.classList.toggle("subtle", it == null) }
                    }
                ).apply {
                    element.classList.toggle("subtle", bit.superSet == null)
                    element.style.width = "21px"
                }
            }
            +rowFor(TIMESTAMP, "timestamp") {
                +SPAN {
                    -bit.timestamp.toLocaleISODateString()
                }
                +INPUT("subtle") {
                    element.apply {
                        type = "time"
                        value = bit.timestamp.toLocaleISOTimeString()
                        onblur = {
                            val index = output.bits.indexOf(bit)
                            bit.timestamp = Date(bit.timestamp.toLocaleISODateString() + "T" + value)
                                .let { date ->
                                    output.bits.getOrNull(index - 1)
                                        ?.timestamp
                                        ?.takeIf { it >= date }
                                        ?.let { Date(it.getTime() + 1) }
                                        ?: date
                                }
                                .let { date ->
                                    output.bits.getOrNull(index + 1)
                                        ?.timestamp
                                        ?.takeIf { it <= date }
                                        ?.let { Date(it.getTime() - 1) }
                                        ?: date
                                }
                                .also { value = it.toLocaleISOTimeString() }
                            Unit
                        }
                    }
                }
            }
            +rowFor(REPS) {
                +HTMLInputEditBind(
                    obj = bit,
                    accessor = OutputBit::reps,
                    mapperBack = { it.toIntOrNull() ?: 0 }
                ).apply { element.style.width = "26px" }
            }
            +rowFor(WEIGHT) {
                +HTMLInputEditBind(
                    obj = bit,
                    accessor = OutputBit::totalWeight,
                    mapperBack = { it.toDoubleOrNull()?.let { it * 100 } ?: 0.0 },
                    mapper = { (this/100).toString() }
                ).apply { element.style.width = "54px" }
            }
            +rowFor(NOTE) {
                +HTMLInputEditBind(
                    obj = bit,
                    accessor = OutputBit::note,
                    mapperBack = {
                        val trim = it.trim()
                        if (trim.isEmpty()) null
                        else {
                            output.notes.indexOf(trim)
                                .takeIf { idx -> idx >= 0 }
                                ?: run {
                                    val idx = output.notes.size
                                    output.notes.add(trim)
                                    output.note[idx] = trim
                                    idx
                                }
                        }
                    },
                    mapper = { this?.let(output.note::get) ?: "" }
                )
            }
            +rowFor(VARIATION) {
                val exercise = output.variation[bit.variationId]!!.first
                val variations = output.variations.filter { it.exerciseId == exercise.exerciseId }.sortedBy { !it.def }
                    .associate {
                        "${it.variationId}" to if (it.def) "Default" else it.name
                    }

                var shiftClicked = false

                +HTMLSelectEditBind(
                    obj = bit,
                    accessor = OutputBit::variationId,
                    options = variations,
                    mapperBack = {
                        val variationId = toInt()
                        if (shiftClicked) {
                            onShiftVariationChange(variationId)
                        }
                        variationId
                    }
                ).apply {
                    element.onmousedown = { e ->
                        if (e.buttons == 1.toShort()) {
                            shiftClicked = e.shiftKey
                        }
                    }
                }
            }
            +rowFor(ACTION, "actions") {
                +BUTTON(className = BUTTON_STYLE) {
                    element.apply {
                        textContent = "Var."
                        onclick = {
                            HTMLBodyBlock.append (
                                HTMLSelectVariationDialog(initialVariationId = bit.variationId) {
                                    if (it != null) {
                                        bit.variationId = it
                                        notify(Notifier.reload)
                                    }
                                }
                            )
                        }
                    }
                }
                +BUTTON(className = BUTTON_STYLE) {
                    element.apply {
                        textContent = "+"
                        onclick = {
                            val index = output.bits.indexOf(bit)
                            val next = output.bits.getOrNull(index + 1)
                            val timestamp = next
                                ?.takeIf { it.trainingId == bit.trainingId }
                                ?.timestamp?.middleDate(bit.timestamp)
                                ?: Date(bit.timestamp.getTime() + 1)
                            output.bits.add(
                                index = index + 1,
                                element = bit.copy(
                                    timestamp = timestamp
                                )
                            )
                            notify(Notifier.reload)
                        }
                    }
                }
                +BUTTON(className = "$BUTTON_STYLE red") {
                    element.apply {
                        textContent = "×"
                        onclick = {
                            if (confirm("Are you sure you want to remove?") && output.bits.remove(bit)) {
                                notify(Notifier.reload)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun rowFor(column: Column, className: String? = null, action: HTMLBlock<HTMLTableCellElement>.() -> Unit = {}): HTMLBlock<*>? =
        if (columns.contains(column)) TD(className = className) { action() }
        else null

    companion object {
        const val CLASS_NAME = "training-bit"

        init {
            style {
                ".$CLASS_NAME" {
                    ".input" {
                        "text-align" to "center"
                    }
                    "td" {
                        "text-align" to "center"

                        "&.timestamp" {
                            "span" {
                                "display" to "none"
                                "margin" to "0 10px"
                            }

                            "> *" {
                                "font-size" to "13.3333px"
                                "font-family" to "Cantarell, sans"
                            }
                        }
                    }
                }
            }
        }
    }
}