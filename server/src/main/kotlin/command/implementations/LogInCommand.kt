package command.implementations

import command.Command
import request.Request
import request.Response

class LogInCommand : Command {
    override val info: String
        get() = "авторизовать пользователя"

    override fun execute(req: Request): Response {

    }

    override fun cancel(): String {
        TODO("Not yet implemented")
    }
}