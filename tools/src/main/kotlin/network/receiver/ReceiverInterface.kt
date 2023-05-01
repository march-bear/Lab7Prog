package network.receiver

import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import request.Response

fun interface ReceiverInterface {
    fun receive(): Response?
    companion object {
        fun deserialize(msg: String): Response? {
            return try {
                Json.decodeFromString<Response>(msg)
            } catch (ex: SerializationException) {
                null
            } catch (ex: IllegalArgumentException) {
                null
            }
        }
    }
}