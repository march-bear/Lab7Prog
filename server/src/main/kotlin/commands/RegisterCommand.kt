package commands

import command.ArgumentType
import command.ArgumentValidator
import command.Command
import command.CommandResult
import db.manager.DataBaseManager
import db.manager.checkLogin
import db.manager.checkPasswd
import db.requests.transactions.RegisterUserTransaction
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import message.Request
import message.Response

class RegisterCommand(
    private val dbManager: DataBaseManager,
) : Command {
    override val info: String
        get() = "зарегистрироваться"

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


        if (!checkLogin(login))
        return CommandResult(
            Response(req.key, false,
            "Логин не подходит по одному или нескольким требованиям:\n" +
                    "1) длина от 1 до 24\n" +
                    "2) отсутствие пробельных символов", "identify"
            )
        )

        if (!checkPasswd(passwd))
            return CommandResult(
                Response(req.key, false,
                "Пароль не подходит по одному или нескольким требованиям:\n" +
                        "1) длина от 8 до 24\n" +
                        "2) отсутствие пробельных символов\n" +
                        "3) хотя бы половина символов уникальна", "identify"
                )
            )
        val res = dbManager.execute(RegisterUserTransaction(login, passwd))
            ?: return CommandResult(Response(req.key, false, "Не удалось обработать запрос"))
        return if (res.changes.isNotEmpty()) {
            CommandResult(Response(req.key, true, res.changes[0].second.token!!, "getCommandInfo"))
        } else {
            CommandResult(Response(req.key, false, "Логин занят", "identify"))
        }
    }
}