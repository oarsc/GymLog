package org.oar.gymlog.manager.constants

import org.oar.gymlog.manager.model.OutputExercise
import org.oar.gymlog.manager.model.OutputVariation

object NotifierId {
    val fileLoaded = object : NotifierId<Unit>() {}
    val trainingIdUpdated = object : NotifierId<Int>() {}
    val menuIdChanged = object : NotifierId<Int>() {}
    val showLoadFile = object : NotifierId<Boolean>() {}
    val reload = object : NotifierId<Unit>() {}
    val exerciseSelected = object : NotifierId<OutputExercise>() {}

    val editorBitsPanel = object : NotifierId<OutputVariation>() {}
    val editorExercisePanel = object : NotifierId<Unit>() {}

    abstract class NotifierId<T: Any> internal constructor()
}