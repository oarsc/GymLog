package org.oar.gymlog.manager.lib

import io.nacular.doodle.utils.observable
import kotlinx.browser.document
import kotlinx.dom.clear
import org.oar.gymlog.manager.lib.HTMLDefinitionConstants.HTMLDefinition
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

    fun append(block:  HTMLBlock<*>): HTMLBlock<E> {
        _children.add(block)
        element.appendChild(block.element)
        block.parent = this
        return this
    }

    fun append(text: String): HTMLBlock<E> {
        this@HTMLBlock.element.appendChild(document.createTextNode(text))
        return this
    }

    fun <T : HTMLElement> remove(block: HTMLBlock<T>): HTMLBlock<T> {
        if (block.parent == this) {
            element.removeChild(block.element)
            _children.remove(block)
            block.parent = null
        }
        return block
    }

    fun remove(): HTMLBlock<E> {
        parent?.remove(this)
        return this
    }


    fun <O : HTMLElement, N : HTMLElement> replace(old: HTMLBlock<O>, new: HTMLBlock<N>) {
        if (old.parent == this) {
            val index = _children.indexOfFirst { it == old }
                .takeIf { it >= 0}
                ?: throw Error("Wrong parent")

            _children.removeAt(index)
            _children.add(index, new)
            element.replaceChild(new.element, old.element)
            old.parent = null
            new.parent = this

        } else {
            throw Error("Wrong parent")
        }
    }

    fun clear(detachMode: DetachMode = DetachMode.NONE) {
        if (detachMode == DetachMode.DETACH) {
            detachAll()
        }
        _children.forEach {
            if (detachMode == DetachMode.DETACH || detachMode == DetachMode.DETACH_ONLY_CHILDREN) {
                it.clear(detachMode = DetachMode.DETACH)
            }
            it.parent = null
        }
        _children.clear()
        element.clear()
    }

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

    operator fun HTMLBlock<*>?.unaryPlus() {
        if (this != null) {
            this@HTMLBlock.append(this)
        }
    }
    operator fun <T : HTMLElement> HTMLBlock<T>.unaryMinus(): HTMLBlock<T> = this@HTMLBlock.remove(this)
    operator fun <T : HTMLElement> HTMLBlock<T>?.unaryMinus(): HTMLBlock<T>? = this?.let(this@HTMLBlock::remove)

    operator fun String.unaryPlus() = append(this)
    operator fun String.unaryMinus() {
        element.textContent = this
    }
    operator fun String.not() {
        element.innerHTML = this
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

        fun <T: HTMLElement> createBlock(
            htmlDefinition: HTMLDefinition<T>,
            className: String? = null,
            id: String? = null
        ) = object : HTMLBlock<T>(htmlDefinition, className = className, id = id) { }
    }

    enum class DetachMode {
        DETACH,
        DETACH_ONLY_CHILDREN,
        NONE
    }
}