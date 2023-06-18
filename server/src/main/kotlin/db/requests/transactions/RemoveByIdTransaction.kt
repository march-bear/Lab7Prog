package db.requests.transactions

import command.CommandArgument
import message.DataBaseChanges
import java.sql.Connection

class RemoveByIdTransaction(
    private val id: Long,
    private val userId: Long,
) : AbstractTransaction() {
    override fun execute(conn: Connection): DataBaseChanges {
        conn.autoCommit = false
        val query = "DELETE FROM ORGANIZATIONS WHERE owner_id = $userId AND id = $id RETURNING id"
        val res = conn.createStatement().executeQuery(query)

        if (res.next()) {
            val elemId = res.getInt(1).toLong()

            val changeIds = conn.createStatement().executeQuery("UPDATE CHANGES SET number = number + 1 RETURNING number")
            conn.commit()
            conn.autoCommit = true
            var changeId: Long? = null
            if (changeIds.next()) {
                changeId = changeIds.getInt("number").toLong()
            }

            return DataBaseChanges("ORGANIZATIONS", changeId!!, listOf(Pair(ChangeType.DELETE, CommandArgument("$elemId"))))
        }
        return DataBaseChanges("ORGANIZATIONS", -1, listOf())
    }
}