package db.requests.queries

import com.google.common.hash.Hashing
import java.sql.Connection
import java.sql.ResultSet

class GetUserIdByTokenQuery(
    private val token: String
) : AbstractQuery() {
    override fun execute(conn: Connection): ResultSet {
        val tokenHash = Hashing.sha1().hashString(token, charset("UTF-8")).toString()
        val stat = conn.createStatement()
        val tokens = stat.executeQuery(
            "SELECT user_id FROM TOKENS WHERE TOKENS.token_hash = '$tokenHash'"
        )
        stat.close()
        return tokens
    }
}