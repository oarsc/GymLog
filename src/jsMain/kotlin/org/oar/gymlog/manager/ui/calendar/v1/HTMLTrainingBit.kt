package org.oar.gymlog.manager.ui.calendar.v1

import org.oar.gymlog.manager.Style.BUTTON_STYLE
import org.oar.gymlog.manager.constants.NotifierId
import org.oar.gymlog.manager.custom.DefinitionConstants.BUTTON
import org.oar.gymlog.manager.custom.DefinitionConstants.INPUT
import org.oar.gymlog.manager.custom.DefinitionConstants.SPAN
import org.oar.gymlog.manager.custom.DefinitionConstants.TD
import org.oar.gymlog.manager.custom.DefinitionConstants.TR
import org.oar.gymlog.manager.custom.HTMLBlock
import org.oar.gymlog.manager.custom.HTMLBlock.Companion.HTMLBodyBlock
import org.oar.gymlog.manager.custom.Utils.confirm
import org.oar.gymlog.manager.custom.style
import org.oar.gymlog.manager.model.Output
import org.oar.gymlog.manager.model.OutputBit
import org.oar.gymlog.manager.ui.support.HTMLCheckboxEditBind
import org.oar.gymlog.manager.ui.support.HTMLInputEditBind
import org.oar.gymlog.manager.ui.support.HTMLSelectVariationDialog
import org.oar.gymlog.manager.utils.DateExt.compareTo
import org.oar.gymlog.manager.utils.DateExt.middleDate
import org.oar.gymlog.manager.utils.DateExt.toLocaleISODateString
import org.oar.gymlog.manager.utils.DateExt.toLocaleISOTimeString
import org.w3c.dom.HTMLTableRowElement
import kotlin.js.Date

class HTMLTrainingBit(
    private val bit: OutputBit,
    private val output: Output,
): HTMLBlock<HTMLTableRowElement>(TR, className = CLASS_NAME) {

    private val indexElement = TD()
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
            +indexElement
            +TD {
                +HTMLCheckboxEditBind(
                    obj = bit,
                    accessor = OutputBit::instant,
                    default = false
                ).apply {
                    onchange = { onUpdateInstant?.invoke() }
                }
            }
            +TD {
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
            +TD("timestamp") {
                +SPAN {
                    -bit.timestamp.toLocaleISODateString()
                }
                +INPUT("subtle") {
                    element.apply {
                        type = "time"
                        value = bit.timestamp.toLocaleISOTimeString()
                        onblur = {
                            val index = output.bits.indexOf(bit)
                            bit.timestamp = Date(bit.timestamp.toLocaleISODateString() + "T" +value)
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
            +TD {
                +HTMLInputEditBind(
                    obj = bit,
                    accessor = OutputBit::reps,
                    mapperBack = { it.toIntOrNull() ?: 0 }
                ).apply { element.style.width = "26px" }
            }
            +TD {
                +HTMLInputEditBind(
                    obj = bit,
                    accessor = OutputBit::totalWeight,
                    mapperBack = { it.toDoubleOrNull()?.let { it * 100 } ?: 0.0 },
                    mapper = { (this/100).toString() }
                ).apply { element.style.width = "54px" }
            }
            +TD {
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
            +TD("actions") {
                +BUTTON(className = BUTTON_STYLE) {
                    element.apply {
                        textContent = "Var."
                        onclick = {
                            HTMLBodyBlock.append (
                                HTMLSelectVariationDialog(initialVariationId = bit.variationId) {
                                    if (it != null) {
                                        bit.variationId = it
                                        notify(NotifierId.reload)
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
                            notify(NotifierId.reload)
                        }
                    }
                }
                +BUTTON(className = "$BUTTON_STYLE red") {
                    element.apply {
                        textContent = "×"
                        onclick = {
                            if (confirm("Are you sure you want to remove?") && output.bits.remove(bit)) {
                                notify(NotifierId.reload)
                            }
                        }
                    }
                }
            }
        }
    }

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