package db.requests.queries

import java.sql.Connection

class SelectAllFromOrganizationTypesQuery : AbstractQuery() {
    override fun execute(conn: Connection): List<Map<String, Any?>> {
        return resultSetToList(conn.createStatement().executeQuery("SELECT * FROM ORGANIZATION_TYPES"))
    }
}