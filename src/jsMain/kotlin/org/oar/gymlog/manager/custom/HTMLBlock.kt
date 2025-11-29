package org.oar.gymlog.manager.custom

import io.nacular.doodle.utils.observable
import kotlinx.browser.document
import kotlinx.dom.clear
import org.oar.gymlog.manager.constants.ExportId.ExportId
import org.oar.gymlog.manager.constants.NotifierId.NotifierId
import org.oar.gymlog.manager.custom.DefinitionConstants.HTMLDefinition
import org.oar.gymlog.manager.custom.DefinitionConstants.toDefinition
import org.oar.gymlog.manager.custom.Utils.createBlock
import org.w3c.dom.HTMLBodyElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLHeadElement
import kotlin.js.Promise
import kotlin.properties.ReadWriteProperty
import kotlin.random.Random

abstract class HTMLBlock<E : HTMLElement> private constructor(val element: E) {
    private val _id = Random.nextLong()
    val classList = element.classList

    constructor(
        htmlDefinition: HTMLDefinition<E>,
        className: String? = null,
        id: String? = null
    ) : this(
        element = htmlDefinition.create()
    ) {
        element.apply {
            className?.let { this.className = it }
            if (id != null) {
                this.id = id
                blocksById[id] = this@HTMLBlock
            }
        }
        Promise.resolve(Unit).then { render(-1) }
    }

    private val _children = mutableListOf<HTMLBlock<*>>()
    val children: List<HTMLBlock<*>> = _children
    var parent: HTMLBlock<*>? = null
        private set

    protected fun <T> renderProperty(
        initial: T,
        identifier: Int = -1,
        onChange: HTMLBlock<E>.(old: T, new: T) -> Unit = { _, _ -> }
    ): ReadWriteProperty<HTMLBlock<E>, T> = observable(initial) { old, new ->
        if (old != new) {
            onChange(old, new)
            update(identifier)
        }
    }

    @Deprecated("Use apply instead")
    fun append(build: ElementBuilder<E>.() -> Unit): HTMLBlock<E> {
        ElementBuilder(this, element).build()
        return this
    }

    fun append(block:  HTMLBlock<*>): HTMLBlock<E> {
        _children.add(block)
        element.appendChild(block.element)
        block.parent = this
        return this
    }

//    @Deprecated("Use append(HTMLBlock) instead", ReplaceWith("append(block)"))
//    fun append(element: Element) {
//        this@HTMLBlock.element.appendChild(element)
//    }

    fun append(text: String): HTMLBlock<E> {
        this@HTMLBlock.element.appendChild(document.createTextNode(text))
        return this
    }

    fun remove(block: HTMLBlock<*>): HTMLBlock<E> {
        if (block.parent == this) {
            element.removeChild(block.element)
            _children.remove(block)
            block.parent = null
        }
        return this
    }

    fun remove(): HTMLBlock<E> {
        parent?.remove(this)
        return this
    }

    fun clear(detach: Boolean = false) {
        _children.forEach {
            if (detach) {
                it.detachAll()
                it.clear(detach = true)
            }
            it.parent = null
        }
        _children.clear()
        element.clear()
    }

//    @Deprecated("Use remove(HTMLBlock) instead", ReplaceWith("remove(block)"))
//    fun remove(element: Element) {
//        this@HTMLBlock.element.removeChild(element)
//    }

    fun detachAll(exposes: Boolean = true, listeners: Boolean = true) {
        if (exposes)
        exposeMap.iterator().apply {
            while (hasNext()) {
                next()
                    .takeIf { it.value.referenceId == _id }
                    ?.also { remove()  }
            }
        }

        if (listeners)
        notifierList.iterator().apply {
            while (hasNext()) {
                next()
                    .takeIf { it.referenceId == _id }
                    ?.also { remove()  }
            }
        }
    }


    // NEW
    operator fun <T : HTMLElement> HTMLDefinition<T>.invoke(className: String? = null, id: String? = null, build: HTMLBlock<T>.() -> Unit): HTMLBlock<T> =
        createBlock(this).apply {
            if (className != null) element.className = className
            if (id != null) element.id = id
            build()
        }

