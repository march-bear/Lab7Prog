package commands

import command.Command
import command.CommandResult
import exceptions.InvalidArgumentsForCommandException
import message.Request
import message.Response

class HackSystemCommand : Command {
    override val info: String
        get() = "взломать систему"

    override fun execute(req: Request): CommandResult {
        try {
            argumentValidator.check(req.args)
        } catch (ex: InvalidArgumentsForCommandException) {
            return CommandResult(Response(
                req.key,
                true,
                "К черту аргументы, чел, введи все нормально",
            ))
        }

        return CommandResult(Response(req.key, true, "Break a (your) leg, imp!", "breakALeg"))
    }
}