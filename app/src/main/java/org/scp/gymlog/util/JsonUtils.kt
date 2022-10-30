package org.scp.gymlog.util

import org.json.JSONArray
import org.json.JSONObject
import org.scp.gymlog.model.ExerciseType
import org.scp.gymlog.model.WeightSpecification
import org.scp.gymlog.room.Converters.fromDate
import org.scp.gymlog.room.Converters.fromExerciseType
import org.scp.gymlog.room.Converters.fromWeightSpecification
import org.scp.gymlog.room.Converters.toDate
import org.scp.gymlog.room.Converters.toExerciseType
import org.scp.gymlog.room.Converters.toWeightSpecification
import java.time.LocalDateTime
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KType
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties

object JsonUtils {

    val <T : Any> List<T>.toJsonArray: JSONArray
        get() = JSONArray().also { this.forEach { obj -> it.put(obj) } }

    fun <T> JSONArray.map(function: (JSONArray, Int) -> T): List<T> {
        return mutableListOf<T>().also { list ->
            val length = this.length()
            for (i in 0 until length) {
                list.add(function(this, i))
            }
        }
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
                        LocalDateTime::class -> json.put(fieldName, fromDate(value as LocalDateTime))
                        WeightSpecification::class ->
                            json.put(fieldName, fromWeightSpecification((value as WeightSpecification)).toInt())
                        ExerciseType::class ->
                            json.put(fieldName, fromExerciseType((value as ExerciseType)).toInt())

                        else -> {}
                    }
                }
            }
            json
        } catch (e: Exception) {
            throw RuntimeException("An exception occurred", e)
        }
    }

    fun <T : Any> JSONObject.objectify(cls: KClass<T>): T {
        if (cls == Iterable::class || cls == MutableMap::class) throw RuntimeException("Object is a List or Map")
        try {
            val obj = cls.createInstance()

            cls.memberProperties
                .filter { field -> field is KMutableProperty<*> }
                .map { field -> field as KMutableProperty<*> }
                .forEach { field ->
                    val type: KType = field.returnType
                    val fieldName = field.name
                    if (has(fieldName)) {
                        val fieldValue = when(type.classifier) {
                            Int::class -> getInt(fieldName)
                            Long::class -> getLong(fieldName)
                            Boolean::class -> getBoolean(fieldName)
                            Float::class -> getDouble(fieldName).toFloat()
                            Double::class -> getDouble(fieldName)
                            String::class -> getString(fieldName)
                            LocalDateTime::class -> toDate(getLong(fieldName))
                            WeightSpecification::class -> toWeightSpecification(
                                getInt(fieldName).toShort())
                            ExerciseType::class -> toExerciseType(
                                getInt(fieldName).toShort())
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

    @kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.PROPERTY)
    annotation class NoJsonify
}