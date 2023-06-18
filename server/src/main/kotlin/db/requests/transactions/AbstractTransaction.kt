package db.requests.transactions

import message.DataBaseChanges
import java.sql.Connection

abstract class AbstractTransaction {
    abstract fun execute(conn: Connection): DataBaseChanges
}