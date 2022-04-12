package org.scp.gymlog.util

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.scp.gymlog.model.WeightSpecification
import org.scp.gymlog.room.Converters.fromDate
import org.scp.gymlog.room.Converters.fromWeightSpecification
import org.scp.gymlog.room.Converters.toDate
import org.scp.gymlog.room.Converters.toWeightSpecification
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KType
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties

object JsonUtils {
    @Throws(JSONException::class)
    fun forEachInt(jsonArray: JSONArray, consumer: JsonConsumer<Int>) {
        val length = jsonArray.length()
        for (i in 0 until length) {
            consumer.accept(jsonArray.getInt(i))
        }
    }

    @Throws(JSONException::class)
    fun <T> mapInt(jsonArray: JSONArray, function: JsonFunction<T, Int>): List<T> {
        val supList: MutableList<T> = ArrayList()
        forEachInt(jsonArray) { i: Int ->
            supList.add(function.call(i))
        }
        return supList
    }

    @Throws(JSONException::class)
    fun forEachString(jsonArray: JSONArray, consumer: JsonConsumer<String?>) {
        val length = jsonArray.length()
        for (i in 0 until length) {
            consumer.accept(jsonArray.getString(i))
        }
    }

    @Throws(JSONException::class)
    fun forEachObject(jsonArray: JSONArray, consumer: JsonConsumer<JSONObject>) {
        val length = jsonArray.length()
        for (i in 0 until length) {
            consumer.accept(jsonArray.getJSONObject(i))
        }
    }

    @Throws(JSONException::class)
    fun <T> mapObject(jsonArray: JSONArray, function: JsonFunction<T, JSONObject>): List<T> {
        val supList: MutableList<T> = ArrayList()
        forEachObject(jsonArray) { obj: JSONObject ->
            supList.add(function.call(obj))
        }
        return supList
    }

    fun jsonify(obj: Any): JSONObject {
        if (obj is Iterable<*> || obj is Map<*, *>) throw RuntimeException("Object is a List or Map")
        return try {
            val json = JSONObject()
            for (field in obj::class.memberProperties) {
                if (field.hasAnnotation<NoJsonify>()) continue
                val value = field.getter.call(obj)
                if (value != null) {
                    val type: KType = field.returnType
                    val fieldName = field.name

                    when (type.classifier){
                        Int::class -> json.put(fieldName, value as Int)
                        Long::class -> json.put(fieldName, value as Long)
                        Boolean::class -> json.put(fieldName, value as Boolean)
                        Float::class -> json.put(fieldName, value as Float)
                        Double::class -> json.put(fieldName, value as Double)
                        String::class -> json.put(fieldName, value as String)
                        Calendar::class -> json.put(fieldName, fromDate(value as Calendar))
                        WeightSpecification::class ->
                            json.put(fieldName, fromWeightSpecification((value as WeightSpecification)).toInt())

                        else -> {}
                    }
                }
            }
            json
        } catch (e: Exception) {
            throw RuntimeException("An exception occurred", e)
        }
    }

    fun <T : Any> objectify(json: JSONObject, cls: KClass<T>): T {
        if (cls == Iterable::class || cls == MutableMap::class) throw RuntimeException("Object is a List or Map")
        try {
            val obj = cls.createInstance()

            cls.memberProperties
                .filter { field -> field is KMutableProperty<*> }
                .map { field -> field as KMutableProperty<*> }
                .forEach { field ->
                    val type: KType = field.returnType
                    val fieldName = field.name
                    if (json.has(fieldName)) {
                        val fieldValue = when(type.classifier) {
                            Int::class -> json.getInt(fieldName)
                            Long::class -> json.getLong(fieldName)
                            Boolean::class -> json.getBoolean(fieldName)
                            Float::class -> json.getDouble(fieldName).toFloat()
                            Double::class -> json.getDouble(fieldName)
                            String::class -> json.getString(fieldName)
                            Calendar::class -> toDate(json.getLong(fieldName))
                            WeightSpecification::class -> toWeightSpecification(
                                json.getInt(fieldName).toShort())
                            else -> null
                        }
                        if (fieldValue != null)
                            field.setter.call(obj, fieldValue)
                    }
                }
            return obj
        } catch (e: Exception) {
            throw RuntimeException("An exception occurred", e)
        }
    }

    fun <T> collector(input : List<T>): JSONArray {
        return JSONArray().also {
            input.forEach { obj -> it.put(obj) }
        }
    }

    fun interface JsonConsumer<T> {
        @Throws(JSONException::class)
        fun accept(t: T)
    }

    fun interface JsonFunction<R, T> {
        @Throws(JSONException::class)
        fun call(t: T): R
    }

    @kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.PROPERTY)
    annotation class NoJsonify
}