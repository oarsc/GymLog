package org.scp.gymlog.util.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scp.gymlog.exceptions.JsonException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;

public class JsonUtils {

    public static void forEachInt(JSONArray jsonArray, JsonConsumer<Integer> consumer) throws JSONException {
        int length = jsonArray.length();
        for (int i = 0; i<length; i++) {
            consumer.accept(jsonArray.getInt(i));
        }
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

    public static <T extends JsonMapped> void forEach(JSONArray jsonArray, Class<T> type, JsonConsumer<T> consumer) throws JSONException {
        forEachObject(jsonArray, jsonObject ->
            consumer.accept(create(jsonObject, type))
        );
    }

    public static <T extends JsonMapped> T create(JSONObject jsonObject, Class<T> type) throws JSONException {
        try {
            T instance = type.newInstance();
            instance.fromJson(jsonObject);
            return instance;
        } catch (IllegalAccessException | InstantiationException e) {
            throw new JsonException("Error creating "+type+" instance", e);
        }
    }

    public static <T extends JsonMapped> List<T> create(JSONArray jsonArray, Class<T> type) throws JSONException {
        List<T> array = new ArrayList<>();
        forEach(jsonArray, type, array::add);
        return array;
    }

    public static <T> Collector<T, JSONArray, JSONArray> collector() {
        return Collector.of(
                JSONArray::new, //init accumulator
                JSONArray::put, //processing each element
                JSONArray::put  //confluence 2 accumulators in parallel execution
        );
    }
}
