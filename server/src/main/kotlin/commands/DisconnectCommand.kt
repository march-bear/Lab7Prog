package commands

import command.ArgumentType
import command.ArgumentValidator
import command.Command
import command.CommandResult
import db.manager.DataBaseManager
import db.manager.checkToken
import db.requests.transactions.InvalidateTokenTransaction
import message.Request
import message.Response

class DisconnectCommand(
    private val dbManager: DataBaseManager,
) : Command {
    override val info: String
        get() = "выйти из учетной записи"


    override val argumentValidator: ArgumentValidator = ArgumentValidator(listOf(ArgumentType.TOKEN))

    override fun execute(req: Request): CommandResult {
        argumentValidator.check(req.args)
        if (!(dbManager.checkToken(req.args.token!!)
                ?: return CommandResult(Response(req.key, false, "Не удалось обработать запрос")))) {
                    return CommandResult(Response(req.key, false, "Выход уже был выполнен", "identify"))
        }

        val res = dbManager.execute(InvalidateTokenTransaction(req.args.token!!))

        return if (res == null || res.changes.isEmpty())
            CommandResult(Response(req.key, false, "Не удалось обработать запрос"))
        else
            CommandResult(Response(req.key, true, "Выход выполнен"))
    }
}