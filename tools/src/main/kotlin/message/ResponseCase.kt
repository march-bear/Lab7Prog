package message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Дата-класс контейнера с ответом, один из видов сообщений.
 *
 * Может быть использован для отправки ответа с сервера на переадресованный
 * от другого сервера запрос: поле key будет использоваться для определения
 * пользователя, от которого поступил запрос, на первом сервере после получения
 * ответа от второго.
 */

@Serializable
@SerialName("responseCase")
data class ResponseCase(
    override val key: String,
    val response: Response,
    val necessaryTask: String? = null,
) : Message()