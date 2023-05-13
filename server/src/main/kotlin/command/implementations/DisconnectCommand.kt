package command.implementations

import com.google.common.hash.Hashing
import command.Command
import db.DataBaseManager
import db.checkToken
import request.Request
import request.Response

class DisconnectCommand(
    private val dbManager: DataBaseManager,
) : Command {
    override val info: String
        get() = "выйти из учетной записи"

    private val statTokenUpdate = dbManager.connection.prepareStatement(
        "UPDATE TOKENS SET valid = 'false' WHERE token_hash = ?",
    )

    override fun execute(req: Request): Response {
        argumentValidator.check(req.args)
        return if (!dbManager.checkToken(req.token)) {
            Response(false, "Выход уже был выполнен", req.key, "identify")
        } else {
            val tokenHash = Hashing.sha1().hashString(req.token, charset("UTF-8")).toString()
            statTokenUpdate.setString(1, tokenHash)

            if (statTokenUpdate.executeUpdate() == 1) {
                Response(true, "Выход выполнен", req.key, "identify")
            } else {
                Response(false, "Не удалось выполнить выход", req.key)
            }
        }
    }
}