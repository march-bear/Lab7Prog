package message

import kotlinx.serialization.Serializable

@Serializable
data class MessageCase(
    override val key: String,
    val message: Message,
) : Message()