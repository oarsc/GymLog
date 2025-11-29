package org.oar.gymlog.manager.ui.calendar.v1

import kotlinx.browser.window
import org.oar.gymlog.manager.constants.ExportId
import org.oar.gymlog.manager.constants.NotifierId
import org.oar.gymlog.manager.custom.DefinitionConstants.DIV
import org.oar.gymlog.manager.custom.DefinitionConstants.H2
import org.oar.gymlog.manager.custom.DefinitionConstants.SPAN
import org.oar.gymlog.manager.custom.HTMLBlock
import org.oar.gymlog.manager.custom.Utils.createBlock
import org.oar.gymlog.manager.custom.style
import org.oar.gymlog.manager.model.OutputBit
import org.oar.gymlog.manager.model.OutputTraining
import org.oar.gymlog.manager.ui.calendar.model.ExerciseBits
import org.oar.gymlog.manager.ui.common.HTMLInputEditBind
import org.w3c.dom.HTMLDivElement

class HTMLTrainingSummary: HTMLBlock<HTMLDivElement>(DIV, id = ID) {
    private var output = read(ExportId.output)!!

    private var trainingId: Int by renderProperty(
        initial = output.bits.lastOrNull()?.trainingId ?: 0,
        identifier = 1
    )
    private var bits = output.bits.filter { it.trainingId == trainingId }.toMutableList()

    private val content = createBlock(DIV)

    init {
        listen(NotifierId.trainingIdUpdated) {
            bits = output.bits.filter { bit -> bit.trainingId == it }.toMutableList()
            trainingId = it
        }

        listen(NotifierId.reload) {
            val scroll = window.scrollY
            bits = output.bits.filter { it.trainingId == trainingId }.toMutableList()
            render(1)
            window.scrollTo(.0, scroll)
        }

        +HTMLFilter(trainingId)
        +content
    }

    override fun render(identifier: Int) {
        when (identifier) {
            -1, 1 -> {
                content.clear(detach = true)
                content.apply {
                    +H2("training-title") {
                        +SPAN { -"Training #$trainingId" }

                        +HTMLInputEditBind(
                            obj = output.training[trainingId]!!,
                            accessor = OutputTraining::note,
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
                        ).apply {
                            classList.add("subtle")
                        }
                    }
                    +DIV(id = "bit-variation") {
                        bits.perVariation.forEach {
                            +HTMLTrainingExercise(it, output)
                        }
                    }
                }
            }
        }
    }

    private val List<OutputBit>.perVariation: List<ExerciseBits> get() {
        if (isEmpty()) return emptyList()

        return buildList {
            var exerciseBits = output.variation[this@perVariation.first().variationId]!!
                .let { (e, v) -> ExerciseBits(e, v) }
            add(exerciseBits)

            this@perVariation.forEach {
                if (it.variationId == exerciseBits.variation.variationId) {
                    exerciseBits.bits.add(it)
                } else {
                    exerciseBits = output.variation[it.variationId]!!
                        .let { (e, v) -> ExerciseBits(e, v, mutableListOf(it)) }
                    add(exerciseBits)
                }
            }
        }
    }

    companion object {
        const val ID = "calendar-training"
        init {
            style {
                "#$ID" {
                    ".training-title" {
                        "> *" {
                            "vertical-align" to "middle"
                        }

                        "input" {
                            "margin-left" to "10px"
                        }
                    }
                }
            }
        }
    }
}