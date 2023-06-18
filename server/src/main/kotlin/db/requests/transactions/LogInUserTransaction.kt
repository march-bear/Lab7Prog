package db.requests.transactions

import com.google.common.hash.Hashing
import command.CommandArgument
import generateKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import message.DataBaseChanges
import java.sql.Connection

class LogInUserTransaction(
    private val login: String,
    private val passwd: String,
) : AbstractTransaction() {
    override fun execute(conn: Connection): DataBaseChanges {
        val passwdHash = Hashing.sha1().hashString(passwd, charset("UTF-8")).toString()
        conn.autoCommit = false
        val statUserSelect = conn.prepareStatement(
            "SELECT * FROM USERS WHERE login = ? AND passwd_hash = ?"
        )

        statUserSelect.setString(1, login)
        statUserSelect.setString(2, passwdHash)

        val userTuple = statUserSelect.executeQuery()

        if (userTuple.next()) {
            val userId = userTuple.getInt("id")
            val token = Hashing.sha1().hashString(
                Json.encodeToString(Triple(login, passwd, generateKey(10))), charset("UTF-8")
            ).toString()
            val tokenHash = Hashing.sha1().hashString(token, charset("UTF-8")).toString()

            val statTokenInsert = conn.prepareStatement(
                "INSERT INTO TOKENS(user_id, token_hash, valid) VALUES (?, ?, ?) ON CONFLICT DO NOTHING"
            )

            statTokenInsert.setInt(1, userId)
            statTokenInsert.setString(2, tokenHash)
            statTokenInsert.setBoolean(3, true)
            statTokenInsert.execute()

            conn.commit()
            conn.autoCommit = true

            val args = CommandArgument()
            args.setToken(token)
            return DataBaseChanges("TOKENS", -1, listOf(Pair(ChangeType.INSERT, args)))
        }

        conn.autoCommit = true

        return DataBaseChanges("TOKENS", -1, listOf())
    }
}