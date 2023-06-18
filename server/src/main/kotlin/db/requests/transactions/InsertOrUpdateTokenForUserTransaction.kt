package db.requests.transactions

import message.DataBaseChanges
import java.sql.Connection

class InsertOrUpdateTokenForUserTransaction : AbstractTransaction() {
    override fun execute(conn: Connection): DataBaseChanges {
        TODO("Not yet implemented")
    }
}