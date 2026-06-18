package org.oar.gymlog.manager.utils

import org.oar.gymlog.manager.model.Output
import org.oar.gymlog.manager.model.OutputExercise
import org.oar.lib.ExportId
import org.oar.lib.NotifierId

object Export {
    val output = object : ExportId<Output>() {}
    val menuId = object : ExportId<Int>() {}
}

object Notifier {
    val fileLoaded = object : NotifierId<Unit>() {}
    val trainingIdUpdated = object : NotifierId<Int>() {}
    val menuIdChanged = object : NotifierId<Int>() {}
    val showLoadFile = object : NotifierId<Boolean>() {}
    val reload = object : NotifierId<Unit>() {}
    val exerciseSelected = object : NotifierId<OutputExercise>() {}

    // Panel switch
    val editorBitsPanel = object : NotifierId<OutputExercise>() {}
    val editorExercisePanel = object : NotifierId<Unit>() {}
}