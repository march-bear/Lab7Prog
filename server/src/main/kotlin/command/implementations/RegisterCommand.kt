package command.implementations

import command.Command
import db.DataBaseManager
import request.Request
import request.Response

class RegisterCommand(
    private val dbManager: DataBaseManager
) : Command {
    override val info: String
        get() = "зарегистрировать нового пользователя"

    override fun execute(req: Request): Response {
        val login = req.user
        val passwd = req.passwd

        val stat = dbManager.connection.createStatement()

        val matchingLogin = stat.executeQuery(
            "SELECT * FROM USERS WHERE login = $login"
        )

        if (matchingLogin.next()) {
            stat.close()
            return Response(false, "Login busy", req.key)
        }


    }

    override fun cancel(): String {
        TODO("Not yet implemented")
    }
}