package command.implementations

import command.Command
import command.CommandResult
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
                "Не усложняйте работу команде - она прекрасно взломает систему и без доп. аргументов ;)",
                req.key,
            )
        }
        return Response(true, Messenger.oops(), req.key)
    }

    override fun cancel(): String = "Ну-ну, отменяй"
}