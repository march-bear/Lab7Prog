package message

import kotlinx.serialization.Serializable

/**
 * Дата-класс контейнера с запросом, один из видов сообщений.
 *
 * Может быть использован для переадресации запроса от одного сервера другому:
 * поле key будет использоваться для определения пользователя, от которого
 * поступил запрос, на первом сервере после получения ответа от второго.
 */

@Serializable
data class RequestCase(
    override val key: String,
    val task: String,
    val priority: UByte,
    val request: Request,
) : Message(), Comparable<RequestCase> {
    override fun compareTo(other: RequestCase): Int {
        return (this.priority - other.priority).toInt()
    }
}