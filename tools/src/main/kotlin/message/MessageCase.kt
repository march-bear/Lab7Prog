package message

data class MessageCase(
    override val key: String,
    val message: Message,
) : Message()