package db.requests.queries

import java.sql.Connection

class GetLastChangesNumberQuery : AbstractQuery() {
    override fun execute(conn: Connection): List<Map<String, Any?>> {
        return resultSetToList(conn.createStatement().executeQuery("SELECT number FROM CHANGES"))
    }
}