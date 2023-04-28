package serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonArray
import java.util.*

class LinkedListSerializer<E>(private val elementSerializer: KSerializer<E>) : KSerializer<LinkedList<E>> {
    private val listSerializer = ListSerializer(elementSerializer)
    override val descriptor: SerialDescriptor = listSerializer.descriptor

    override fun serialize(encoder: Encoder, value: LinkedList<E>) {
        listSerializer.serialize(encoder, value.toList())
    }

    override fun deserialize(decoder: Decoder): LinkedList<E> {
        val list = with(decoder as JsonDecoder) {
            decodeJsonElement().jsonArray.mapNotNull {
                try {
                    json.decodeFromJsonElement(elementSerializer, it)
                } catch (e: SerializationException) {
                    e.printStackTrace()
                    null
                }
            }
        }

        return LinkedList(list)
    }
}