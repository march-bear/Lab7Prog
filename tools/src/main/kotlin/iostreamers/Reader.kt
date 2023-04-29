package iostreamers

import command.CommandArgument
import command.CommandData
import java.util.*
import java.util.regex.Pattern

/**
 * Класс для считывания данных с входного потока
 */
class Reader(private val sc: Scanner = Scanner(System.`in`)) {
    var lineCounter: ULong = 0UL
        private set

    fun readStringOrNull(): String? {
        val input = sc.nextLine().trim()
        lineCounter++
        return if (input != "") input else null
    }
    fun readString(): String {
        lineCounter++
        return sc.nextLine().trim()
    }

    fun readCommand(): CommandData? {
        if (!sc.hasNextLine())
            return null
        val commandList = readString().split(Pattern.compile("\\s+"), limit = 2)
        return CommandData(commandList[0], CommandArgument(if (commandList.size == 2) commandList[1] else null))
    }
}
