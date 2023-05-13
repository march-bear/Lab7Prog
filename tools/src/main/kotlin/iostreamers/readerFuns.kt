package iostreamers

import java.util.InputMismatchException
import kotlin.NoSuchElementException
import kotlin.NumberFormatException

fun Reader.readPort(withZero: Boolean = false): Int? {
    while (true) {
        try {
            val port = this.readString().toInt()
            if (port > 65536 || port < (if (withZero) 0 else 1)) throw NumberFormatException()
            return port
        } catch (ex: NoSuchElementException) {
            return null
        } catch (ex: NumberFormatException) {
            Messenger.inputPrompt("Введите целое число от ${if (withZero) 0 else 1} до 65535", color = TextColor.RED)
        }
    }
}

fun Reader.readHost(): String? {
    return try {
        val host = this.readStringOrNull()
        host ?: "localhost"
    } catch (ex: NoSuchElementException) {
        null
    }
}

fun Reader.readNotEmptyString(): String? {
    while (true) {
        try {
            return readStringOrNull() ?: throw InputMismatchException()
        } catch (ex: InputMismatchException) {
            Messenger.inputPrompt("Ввод не может быть пустым", color = TextColor.RED)
        } catch (ex: NoSuchElementException) {
            return null
        }
    }
}