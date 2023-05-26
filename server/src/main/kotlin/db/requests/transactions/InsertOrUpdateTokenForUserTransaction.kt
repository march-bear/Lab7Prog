package db.requests.transactions

import message.Infarct
import java.sql.Connection

class InsertOrUpdateTokenForUserTransaction : AbstractTransaction() {
    override fun execute(conn: Connection): Infarct? {
        TODO("Not yet implemented")
    }
}