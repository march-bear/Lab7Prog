package command.implementations

import command.Command
import exceptions.InvalidArgumentsForCommandException
import iostreamers.Messenger
import request.Request
import request.Response

class HackSystemCommand : Command {
    override val info: String
        get() = "взломать систему"

    override fun execute(req: Request): Response {
        try {
            argumentValidator.check(req.args)
        } catch (ex: InvalidArgumentsForCommandException) {
            return Response(
                true,
                "К черту аргументы, чел, введи все нормально",
                req.key,
            )
        }

        return Response(true, "Break a (your) leg, imp!", req.key, "breakALeg")
    }
}