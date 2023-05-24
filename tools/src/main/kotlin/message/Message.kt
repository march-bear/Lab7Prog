package message

import kotlinx.serialization.Serializable

/**
 * Абстрактный класс всех сообщений, передаваемых между сервером и клиентом
 */

@Serializable
sealed class Message {
    abstract val key: String
}