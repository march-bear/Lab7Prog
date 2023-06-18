package db.requests.queries

import java.sql.Connection

class SelectAllFromCoordinatesQuery : AbstractQuery() {
    override fun execute(conn: Connection): List<Map<String, Any?>> {
        return resultSetToList(conn.createStatement().executeQuery("SELECT * FROM COORDINATES"))
    }
}