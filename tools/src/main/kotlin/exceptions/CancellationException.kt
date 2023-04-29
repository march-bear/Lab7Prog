package exceptions

class CancellationException(override val message: String? = "Ошибка во время отмены запросы") : Exception(message)