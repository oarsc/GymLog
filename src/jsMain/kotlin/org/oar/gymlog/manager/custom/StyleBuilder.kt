package org.oar.gymlog.manager.custom

import org.oar.gymlog.manager.custom.DefinitionConstants.STYLE
import org.oar.gymlog.manager.custom.HTMLBlock.Companion.HTMLHeadBlock
import org.w3c.dom.css.CSSStyleSheet

private val styleElement = STYLE(build = HTMLHeadBlock::append)
    .element
    .apply { type = "text/css" }

fun style(builder: CSSBuilder.() -> Unit) {
    val sheet = styleElement.sheet as CSSStyleSheet
    CSSBuilder(sheet).builder()
}

class CSSBuilder(private val sheet: CSSStyleSheet) {
    operator fun String.invoke(buildProperties: CSSPropertiesBuilder.() -> Unit) {
        CSSPropertiesBuilder()
            .apply(buildProperties)
            .build(this)
            .map { sheet.insertRule(it, sheet.cssRules.length) }
    }
}

class CSSPropertiesBuilder {
    private val rules = mutableListOf<String>()

    infix fun String.to(other: Any) {
        val kebabCaseName = replace(Regex("([A-Z])")) { "-${it.value.lowercase()}" }
        rules.add("$DELIMITER$kebabCaseName:$other;")
    }

    operator fun String.invoke(buildProperties: CSSPropertiesBuilder.() -> Unit) {
        val cssBuilder = CSSPropertiesBuilder()
            .apply { buildProperties() }

        split(",")
            .map(String::trim)
            .map {
                when {
                    it.startsWith("&") -> it.substring(1)
                    it.isNotEmpty() -> " $it"
                    else -> it
                }
            }
            .flatMap { selector ->
                cssBuilder.rules.map {
//                    console.log(selector, "->", it)
                    "$selector$it"
                } }
            .let(rules::addAll)
    }

    fun build(selector: String): List<String> {
        val grouped = rules
            .map { it.split(DELIMITER) }
            .filter { it.size > 1 }
            .groupBy(
                keySelector = { it.first() },
                valueTransform = { it.drop(1).joinToString("") }
            )

        return selector.split(",")
            .map(String::trim)
            .flatMap { pre ->
                grouped.entries.map { (select, value) ->
                    "$pre$select{${value.joinToString("")}}"
                }
            }
    }

    companion object {
        private const val DELIMITER = "''"
    }
}
