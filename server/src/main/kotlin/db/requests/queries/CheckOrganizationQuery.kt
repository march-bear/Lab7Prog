package db.requests.queries

import organization.Organization
import java.sql.Connection

class CheckOrganizationQuery(
    private val org: Organization,
) : AbstractQuery() {
    override fun execute(conn: Connection): List<Map<String, Any?>> {
        var query = "SELECT 1 FROM ORGANIZATIONS WHERE full_name"

        query += if (org.fullName == null) {
            " IS NULL"
        } else {
            " = '${org.fullName}'"
        }


        return resultSetToList(conn.createStatement().executeQuery(query))
    }
}