package request

import command.ArgumentType
import kotlinx.serialization.Serializable

@Serializable
data class CommandInfo(
    val name: String,
    val args: List<ArgumentType>,
)