package request

import command.CommandArgument
import kotlinx.serialization.Serializable

/**
 * Дата-класс запросов
 * @param name - имя команды для исполнения
 * @param key - ключ для проверки корректности ответа сервера
 * @param args - аргументы для команды
 * @param token - токен
 */
@Serializable
data class Request(
    val name: String,
    val key: String,
    val args: CommandArgument = CommandArgument(),
    val token: String = ""
)
