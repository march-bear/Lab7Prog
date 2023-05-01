package command.implementations

import command.Command
import command.CommandResult
import exceptions.CancellationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import request.CommandInfo
import request.Request
import request.Response

class HelpCommand (
    private val commands: List<CommandInfo>,
) : Command {
    override val info: String
        get() = "вывести справку по доступным командам"

    override fun execute(req: Request): Response {
        argumentValidator.check(req.args)

        val output = Json.encodeToString(commands)

        return Response(true, output, req.key)
    }

    override fun cancel(): String {
        throw CancellationException("Отмена выполнения команды невозможна")
    }
}