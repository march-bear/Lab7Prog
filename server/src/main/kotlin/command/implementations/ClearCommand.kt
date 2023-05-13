package command.implementations

import command.Command
import db.DataBaseManager
import request.Request
import request.Response

class ClearCommand(
    private val dbManager: DataBaseManager,
) : Command {

    override val info: String
        get() = "очистить коллекцию"

    private val statAllDelete = dbManager.connection.prepareStatement(
        "DELETE FROM TABLE ORGANIZATIONS WHERE owner_id = ?"
    )

    private val statUserIdByTokenSelect = dbManager.connection.prepareStatement(
        "SELECT * FROM USERS JOIN TOKENS ON USERS.id = TOKENS.user_id and TOKENS.token_hash = ?"
    )

    override fun execute(req: Request): Response {
        argumentValidator.check(req.args)



        return Response(true, "Удалено элементов: d", req.key)
    }
}