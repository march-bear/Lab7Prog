package db.requests.queries

import java.sql.Connection
import java.sql.ResultSet

abstract class AbstractQuery {
    abstract fun execute(conn: Connection): ResultSet
}