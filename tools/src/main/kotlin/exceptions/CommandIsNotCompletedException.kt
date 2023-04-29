package exceptions

class CommandIsNotCompletedException(
    override val message: String? = "Команда не была выполнена"
) : Exception(message)