package command.implementations

import command.ArgumentType
import command.ArgumentValidator
import command.Command
import db.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import request.Request
import request.Response
import java.lang.IllegalArgumentException

class LogInCommand(private val dbManager: DataBaseManager) : Command {
    override val info: String
        get() = "авторизовать пользователя"

    override val argumentValidator: ArgumentValidator = ArgumentValidator(listOf(ArgumentType.STRING))

    override fun execute(req: Request): Response {
        argumentValidator.check(req.args)

        val (login, passwd) = try {
            Json.decodeFromString<Pair<String, String>>(req.args.primArgs[0])
        } catch (ex: SerializationException) {
            return Response(false, "Некорректный формат отправки логина и пароля", req.key, "identify")
        } catch (ex: IllegalArgumentException) {
            return Response(false, "Некорректный формат отправки логина и пароля", req.key, "identify")
        }



        return Response(false, "Не, пока низя", req.key)
    }
}