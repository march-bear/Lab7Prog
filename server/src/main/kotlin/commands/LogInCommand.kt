package commands

import command.ArgumentType
import command.ArgumentValidator
import command.Command
import command.CommandResult
import db.manager.DataBaseManager
import db.requests.transactions.LogInUserTransaction
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import message.Request
import message.Response
import java.lang.IllegalArgumentException

class LogInCommand(private val dbManager: DataBaseManager) : Command {
    override val info: String
        get() = "авторизовать пользователя"

    override val argumentValidator: ArgumentValidator = ArgumentValidator(listOf(ArgumentType.STRING))

    override fun execute(req: Request): CommandResult {
        argumentValidator.check(req.args)

        val (login, passwd) = try {
            Json.decodeFromString<Pair<String, String>>(req.args.primArgs[0])
        } catch (ex: SerializationException) {
            return CommandResult(Response(req.key, false, "Некорректный формат отправки логина и пароля", "identify"))
        } catch (ex: IllegalArgumentException) {
            return CommandResult(Response(req.key, false, "Некорректный формат отправки логина и пароля", "identify"))
        }

        val res = dbManager.execute(LogInUserTransaction(login, passwd))
            ?: return CommandResult(Response(req.key, false, "Не удалось произвести авторизацию"))

        if (res.changes.isNotEmpty())
            return CommandResult(Response(req.key, true, res.changes[0].second.token!!, "getCommandInfo"))

        return CommandResult(Response(req.key, false, "Некорректный логин или пароль", "identify"))
    }
}