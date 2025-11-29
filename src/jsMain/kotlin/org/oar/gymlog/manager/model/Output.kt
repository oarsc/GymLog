package org.oar.gymlog.manager.model

import JsDateAsLongSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
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
    val exerciseId: Int,
    val image: String,
    var name: String
)

@Serializable
data class OutputVariation(
    var def: Boolean,
    val exerciseId: Int,
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
    val exerciseId: Int,
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