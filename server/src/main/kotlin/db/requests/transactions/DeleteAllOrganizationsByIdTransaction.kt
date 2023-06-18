package db.requests.transactions

import ChangeType
import command.CommandArgument
import message.DataBaseChanges
import java.sql.Connection

class DeleteAllOrganizationsByIdTransaction(
    private val userId: Long,
) : AbstractTransaction() {
    override fun execute(conn: Connection): DataBaseChanges {
        conn.autoCommit = false
        val query = "DELETE FROM ORGANIZATIONS WHERE owner_id = $userId RETURNING id"
        val res = conn.createStatement().executeQuery(query)

        if (res.next()) {
            val changes = mutableListOf<Pair<ChangeType, CommandArgument>>()

            do {
                changes.add(Pair(ChangeType.DELETE, CommandArgument("${res.getInt(1).toLong()}")))
            } while (res.next())

            val changeIds = conn.createStatement().executeQuery("UPDATE CHANGES SET number = number + 1 RETURNING number")

            conn.commit()
            conn.autoCommit = true

            var changeId: Long? = null
            if (changeIds.next()) {
                changeId = changeIds.getInt("number").toLong()
            }

            return DataBaseChanges("ORGANIZATIONS", changeId!!, changes)
        }
        return DataBaseChanges("ORGANIZATIONS", -1, listOf())
    }
}