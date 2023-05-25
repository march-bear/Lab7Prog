package message

import command.CommandArgument
import kotlinx.serialization.Serializable

/**
 * Instructions For UPdate The Collection
 * Дата-класс с информацией об изменениях, которые были произведены с кеш-коллекцией
 *
 * @param number - глобальный номер изменения
 * @param changes - список изменений
 */

@Serializable
data class Infarct(
    override val key: String,
    val number: Long,
    val changes: List<Pair<String, CommandArgument>>,
) : Message()