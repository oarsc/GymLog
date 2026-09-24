package org.oar.gymlog.manager.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.oar.lib.JsDateAsLongSerializer
import kotlin.js.Date

@Serializable
data class Output(
    var version: Int = 1,
    var notes: MutableList<String>,
    val prefs: OutputPreferences,
    val gyms: List<String>,
    val exercises: MutableList<OutputExercise>,
    val variations: MutableList<OutputVariation>,
    val primaries: MutableList<OutputMuscleRelation>,
    val secondaries: MutableList<OutputMuscleRelation>,
    val trainings: MutableList<OutputTraining>,
    val workouts: MutableList<OutputWorkout>,
    val workoutExercises: MutableList<OutputWorkoutExercise>,
    val workoutSets: MutableList<OutputWorkoutSet>,
    @Serializable(with = OutputWeightMapSerializer::class) val weights: MutableList<OutputWeight>,
    val weightPeriods: MutableList<OutputWeightPeriod> = mutableListOf(),
    val weightPeriodModifications: MutableList<OutputWeightPeriodModification> = mutableListOf(),
    val bits: MutableList<OutputBit>,

    @Transient
    var training: MutableMap<Int, OutputTraining> = mutableMapOf(),
    @Transient
    var variation: MutableMap<Int, Pair<OutputExercise, OutputVariation>> = mutableMapOf(),
    @Transient
    var exercise: MutableMap<Int, OutputExercise> = mutableMapOf(),
    @Transient
    var note: MutableMap<Int, String> = mutableMapOf(),
    @Transient
    var muscles: MutableMap<Int, Muscle> = mutableMapOf(),
    @Transient
    var workout: MutableMap<Int, OutputWorkout> = mutableMapOf(),
    @Transient
    var weightPeriod: MutableMap<Int, OutputWeightPeriod> = mutableMapOf()
)

@Serializable
data class OutputPreferences(
    val logDeletion: String? = null,
    val exercisesSortLastUsed: ExercisesSortLastUsed? = null,
    val nightTheme: Boolean? = null,
    val restTime: String? = null,
    val internationalSystem: Boolean? = null,
    val exerciseDeletion: String? = null,
    val trainingBitsSort: String? = null,
    val gym: Int? = null,
    val conversionExactValue: Boolean? = null,
    val conversionStep: String? = null
)

@Serializable
data class OutputExercise(
    var exerciseId: Int,
    var image: String,
    var name: String
)

@Serializable
data class OutputVariation(
    var def: Boolean,
    var exerciseId: Int,
    var gymId: Int? = null,
    var gymRelation: GymRelation,
    var lastBarId: BarId? = null,
    var lastRestTime: Int,
    var lastStep: Step,
    var lastWeightSpec: WeightSpec,
    var name: String,
    var type: ExerciseType,
    var variationId: Int
)

@Serializable
data class OutputMuscleRelation(
    var exerciseId: Int,
    var muscleId: Int
)

@Serializable
data class OutputTraining(
    @Serializable(with = JsDateAsLongSerializer::class) var end: Date,
    val gymId: Int,
    @Serializable(with = JsDateAsLongSerializer::class) var start: Date,
    val trainingId: Int,
    @SerialName("n") var note: Int? = null,
)

@Serializable
data class OutputBit(
    var kilos: Boolean? = null,
    var instant: Boolean? = null,
    var reps: Int,
    var superSet: Int? = null,
    @SerialName("s") @Serializable(with = JsDateAsLongSerializer::class) var timestamp: Date,
    @SerialName("w") var totalWeight: Double,
    @SerialName("t") var trainingId: Int,
    @SerialName("v") var variationId: Int,
    @SerialName("n") var note: Int? = null
)

@Serializable
data class OutputWorkout(
    var workoutId: Int,
    var name: String,
    var color: String
)

@Serializable
data class OutputWorkoutExercise(
    var workoutExerciseId: Int,
    var workoutId: Int,
    var variationId: Int,
    var superSet: Int
)

@Serializable
data class OutputWorkoutSet(
    var workoutSetId: Int,
    var workoutExerciseId: Int,
    var order: Int,
    var totalWeight: Int,
    var kilos: Boolean,
    var reps: Int,
    var note: String,
    var restTime: Int,
    var warmUp: Boolean
)

@Serializable
data class OutputWeight(
    @Serializable(with = JsDateAsYyyyMmDdSerializer::class) var date: Date,
    var weight: Int,
    var kilos: Boolean
)


@Serializable
data class OutputWeightPeriod(
    var weightPeriodId: Int,
    @Serializable(with = JsDateAsYyyyMmDdSerializer::class) var start: Date,
    @Serializable(with = JsDateAsYyyyMmDdSerializer::class) var end: Date,
    var initialWeight: Int,
    var gainGramsPerWeek: Int,
    var lossGramsPerWeek: Int,
    var expectedMuscleGain: Int,
    var toleranceGrams: Int
)

@Serializable
data class OutputWeightPeriodModification(
    var weightPeriodModificationId: Int,
    var weightPeriodId: Int,
    @Serializable(with = JsDateAsYyyyMmDdSerializer::class) var start: Date,
    @Serializable(with = JsDateAsYyyyMmDdSerializer::class) var end: Date,
    var gramsPerWeek: Int
)

