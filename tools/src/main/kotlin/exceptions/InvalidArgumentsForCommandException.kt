package exceptions

class InvalidArgumentsForCommandException(message: String = "Недопустимые значения аргументов для команды") :
    Exception(message)