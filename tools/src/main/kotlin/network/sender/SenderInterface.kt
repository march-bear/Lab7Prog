package network.sender

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import request.Request

fun interface SenderInterface {
    fun send(request: Request)

    companion object {
        fun serialize(msg: Request): String? {
            return try {
                Json.encodeToString(msg)
            } catch (ex: SerializationException) {
                null
            } catch (ex: IllegalArgumentException) {
                null
            }
        }
    }
}