package command.implementations

import com.google.common.hash.Hashing
import command.ArgumentType
import command.ArgumentValidator
import command.Command
import db.DataBaseManager
import db.checkLogin
import db.checkPasswd
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import proxy.generateKey
import request.Request
import request.Response
import java.lang.IllegalArgumentException

class RegisterCommand(
    private val dbManager: DataBaseManager,
) : Command {
    override val info: String
        get() = "зарегистрироваться"

    override val argumentValidator: ArgumentValidator = ArgumentValidator(listOf(ArgumentType.STRING))

    private val statLoginSelect = dbManager.connection.prepareStatement(
        "SELECT * FROM USERS WHERE login = ?"
    )

    private val statUserInsert = dbManager.connection.prepareStatement(
        "INSERT INTO USERS(login, passwd_hash) VALUES (?, ?) ON CONFLICT DO NOTHING"
    )

    private val statTokenInsert = dbManager.connection.prepareStatement(
        "INSERT INTO TOKENS(user_id, token_hash, valid) VALUES (?, ?, ?) ON CONFLICT DO NOTHING"
    )

    override fun execute(req: Request): Response {
        argumentValidator.check(req.args)
        val (login, passwd) = try {
            Json.decodeFromString<Pair<String, String>>(req.args.primArgs[0])
        } catch (ex: SerializationException) {
            return Response(false, "Некорректный формат отправки логина и пароля", req.key, "identify")
        } catch (ex: IllegalArgumentException) {
            return Response(false, "Некорректный формат отправки логина и пароля", req.key, "identify")
        }

        statLoginSelect.setString(1, login)

        if (!checkLogin(login))
            return Response(false,
                "Логин не подходит по одному или нескольким требованиям:\n" +
                        "1) длина от 1 до 24\n" +
                        "2) отсутствие пробельных символов", req.key, "identify"
            )

        if (!checkPasswd(passwd))
            return Response(false,
                "Пароль не подходит по одному или нескольким требованиям:\n" +
                        "1) длина от 8 до 24\n" +
                        "2) отсутствие пробельных символов\n" +
                        "3) хотя бы половина символов уникальна", req.key, "identify"
            )

        if (statLoginSelect.executeQuery().next()) {
            return Response(false, "Логин занят", req.key, "identify")
        }

        val passwdHash = Hashing.sha1().hashString(passwd, charset("UTF-8")).toString()

        statUserInsert.setString(1, login)
        statUserInsert.setString(2, passwdHash)

        return if (statUserInsert.executeUpdate() != 0) {
            val token = Hashing.sha1().hashString(
                Json.encodeToString(Triple(login, passwd, generateKey(10))), charset("UTF-8")
            ).toString()
            val tokenHash = Hashing.sha1().hashString(token, charset("UTF-8")).toString()
            var userId = 1
            val userTuple = statLoginSelect.executeQuery()
            if (userTuple.next()) userId = userTuple.getInt("id")

            statTokenInsert.setInt(1, userId)
            statTokenInsert.setString(2, tokenHash)
            statTokenInsert.setBoolean(3, true)
            statTokenInsert.execute()

            Response(true, token, req.key, "getCommandInfo")
        } else {
            Response(false, "Не удалось произвести регистрацию. Повторите попытку", req.key, "register")
        }
    }
}