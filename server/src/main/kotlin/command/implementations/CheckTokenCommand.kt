package command.implementations

import command.Command
import db.DataBaseManager
import db.checkToken
import request.Request
import request.Response

class CheckTokenCommand(
    private val dbManager: DataBaseManager,
) : Command {
    override val info: String
        get() = "проверить корректность токена"

    override fun execute(req: Request): Response {
        return if (dbManager.checkToken(req.token)) {
            Response(true, "Токен корректен", req.key, "getCommandInfo")
        } else {
            Response(false, "Токен недействителен", req.key, "identify")
        }
    }
}