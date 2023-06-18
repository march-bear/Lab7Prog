package db.requests.queries

import com.google.common.hash.Hashing
import java.sql.Connection

class GetUserIdByTokenQuery(
    private val token: String
) : AbstractQuery() {
    override fun execute(conn: Connection): List<Map<String, Any?>> {
        val tokenHash = Hashing.sha1().hashString(token, charset("UTF-8")).toString()
        val stat = conn.createStatement()
        val tokens = stat.executeQuery(
            "SELECT user_id FROM TOKENS WHERE TOKENS.token_hash = '$tokenHash'"
        )
        val res = resultSetToList(tokens)
        stat.close()
        return res
    }
}