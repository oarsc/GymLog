package org.scp.gymlog.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class JsonUtils {

    public static void forEachInt(JSONArray jsonArray, JsonConsumer<Integer> consumer) throws JSONException {
        int length = jsonArray.length();
        for (int i = 0; i<length; i++) {
            consumer.accept(jsonArray.getInt(i));
        }
    }

    public static <T> Stream<T> mapInt(JSONArray jsonArray, JsonFunction<T, Integer> function) throws JSONException {
        List<T> supList = new ArrayList<>();
        forEachInt(jsonArray, i -> {
            supList.add(function.call(i));
        });
        return supList.stream();
    }

    public static void forEachString(JSONArray jsonArray, JsonConsumer<String> consumer) throws JSONException {
        int length = jsonArray.length();
        for (int i = 0; i<length; i++) {
            consumer.accept(jsonArray.getString(i));
        }
    }

    public static void forEachObject(JSONArray jsonArray, JsonConsumer<JSONObject> consumer) throws JSONException {
        int length = jsonArray.length();
        for (int i = 0; i<length; i++) {
            consumer.accept(jsonArray.getJSONObject(i));
        }
    }

    public static <T> Stream<T> mapObject(JSONArray jsonArray, JsonFunction<T, JSONObject> function) throws JSONException {
        List<T> supList = new ArrayList<>();
        forEachObject(jsonArray, obj -> {
            supList.add(function.call(obj));
        });
        return supList.stream();
    }

    public static <T> Collector<T, JSONArray, JSONArray> collector() {
        return Collector.of(
                JSONArray::new, //init accumulator
                JSONArray::put, //processing each element
                JSONArray::put  //confluence 2 accumulators in parallel execution
        );
    }

    @FunctionalInterface
    public interface JsonConsumer<T> {
        void accept(T t) throws JSONException;
    }

    @FunctionalInterface
    public interface JsonFunction<R,T> {
        R call(T t) throws JSONException;
    }
}
