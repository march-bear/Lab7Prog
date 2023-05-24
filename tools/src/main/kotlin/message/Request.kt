package message

import command.CommandArgument
import kotlinx.serialization.Serializable

/**
 * Дата-класс запросов, один из видов сообщений
 * @param name - имя команды для исполнения
 * @param key - ключ для проверки корректности ответа сервера
 * @param args - аргументы для команды
 */

@Serializable
data class Request(
    override val key: String,
    val name: String,
    val args: CommandArgument = CommandArgument(),
) : Message()
