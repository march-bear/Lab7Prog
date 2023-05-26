package db.requests.transactions

import message.Infarct
import java.sql.Connection

abstract class AbstractTransaction {
    abstract fun execute(conn: Connection): Infarct?
}