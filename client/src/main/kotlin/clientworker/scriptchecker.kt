package clientworker

import OrganizationFactory
import command.ArgumentType
import command.CommandArgument
import exceptions.InvalidFieldValueException
import exceptions.ScriptException
import iostreamers.Reader
import message.CommandInfo
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.util.*

fun addCommandsFromFile(
    fileName: String,
    requestList: MutableList<Pair<String, CommandArgument>>,
    scriptFiles: LinkedList<String>,
    commandList: List<CommandInfo>,
) {
    if (fileName in scriptFiles) {
        var message = "Обнаружен циклический вызов скрипта:"

        for (i in scriptFiles.indexOf(fileName) until scriptFiles.size)
            message += "\n ${scriptFiles[i]} ->"
        throw ScriptException("$message $fileName")
    }

    scriptFiles.add(fileName)
    val script: String
    try {
        val inputStreamReader = InputStreamReader(FileInputStream(fileName))
        script = inputStreamReader.readText()
        inputStreamReader.close()
    } catch (ex: FileNotFoundException) {
        throw ScriptException("Ошибка во время открытия файла $fileName: ${ex.message}")
    }
    val reader = Reader(Scanner(script))
    var commandData = reader.readCommand()

    while (commandData != null) {
        val (commandName, commandArguments) = commandData
        val commandInfo = commandList.find { it.name == commandName }
            ?: listOf(
                CommandInfo("exit", "", listOf()),
                CommandInfo("help", "", listOf()),
                CommandInfo("execute_script", "", listOf())
            ).find { it.name == commandName }
            ?: throwNestedScriptException(fileName, reader.lineCounter, "$commandName: команда не найдена")

        if (ArgumentType.ORGANIZATION in commandInfo.args) {
            try {
                commandArguments.setOrganization(OrganizationFactory(reader).newOrganizationFromInput())
            } catch (ex: InvalidFieldValueException) {
                throwNestedScriptException(
                    fileName, reader.lineCounter,
                    "Ошибка во время считывания аргумента для команды"
                )
            }
        }
        if (commandInfo.name == "execute_script") {
            if (scriptFiles.size > MAXIMUM_NESTED_SCRIPTS_CALLS)
                throwNestedScriptException(
                    fileName, reader.lineCounter,
                    "Превышено максимальное количество вложенных вызовов скриптов: $MAXIMUM_NESTED_SCRIPTS_CALLS"
                )
            try {
                addCommandsFromFile(commandArguments.primArgs[0], requestList, scriptFiles, commandList)
            } catch (ex: FileNotFoundException) {
                throwNestedScriptException(fileName, reader.lineCounter, ex.message)
            } catch (ex: ScriptException) {
                throwNestedScriptException(fileName, reader.lineCounter, ex.message)
            }
        } else {
            requestList.add(Pair(commandName, commandArguments))
        }
        commandData = reader.readCommand()
    }

    scriptFiles.pop()
}

const val MAXIMUM_NESTED_SCRIPTS_CALLS: Int = 10

fun throwNestedScriptException(
    fileName: String,
    lineNumber: ULong,
    message: String?
): Nothing {
    throw ScriptException("${message}\n<- Ошибка во время проверки скрипта $fileName, строка $lineNumber")
}