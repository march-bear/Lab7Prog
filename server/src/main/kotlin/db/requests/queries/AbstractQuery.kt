package db.requests.queries

import java.sql.Connection
import java.sql.ResultSet
import java.sql.Types
import java.util.*


abstract class AbstractQuery {
    abstract fun execute(conn: Connection): List<Map<String, Any?>>
}

fun resultSetToList(rs: ResultSet): List<Map<String, Any?>> {
    val rsmd = rs.metaData
    val res: MutableList<Map<String, Any?>> = ArrayList()

    while (rs.next()) {
        val numColumns = rsmd.columnCount
        val obj: MutableMap<String, Any?> = HashMap()
        for (i in 1 until numColumns + 1) {
            val columnName = rsmd.getColumnName(i).lowercase(Locale.getDefault())
            if (rsmd.getColumnType(i) == Types.BIGINT) {
                obj[columnName] = rs.getLong(columnName)
            } else if (rsmd.getColumnType(i) == Types.BOOLEAN) {
                obj[columnName] = rs.getBoolean(columnName)
            } else if (rsmd.getColumnType(i) == Types.DOUBLE) {
                obj[columnName] = rs.getDouble(columnName)
            } else if (rsmd.getColumnType(i) == Types.FLOAT) {
                obj[columnName] = rs.getDouble(columnName)
            } else if (rsmd.getColumnType(i) == Types.INTEGER) {
                obj[columnName] = rs.getInt(columnName)
            } else if (rsmd.getColumnType(i) == Types.SMALLINT) {
                obj[columnName] = rs.getShort(columnName)
            } else if (rsmd.getColumnType(i) == Types.DATE) {
                obj[columnName] = rs.getDate(columnName)
            } else if (rsmd.getColumnType(i) == Types.TIME) {
                obj[columnName] = rs.getTime(columnName)
            } else if (rsmd.getColumnType(i) == Types.TIMESTAMP) {
                obj[columnName] = rs.getTimestamp(columnName).time
            } else {
                obj[columnName] = rs.getString(i)
            }
        }
        res.add(obj)
    }
    return res
}