package db.requests.transactions

import ChangeType
import com.google.common.hash.Hashing
import command.CommandArgument
import db.manager.TOKEN_VALIDITY_PERIOD
import message.Infarct
import java.sql.Connection
import java.sql.SQLException
import java.sql.Timestamp

class UpdateTokenInfoTransaction(
    private val token: String,
    private val updateLastUse: Boolean = true
) : AbstractTransaction() {
    override fun execute(conn: Connection): Infarct? {
        conn.autoCommit = false
        val tokenHash = Hashing.sha1().hashString(token, charset("UTF-8")).toString()
        val stat = conn.createStatement()
        val tokens = stat.executeQuery(
            "SELECT user_id FROM TOKENS WHERE TOKENS.token_hash = '$tokenHash'"
        )

        var res = "f"
        if (tokens.next()) {
            if (System.currentTimeMillis() - tokens.getTimestamp("last_use").time < TOKEN_VALIDITY_PERIOD) {
                if (tokens.getBoolean("valid")) {
                    if (updateLastUse) {
                        stat.executeUpdate(
                            "UPDATE TOKENS SET last_use = '${Timestamp(System.currentTimeMillis())}' WHERE TOKENS.token_hash = '$tokenHash'"
                        )
                    }
                    res = "t"
                }
            } else {
                stat.executeUpdate(
                    "UPDATE TOKENS SET valid = 'false' WHERE TOKENS.token_hash = '$tokenHash'"
                )
            }
        }
        try {
            conn.commit()
        } catch (ex: SQLException) {
            conn.autoCommit = false
            return null
        }
        conn.autoCommit = false

        return Infarct("TOKENS", -1, listOf(Pair(ChangeType.UPDATE, CommandArgument(res))))
    }
}