@Serializable
enum class ExercisesSortLastUsed {
    @SerialName("alphabetically") ALPHABETICALLY,
    @SerialName("last_used") LAST_USED
}


@Serializable(with = GymRelationSerializer::class)
enum class GymRelation {
    NO_RELATION, INDIVIDUAL_RELATION, STRICT_RELATION;
}

@Serializable(with = ExerciseTypeSerializer::class)
enum class ExerciseType {
    NONE, DUMBBELL, BARBELL, PLATE, PULLEY_MACHINE, SMITH_MACHINE, MACHINE, CARDIO
}

@Serializable(with = BarIdSerializer::class)
enum class BarId(val weight: Int) {
    NONE(0),
    BAR_7_5(750),
    BAR_10(1000),
    BAR_12(1200),
    BAR_15(1500),
    BAR_20(2000),
    BAR_25(2500);

    override fun toString(): String = if (weight == 0) "No bar" else "${weight / 100.0}kg"
}

@Serializable(with = WeightSpecSerializer::class)
enum class WeightSpec {
    TOTAL_WEIGHT, NO_BAR_WEIGHT, ONE_SIDE_WEIGHT
}

@Serializable(with = StepSerializer::class)
enum class Step(val weightStep: Int) {
    STEP_0_5(50),
    STEP_1(100),
    STEP_1_25(125),
    STEP_2(200),
    STEP_2_5(250),
    STEP_5(500),
    STEP_10(1000),
    STEP_15(1500),
    STEP_20(2000),
    STEP_25(2500);

    override fun toString(): String = "${(weightStep / 100.0).asDynamic().toFixed(2)}kg"
}

object GymRelationSerializer: EnumSerializer<GymRelation>(GymRelation.entries, "GymRelationSerializer")
object ExerciseTypeSerializer: EnumSerializer<ExerciseType>(ExerciseType.entries, "ExerciseTypeSerializer")
object BarIdSerializer: EnumSerializer<BarId>(BarId.entries, "BarIdSerializer")
object WeightSpecSerializer: EnumSerializer<WeightSpec>(WeightSpec.entries, "WeightSpecSerializer")

open class EnumSerializer<T : Enum<T>>(private val list: List<T>, serialName: String) : KSerializer<T> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(serialName, PrimitiveKind.INT)
    override fun serialize(encoder: Encoder, value: T): Unit = encoder.encodeInt(list.indexOf(value))
    override fun deserialize(decoder: Decoder): T = list[decoder.decodeInt()]
}

class StepSerializer : KSerializer<Step> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("StepSerializer", PrimitiveKind.INT)
    override fun serialize(encoder: Encoder, value: Step) = encoder.encodeInt(value.weightStep)
    override fun deserialize(decoder: Decoder): Step = decoder.decodeInt()
        .let { value -> Step.entries.first { it.weightStep == value } }
}

object JsDateAsYyyyMmDdSerializer : KSerializer<Date> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("JsDateAsYyyyMmDdSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Date) {
        val year = value.getFullYear()
        val month = (value.getMonth() + 1).toString().padStart(2, '0')
        val day = value.getDate().toString().padStart(2, '0')
        encoder.encodeString("$year$month$day")
    }

    override fun deserialize(decoder: Decoder): Date {
        val dateString = decoder.decodeString()
        val year = dateString.substring(0, 4).toInt()
        val month = dateString.substring(4, 6).toInt() - 1
        val day = dateString.substring(6, 8).toInt()
        return Date(year, month, day)
    }
}

object OutputWeightMapSerializer : KSerializer<MutableList<OutputWeight>> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("OutputWeightMapSerializer")

    override fun serialize(encoder: Encoder, value: MutableList<OutputWeight>) {
        val jsonEncoder = encoder as? JsonEncoder ?: error("Expected JsonEncoder")
        val jsonObject = buildJsonObject {
            value.forEach { weight ->
                val year = weight.date.getFullYear()
                val month = (weight.date.getMonth() + 1).toString().padStart(2, '0')
                val day = weight.date.getDate().toString().padStart(2, '0')
                val dateKey = "$year$month$day"
                val weightValue = "${weight.weight}${if (weight.kilos) "K" else "L"}"
                put(dateKey, weightValue)
            }
        }
        jsonEncoder.encodeJsonElement(jsonObject)
    }

    override fun deserialize(decoder: Decoder): MutableList<OutputWeight> {
        val jsonDecoder = decoder as? JsonDecoder ?: error("Expected JsonDecoder")
        val jsonObject = jsonDecoder.decodeJsonElement() as? JsonObject ?: error("Expected JsonObject")

        return jsonObject.entries
            .map { (dateString, jsonElement) ->
                val weightString = (jsonElement as? JsonPrimitive)?.content ?: error("Expected JsonPrimitive")

                val year = dateString.substring(0, 4).toInt()
                val month = dateString.substring(4, 6).toInt() - 1
                val day = dateString.substring(6, 8).toInt()

                OutputWeight(
                    date = Date(year, month, day),
                    weight = weightString.dropLast(1).toInt(),
                    kilos = weightString.last() == 'K'
                )
            }
            .sortedBy { it.date.getTime() }
            .toMutableList()
    }
}