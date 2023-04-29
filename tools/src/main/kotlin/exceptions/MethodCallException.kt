package exceptions

class MethodCallException(override val message: String? = "Ошибка во время вызова метода"): Exception(message)