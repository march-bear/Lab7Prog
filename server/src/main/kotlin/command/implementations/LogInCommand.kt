package command.implementations

import com.google.common.hash.Hashing
import command.ArgumentType
import command.ArgumentValidator
import command.Command
import db.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import proxy.generateKey
import request.Request
import request.Response
import java.lang.IllegalArgumentException
import java.sql.Timestamp

class LogInCommand(private val dbManager: DataBaseManager) : Command {
    override val info: String
        get() = "авторизовать пользователя"

    override val argumentValidator: ArgumentValidator = ArgumentValidator(listOf(ArgumentType.STRING))

    private val statUserSelect = dbManager.connection.prepareStatement(
        "SELECT * FROM USERS WHERE login = ? AND passwd_hash = ?"
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

        val passwdHash = Hashing.sha1().hashString(passwd, charset("UTF-8")).toString()

        statUserSelect.setString(1, login)
        statUserSelect.setString(2, passwdHash)

        val userTuple = statUserSelect.executeQuery()
        if (userTuple.next()) {
            val userId = userTuple.getInt("id")
            val token = Hashing.sha1().hashString(
                Json.encodeToString(Triple(login, passwd, generateKey(10))), charset("UTF-8")
            ).toString()
            val tokenHash = Hashing.sha1().hashString(token, charset("UTF-8")).toString()

            statTokenInsert.setInt(1, userId)
            statTokenInsert.setString(2, tokenHash)
            statTokenInsert.setBoolean(3, true)
            statTokenInsert.execute()

            return Response(true, token, req.key, "getCommandInfo")
        }

        return Response(false, "Некорректный логин или пароль", req.key, "identify")
    }
}