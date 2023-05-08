package serverworker

import iostreamers.Messenger
import iostreamers.TextColor
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LoggerWrapper(private val log: Logger) {
    fun warn(msg: String) {
        Messenger.print(msg, TextColor.YELLOW)
        log.warn(msg)
    }

    fun error(msg: String) {
        Messenger.print(msg, TextColor.RED)
        log.error(msg)
    }

    fun info(msg: String) {
        Messenger.print(msg)
        log.info(msg)
    }
}