    operator fun HTMLBlock<*>.unaryPlus() = this@HTMLBlock.append(this)
    operator fun HTMLBlock<*>.inc() = this@HTMLBlock.append(this)
    operator fun String.unaryPlus() = append(this)
    operator fun String.unaryMinus() {
        element.textContent = this
    }
    operator fun String.not() {
        element.innerHTML = this
    }

    @Deprecated("")
    class ElementBuilder<E : HTMLElement> internal constructor(
        val block: HTMLBlock<E>,
        _element: HTMLElement
    ) {
        val element: E = _element as E
        @Deprecated("")
        var id: String
            get() = element.id
            set(id) { element.id = id }
        @Deprecated("")
        var className: String
            get() = element.className
            set(className) { element.className = className }

        @Deprecated("")
        operator fun String.invoke(build: ElementBuilder<*>.() -> Unit) {
            val subBlock = createBlock(this.toDefinition())
            block.append(subBlock)

            ElementBuilder(subBlock, subBlock.element).build()
        }

        @Deprecated("")
        operator fun <T : HTMLElement> HTMLDefinition<T>.invoke(build: ElementBuilder<T>.() -> Unit) {
            val subBlock = createBlock(this)
            block.append(subBlock)

            ElementBuilder(subBlock, subBlock.element).build()
        }

        @Suppress("LABEL_RESOLVE_WILL_CHANGE")
        operator fun HTMLBlock<*>.unaryPlus() = this@ElementBuilder.block.append(this)
        @Deprecated("")
        fun add(block: HTMLBlock<*>) = this.block.append(block)
        @Deprecated("")
        operator fun String.unaryPlus() = block.append(this)
        @Deprecated("")
        operator fun String.unaryMinus() {
            element.textContent = this
        }
        @Deprecated("")
        operator fun String.not() {
            element.innerHTML = this
        }
    }

    private fun update(identifier: Int) = render(identifier)
    protected open fun render(identifier: Int) {}

    companion object {
        object HTMLBodyBlock:
            HTMLBlock<HTMLBodyElement>(document.body as HTMLBodyElement)

        object HTMLHeadBlock:
            HTMLBlock<HTMLHeadElement>(document.head as HTMLHeadElement)

        private val exposeMap = mutableMapOf<ExportId<*>, Exporter<*>>()

        data class Exporter<T: Any>(
            val function: () -> T?,
            val referenceId: Long
        )

        fun <H: HTMLBlock<*>, T: Any> H.expose(id: ExportId<T>, function: () -> T?) {
            if (exposeMap.containsKey(id)) {
                throw Error("ExportId already exposed.")
            }
            exposeMap[id] = Exporter(function, _id)
        }

        @Suppress("UNCHECKED_CAST")
        fun <T: Any> read(id: ExportId<T>): T? {
            return exposeMap[id]
                ?.let { it.function as () -> T }
                ?.let { it() }
        }

        private val notifierList = mutableListOf<Notifier<*>>()

        data class Notifier<T: Any>(
            val id: NotifierId<T>,
            val function: (T) -> Unit,
            val referenceId: Long
        )

        fun <H: HTMLBlock<*>, T: Any> H.listen(id: NotifierId<T>, function: (T) -> Unit) {
            notifierList.add(Notifier(id, function, _id))
        }

        fun notify(id: NotifierId<Unit>) = notify(id, Unit)

        @Suppress("UNCHECKED_CAST")
        fun <T: Any> notify(id: NotifierId<T>, value: T) {
            val listeners = notifierList.filter { it.id == id } as List<Notifier<T>>
            listeners.forEach { it.function(value) }
        }

        private val blocksById = mutableMapOf<String, HTMLBlock<*>>()

        @Suppress("UNCHECKED_CAST")
        fun <T: HTMLElement> findById(id: String): HTMLBlock<T>? =
            blocksById[id] as? HTMLBlock<T>

        fun resetElements() {
            blocksById.clear()
        }
    }
}