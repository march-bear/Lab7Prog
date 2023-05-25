package message.handler

class UnexpectedMessageTypeException(override val message: String?): Exception(message)