package org.scp.gymlog.util;

import static org.scp.gymlog.util.FormatUtils.isAnyOf;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scp.gymlog.model.WeightSpecification;
import org.scp.gymlog.room.Converters;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
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

    public static JSONObject jsonify(Object obj) {
        if (obj instanceof Iterable<?> || obj instanceof Map)
            throw new RuntimeException("Object is a List or Map");

        try {
            JSONObject json = new JSONObject();
            for (Field field : obj.getClass().getFields()) {
                if (field.get(obj) != null) {
                    Type type = field.getType();
                    String fieldName = field.getName();
                    if (type == int.class)          json.put(fieldName, field.getInt(obj));
                    else if (type == long.class)    json.put(fieldName, field.getLong(obj));
                    else if (type == boolean.class) json.put(fieldName, field.getBoolean(obj));
                    else if (type == float.class)   json.put(fieldName, field.getFloat(obj));
                    else if (type == double.class)  json.put(fieldName, field.getDouble(obj));
                    else {
                        Object value = field.get(obj);
                        if (value != null) {
                            if (isAnyOf(type, Integer.class, Long.class, Boolean.class, Float.class, Double.class, String.class))
                                json.put(fieldName, value);
                            else if (type == WeightSpecification.class)
                                json.put(fieldName, Converters.fromWeightSpecification(
                                        (WeightSpecification)value));

                            else if (type == Calendar.class)
                                json.put(fieldName, Converters.fromDate(
                                        (Calendar)value));
                        }
                    }
                }
            }
            return json;
        } catch (Exception e) {
            throw new RuntimeException("An exception occurred", e);
        }
    }

    public static <T> T objectify(JSONObject json, Class<T> cls) {
        if (cls == Iterable.class || cls == Map.class)
            throw new RuntimeException("Object is a List or Map");

        try {
            T obj = cls.newInstance();
            for (Field field : cls.getFields()) {
                Type type = field.getType();
                String fieldName = field.getName();

                if (json.has(fieldName)){
                    if (type == int.class)          field.setInt(obj, json.getInt(fieldName));
                    else if (type == long.class)    field.setLong(obj, json.getLong(fieldName));
                    else if (type == boolean.class) field.setBoolean(obj, json.getBoolean(fieldName));
                    else if (type == float.class)   field.setFloat(obj, (float)json.getDouble(fieldName));
                    else if (type == double.class)  field.setDouble(obj, json.getDouble(fieldName));
                    else if (type == String.class)  field.set(obj, json.getString(fieldName));

                    else if (type == WeightSpecification.class)
                        field.set(obj, Converters.toWeightSpecification(
                                (short) json.getInt(fieldName)));
                    else if (type == Calendar.class)
                        field.set(obj, Converters.toDate(
                                json.getLong(fieldName)));

                    else if (type == Integer.class) field.set(obj, json.getInt(fieldName));
                    else if (type == Long.class)    field.set(obj, json.getLong(fieldName));
                    else if (type == Boolean.class) field.set(obj, json.getBoolean(fieldName));
                    else if (type == Float.class)   field.set(obj, (float)json.getDouble(fieldName));
                    else if (type == Double.class)  field.set(obj, json.getDouble(fieldName));
                }
            }
            return obj;
        } catch (Exception e) {
            throw new RuntimeException("An exception occurred", e);
        }
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
