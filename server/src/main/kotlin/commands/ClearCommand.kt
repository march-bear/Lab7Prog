package commands

import command.ArgumentType
import command.ArgumentValidator
import command.Command
import command.CommandResult
import db.manager.DataBaseManager
import db.manager.checkToken
import db.requests.queries.GetUserIdByTokenQuery
import db.requests.transactions.DeleteAllOrganizationsByIdTransaction
import db.requests.transactions.RemoveByIdTransaction
import message.Request
import message.Response

class ClearCommand(
    private val dbManager: DataBaseManager,
) : Command {

    override val info: String
        get() = "очистить коллекцию"

    override val argumentValidator = ArgumentValidator(listOf(ArgumentType.TOKEN))

    override fun execute(req: Request): CommandResult {
        argumentValidator.check(req.args)

        val errResponse = CommandResult(Response(req.key, false, "Не удалось обработать запрос"))

        if (!(dbManager.checkToken(req.args.token!!) ?: return errResponse)) {
            return CommandResult(Response(req.key, false, "Токен невалиден", "identify"))
        }

        val resUserId = dbManager.execute(GetUserIdByTokenQuery(req.args.token!!)) ?: return errResponse
        if (resUserId.isEmpty()) return errResponse

        val userId = (resUserId[0]["user_id"] as Number).toLong()
        val res = dbManager.execute(DeleteAllOrganizationsByIdTransaction(userId)) ?: return errResponse

        if (res.number == -1L) {
            return CommandResult(Response(req.key, true, "В коллекции нет принадлежащих пользователю организаций"))
        }

        return CommandResult(Response(req.key, true, "Удалено элементов: ${res.changes.size}"), res)
    }
}