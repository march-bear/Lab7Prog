package command.implementations

import command.Command
import db.DataBaseManager
import db.checkToken
import db.getUserByToken
import request.Request
import request.Response

class ClearCommand(
    private val dbManager: DataBaseManager,
) : Command {

    override val info: String
        get() = "очистить коллекцию"

    private val statAllDelete = dbManager.connection.prepareStatement(
        "DELETE FROM ORGANIZATIONS WHERE owner_id = ?"
    )

    override fun execute(req: Request): Response {
        argumentValidator.check(req.args)
        if (!dbManager.checkToken(req.token)) {
            return Response(false, "Токен невалиден", req.key, "identify")
        }

        val userId = dbManager.getUserByToken(req.token)
            ?: return Response(false, "Токен невалиден", req.key, "identify")

        statAllDelete.setInt(1, userId)

        return Response(true, "Удалено элементов: ${statAllDelete.executeUpdate()}", req.key)
    }
}