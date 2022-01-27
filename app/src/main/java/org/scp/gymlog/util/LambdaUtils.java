package org.scp.gymlog.util;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class LambdaUtils {

    public static <T> Predicate<T> valueEquals(T compare) {
        return value -> value.equals(compare);
    }

    public static <I> void indexForEach(List<I> list, BiConsumer<Integer, I> consumer) {
        int index = 0;
        for (I element : list) {
            consumer.accept(index, element);
            index++;
        }
    }

    public static <I> void indexForEach(I[] list, BiConsumer<Integer, I> consumer) {
        int index = 0;
        for (I element : list) {
            consumer.accept(index, element);
            index++;
        }
    }
}
