package message

import kotlinx.serialization.Serializable

/**
 * Дата-класс ответа от сервера
 * @param key - ключ запроса
 * @param success - успешность обработки запроса
 * @param message - сообщение ответа
 * @param necessaryTask - строка с названием задачи,
 * которую должен выполнить клиент после получения ответа
 */

@Serializable
data class Response(
    override val key: String,
    val success: Boolean,
    val message: String,
    val necessaryTask: String? = null,
) : Message()