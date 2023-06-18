package commands

import collection.CollectionWrapper
import command.ArgumentType
import command.ArgumentValidator
import command.Command
import command.CommandResult
import db.manager.DataBaseManager
import db.manager.checkToken
import db.requests.queries.GetUserIdByTokenQuery
import organization.Organization
import message.Request
import message.Response

class ShowCommand(
    private val collection: CollectionWrapper<Organization>,
    private val dbManager: DataBaseManager,
) : Command {
    override val info: String
        get() = "вывести в стандартный поток вывода все элементы коллекции в строковом представлении"

    override val argumentValidator: ArgumentValidator = ArgumentValidator(listOf(ArgumentType.TOKEN))

    override fun execute(req: Request): CommandResult {
        argumentValidator.check(req.args)

        var uid: Long? = null
        println(req.args.token)
        if ((dbManager.checkToken(req.args.token!!)
                ?: return CommandResult(Response(req.key, false, "Не удалось обработать запрос")))) {
                val resUid = dbManager.execute(GetUserIdByTokenQuery(req.args.token!!))
                if (!resUid.isNullOrEmpty()) {
                    uid = (resUid[0]["user_id"] as Number).toLong()
                }
        }

        if (collection.isEmpty()) {
            return CommandResult(Response(req.key, true, "Коллекция пуста"))
        }

        var output = "Элементы коллекции:"

        println(uid)
        collection.stream().forEach {
            output += "\n------------------------"
            output += "\n" + it.toString()
            output += "\n\nВладелец: ${if (uid == it.ownerId) "Вы" else "???"}"
            output += "\n------------------------"

        }

        return CommandResult(Response(req.key, true, output))
    }
}