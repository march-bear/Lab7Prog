package db.requests.transactions

import com.google.common.hash.Hashing
import command.CommandArgument
import message.DataBaseChanges
import java.sql.Connection

class InvalidateTokenTransaction(
    private val token: String,
) : AbstractTransaction() {
    override fun execute(conn: Connection): DataBaseChanges {
        conn.autoCommit = false

        val statTokenUpdate = conn.prepareStatement(
            "UPDATE TOKENS SET valid = 'false' WHERE token_hash = ?",
        )

        val tokenHash = Hashing.sha1().hashString(token, charset("UTF-8")).toString()
        statTokenUpdate.setString(1, tokenHash)
        val res = statTokenUpdate.executeUpdate()
        conn.commit()

        return if (res == 1) {
            DataBaseChanges("TOKENS", -1, listOf(Pair(ChangeType.UPDATE, CommandArgument("false"))))
        } else {
            DataBaseChanges("TOKENS", -1, listOf())
        }
    }
}