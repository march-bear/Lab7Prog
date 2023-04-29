package command.implementations

import command.Command
import command.CommandArgument
import command.CommandResult
import exceptions.CancellationException
import iostreamers.Messenger
import iostreamers.TextColor

class HelpCommand (
    private val commandMap: Map<String, String>,
) : Command {
    override val info: String
        get() = "вывести справку по доступным командам"

    override fun execute(args: CommandArgument): CommandResult {
        argumentValidator.check(args)

        var output = ""

        commandMap.forEach {
            output += Messenger.message(String.format("%-40s", "${it.key}:"), TextColor.DEFAULT)
            output += Messenger.message(it.value + "\n", TextColor.BLUE)
        }

        return CommandResult(true, output, false)
    }

    override fun cancel(): String {
        throw CancellationException("Отмена выполнения команды невозможна")
    }
}