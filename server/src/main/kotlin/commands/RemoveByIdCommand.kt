package commands

import collection.CollectionWrapper
import organization.Organization
import command.*
import db.manager.DataBaseManager
import db.manager.checkToken
import db.requests.queries.GetUserIdByTokenQuery
import db.requests.transactions.RemoveByIdTransaction
import message.Request
import message.Response

class RemoveByIdCommand(
    private val dbManager: DataBaseManager,
) : Command {
    override val info: String
        get() = "удалить элемент из коллекции по его id (id указывается после имени команды)"
    override val argumentValidator = ArgumentValidator(listOf(ArgumentType.LONG, ArgumentType.TOKEN))

    override fun execute(req: Request): CommandResult {
        val error = CommandResult(Response(req.key, false, "Не удалось обработать запрос"))
        argumentValidator.check(req.args)
        if (!(dbManager.checkToken(req.args.token!!) ?: return error)) {
            return CommandResult(Response(req.key, false, "Токен некорректен", "identify"))
        }
        val id: Long = req.args.primArgs[0].toLong()

        if (!Organization.idIsValid(id))
            return CommandResult(Response(req.key, false, "Введенное значение не является id"))

        val resUserId = dbManager.execute(GetUserIdByTokenQuery(req.args.token!!)) ?: return error
        if (resUserId.isEmpty()) return error

        val userId = (resUserId[0]["user_id"] as Number).toLong()
        val res = dbManager.execute(RemoveByIdTransaction(id, userId)) ?: return error


        return if (res.changes.isNotEmpty()) {
            CommandResult(Response(req.key, true, "Элемент удален"), res)
        } else {
            CommandResult(Response(req.key, false, "Элемент с id $id принадлежит другому юзеру или не существует"))
        }
    }
}