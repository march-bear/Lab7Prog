package message

import command.ArgumentType
import kotlinx.serialization.Serializable

/**
 * Дата-класс с информацией о команде
 * @param name - название команды
 * @param info - строка с кратким описанием команды
 * @param args - список аргументов, принимаемых командой
 */
@Serializable
data class CommandInfo(
    val name: String,
    val info: String,
    val args: List<ArgumentType>,
)