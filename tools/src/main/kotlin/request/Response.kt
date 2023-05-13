package request

import kotlinx.serialization.Serializable

/**
 * Дата-класс ответа от сервера
 * @param success - успешность обработки запроса
 * @param message - сообщение ответа
 * @param requestKey - ключ запроса
 * @param necessaryTask - строка с названиями задач (разделены пробелом),
 * которые должен выполнить клиент после получения ответа
 */
@Serializable
data class Response(
    val success: Boolean,
    val message: String,
    val requestKey: String,
    val necessaryTask: String? = null,
)