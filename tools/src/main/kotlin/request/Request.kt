package request

import command.CommandArgument
import kotlinx.serialization.Serializable

@Serializable
data class Request(
    val name: String,
    val key: String,
    val args: CommandArgument = CommandArgument(),
    val user: String = "",
    val passwd: String = "",
)
