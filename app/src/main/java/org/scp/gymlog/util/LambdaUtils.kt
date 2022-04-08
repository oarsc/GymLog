package org.scp.gymlog.util

import java.util.function.BiConsumer
import java.util.function.Predicate

object LambdaUtils {
    fun <T> valueEquals(compare: T): Predicate<T> {
        return Predicate { value: T -> value == compare }
    }

    fun <I> indexForEach(list: List<I>, consumer: BiConsumer<Int, I>) {
        for ((index, element) in list.withIndex()) {
            consumer.accept(index, element)
        }
    }

    fun <I> indexForEach(list: Array<I>, consumer: BiConsumer<Int, I>) {
        for ((index, element) in list.withIndex()) {
            consumer.accept(index, element)
        }
    }
}
