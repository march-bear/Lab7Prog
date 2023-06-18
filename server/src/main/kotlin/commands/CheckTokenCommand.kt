package commands

import command.ArgumentType
import command.ArgumentValidator
import command.Command
import command.CommandResult
import db.manager.DataBaseManager
import db.manager.checkToken
import message.Request
import message.Response

class CheckTokenCommand(
    private val dbManager: DataBaseManager,
) : Command {
    override val info: String
        get() = "проверить корректность токена"

    override val argumentValidator: ArgumentValidator = ArgumentValidator(listOf(ArgumentType.TOKEN))

    override fun execute(req: Request): CommandResult {
        argumentValidator.check(req.args)
        return if (
            dbManager.checkToken(req.args.token!!)
                ?: return CommandResult(Response(req.key, false, "Не удалось обработать запрос"))
        ) {
            CommandResult(Response(req.key, true, "Токен корректен"))
        } else {
            CommandResult(Response(req.key, false, "Токен недействителен", "identify"))
        }
    }
}