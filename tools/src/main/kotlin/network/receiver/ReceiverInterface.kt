package network.receiver

import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import request.Response

fun interface ReceiverInterface {
    fun receive(): String
}