package db.requests.transactions

import com.google.common.hash.Hashing
import command.CommandArgument
import generateKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import message.DataBaseChanges
import java.sql.Connection

class RegisterUserTransaction(
    private val login: String,
    private val passwd: String,
) : AbstractTransaction() {
    override fun execute(conn: Connection): DataBaseChanges {
        conn.autoCommit = false
        val savepoint = conn.setSavepoint()
        val statLoginSelect = conn.prepareStatement(
            "SELECT * FROM USERS WHERE login = ?"
        )

        val statUserInsert = conn.prepareStatement(
            "INSERT INTO USERS(login, passwd_hash) VALUES (?, ?) ON CONFLICT DO NOTHING"
        )

        val statTokenInsert = conn.prepareStatement(
            "INSERT INTO TOKENS(user_id, token_hash, valid) VALUES (?, ?, ?) ON CONFLICT DO NOTHING"
        )

        statLoginSelect.setString(1, login)

        if (statLoginSelect.executeQuery().next()) {
            return DataBaseChanges("USERS", -1, listOf())
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

            conn.commit()

            val args = CommandArgument()
            args.setToken(token)
            DataBaseChanges("TOKENS", -1, listOf(Pair(ChangeType.INSERT, args)))

        } else {
            conn.releaseSavepoint(savepoint)
            DataBaseChanges("TOKENS", -1, listOf())
        }
    }
}