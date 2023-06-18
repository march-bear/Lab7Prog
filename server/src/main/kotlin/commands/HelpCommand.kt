package commands

import command.ArgumentType
import command.ArgumentValidator
import command.Command
import command.CommandResult
import db.manager.DataBaseManager
import db.manager.checkToken
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import message.CommandInfo
import message.Request
import message.Response
import java.sql.SQLException

class  HelpCommand (
    private val dbManager: DataBaseManager,
    private val commands: List<CommandInfo>,
) : Command {
    override val info: String
        get() = "вывести справку по доступным командам"

    override val argumentValidator: ArgumentValidator = ArgumentValidator(listOf(ArgumentType.TOKEN))

    override fun execute(req: Request): CommandResult {
        argumentValidator.check(req.args)
        try {
            if (
                !(dbManager.checkToken(req.args.token!!)
                    ?: return CommandResult(Response(req.key, false, "Не удалось обработать запрос")))
            ) {
                return CommandResult(Response(req.key, false, "Требуется авторизация", "identify"))
            }
        } catch (_: SQLException) {
            return CommandResult(Response(req.key, false, "Не удалось обработать запрос"))
        }
        val output = Json.encodeToString(commands)

        return CommandResult(Response(req.key, true, output))
    }
}