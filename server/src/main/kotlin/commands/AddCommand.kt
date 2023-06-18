package commands

import command.*
import db.manager.DataBaseManager
import db.manager.checkToken
import db.requests.queries.GetUserIdByTokenQuery
import db.requests.transactions.InsertOrganizationTransaction
import message.Request
import message.Response
import java.sql.SQLException

class AddCommand(
    private val dbManager: DataBaseManager
) : Command {
    override val info: String
        get() = "добавить новый элемент в коллекцию (поля элемента указываются на отдельных строках)"

    override val argumentValidator = ArgumentValidator(listOf(ArgumentType.ORGANIZATION, ArgumentType.TOKEN))

    override fun execute(req: Request): CommandResult {
        val errorResult = CommandResult(Response(req.key, false, "Не удалось обработать запрос"))

        argumentValidator.check(req.args)
        if (!(dbManager.checkToken(req.args.token!!)
                ?: return errorResult))
            return CommandResult(Response(req.key, false, "Токен некорректен", "identify"))

        val getUserIdRes = dbManager.execute(GetUserIdByTokenQuery(req.args.token!!)) ?: return errorResult
        if (getUserIdRes.isEmpty()) return errorResult
        val userId: Long

        try {
            userId = getUserIdRes[0]["user_id"] as Long? ?: return errorResult
        } catch (_: SQLException) {
            return errorResult
        }

        val res = dbManager.execute(InsertOrganizationTransaction(req.args.organization!!, userId)) ?: return errorResult

        return CommandResult(
            Response(req.key, true, "Элемент добавлен с id=${res.changes[0].second.organization!!.id}"),
            res,
        )
    }
}