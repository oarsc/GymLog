package org.oar.gymlog.manager.ui.editor.v1

import org.oar.gymlog.manager.constants.ExportId
import org.oar.gymlog.manager.constants.NotifierId
import org.oar.gymlog.manager.custom.DefinitionConstants.DIV
import org.oar.gymlog.manager.custom.DefinitionConstants.H1
import org.oar.gymlog.manager.custom.DefinitionConstants.TABLE
import org.oar.gymlog.manager.custom.DefinitionConstants.TBODY
import org.oar.gymlog.manager.custom.DefinitionConstants.TD
import org.oar.gymlog.manager.custom.DefinitionConstants.TH
import org.oar.gymlog.manager.custom.DefinitionConstants.THEAD
import org.oar.gymlog.manager.custom.DefinitionConstants.TR
import org.oar.gymlog.manager.custom.HTMLBlock
import org.oar.gymlog.manager.custom.Utils.createBlock
import org.oar.gymlog.manager.custom.style
import org.oar.gymlog.manager.model.Output
import org.oar.gymlog.manager.model.OutputExercise
import org.oar.gymlog.manager.ui.common.HTMLExerciseIcon
import org.oar.gymlog.manager.ui.common.HTMLInputEditBind
import org.w3c.dom.HTMLDivElement

class HTMLExerciseEditor: HTMLBlock<HTMLDivElement>(DIV, id = ID) {

    private var output: Output = read(ExportId.output)!!
    private lateinit var exerciseSelected: OutputExercise

    init {
        listen(NotifierId.exerciseSelected) {
            exerciseSelected = it
            load()
        }

        listen(NotifierId.reload) {
            load()
        }
    }

    private fun load() {
        clear(detach = true)
        +createExerciseBox(exerciseSelected)
        +DIV {
            +addVariations(exerciseSelected)
        }
    }

    private fun createExerciseBox(exercise: OutputExercise) = createBlock(DIV).apply {
        +H1 { -"Exercise #${exercise.exerciseId}" }

        +HTMLExerciseIcon(source = exercise.image, size = 64)

        +HTMLInputEditBind(
            obj = exercise,
            accessor = OutputExercise::name,
            mapperBack = { it.trim() }
        ).apply { classList.add("name","subtle") }

        +TABLE {
            +THEAD {
                +TR {
                    +TH { -"Primary" }
                    +TH { -"Secondary" }
                }
            }
            +TBODY {
                +TR {
                    +TD("muscle-cell") {
                        +HTMLMuscleAssigner(output, exercise, output.primaries)
                    }
                    +TD("muscle-cell") {
                        +HTMLMuscleAssigner(output, exercise, output.secondaries, allowEmpty = true)
                    }
                }
            }
        }
    }

    private fun addVariations(exercise: OutputExercise)  = createBlock(DIV).apply {
        output.variations
            .filter { it.exerciseId == exercise.exerciseId }
            .forEach {
                +HTMLVariationEditor(output, it.variationId)
            }
    }

    companion object {
        const val ID = "exercise-editor"
        init {
            style {
                "#$ID" {
                    ".muscle-cell" {
                        "vertical-align" to "top"
                    }
                    ".name" {
                        "padding" to "10px"
                        "width" to "500px"
                        "margin-left" to "10px"
                        "font-size" to "16px"
                    }
                }
            }
        }
    }
}