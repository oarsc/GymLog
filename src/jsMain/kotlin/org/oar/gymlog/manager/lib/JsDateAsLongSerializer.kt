import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.js.Date

object JsDateAsLongSerializer : KSerializer<Date> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("JsDateAsLong", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: Date): Unit = encoder.encodeLong(value.getTime().toLong())
    override fun deserialize(decoder: Decoder): Date = Date(decoder.decodeLong().toDouble())
}
