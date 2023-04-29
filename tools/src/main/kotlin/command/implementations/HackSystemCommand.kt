package command.implementations

import command.Command
import command.CommandArgument
import command.CommandResult
import exceptions.InvalidArgumentsForCommandException
import iostreamers.Messenger

class HackSystemCommand : Command {
    override val info: String
        get() = "взломать систему"

    override fun execute(args: CommandArgument): CommandResult {
        try {
            argumentValidator.check(args)
        } catch (ex: InvalidArgumentsForCommandException) {
            return CommandResult(
                true,
                "Не усложняйте работу команде - она прекрасно взломает систему и без доп. аргументов ;)",
                false,
            )
        }
        return CommandResult(true, Messenger.oops(), false)
    }

    override fun cancel(): String = "Ну-ну, отменяй"
}