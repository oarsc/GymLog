package org.scp.gymlog.util;

import java.util.function.Predicate;

public class LambdaUtils {

    public static <T> Predicate<T> valueEquals(T compare) {
        return value -> value.equals(compare);
    }
}
