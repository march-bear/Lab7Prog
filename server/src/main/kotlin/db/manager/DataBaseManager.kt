package db.manager

import db.requests.queries.AbstractQuery
import db.requests.transactions.AbstractTransaction
import message.DataBaseChanges
import org.apache.commons.dbcp.BasicDataSource
import org.postgresql.util.PSQLException
import organization.OrganizationType
import java.sql.*

class DataBaseManager(
    host: String,
    port: Int,
    name: String,
    user: String,
    passwd: String,
) {
    private val connPool = BasicDataSource()

    init {
        DriverManager.registerDriver(org.postgresql.Driver())

        connPool.url = "jdbc:postgresql://$host:$port/$name"
        connPool.username = user
        connPool.password = passwd

        connPool.maxActive = 32
    }

    private fun getConnection(): Connection? {
        return try {
            connPool.connection
        } catch (ex: SQLException) {
            ex.printStackTrace()
            null
        }
    }

    fun initTables() {
        val conn = getConnection() ?: throw SQLException("Невозможно инициализировать таблицы")

        try {
            val stat = conn.createStatement()

            stat.execute(
                "CREATE TABLE IF NOT EXISTS USERS(" +
                        "id SERIAL PRIMARY KEY," +
                        "login TEXT UNIQUE NOT NULL," +
                        "passwd_hash TEXT NOT NULL" +
                        ")"
            )

            stat.execute(
                "CREATE TABLE IF NOT EXISTS TOKENS(" +
                        "id SERIAL PRIMARY KEY," +
                        "user_id BIGINT NOT NULL REFERENCES USERS(id) ON DELETE CASCADE," +
                        "token_hash TEXT UNIQUE NOT NULL," +
                        "last_use TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "valid BOOLEAN NOT NULL" +
                        ")"
            )

            stat.execute(
                "CREATE TABLE IF NOT EXISTS COORDINATES(" +
                        "id SERIAL PRIMARY KEY," +
                        "x DOUBLE PRECISION NOT NULL," +
                        "y INT NOT NULL," +
                        "UNIQUE(x, y)" +
                        ")"
            )

            stat.execute(
                "CREATE TABLE IF NOT EXISTS ADDRESSES(" +
                        "id SERIAL PRIMARY KEY," +
                        "zipcode TEXT UNIQUE NOT NULL" +
                        ")"
            )

            stat.execute(
                "CREATE TABLE IF NOT EXISTS ORGANIZATION_TYPES(" +
                        "id SMALLINT PRIMARY KEY," +
                        "name TEXT UNIQUE NOT NULL" +
                        ")"
            )

            stat.execute(
                "CREATE TABLE IF NOT EXISTS CHANGES(" +
                        "number BIGINT NOT NULL" +
                        ")"
            )

            val orgTypes = OrganizationType.values()

            for (i in orgTypes.indices) {
                try {
                    val stat = conn.prepareStatement("INSERT INTO ORGANIZATION_TYPES VALUES(?, ?) ON CONFLICT DO NOTHING")
                    stat.setInt(1, i + 1)
                    stat.setString(2, orgTypes[i].name)
                    stat.execute()
                    stat.close()
                } catch (ex: SQLException) {
                    conn.close()
                    throw SQLException("Не удалось инициализировать таблицы")
                }
            }

            stat.execute(
                "CREATE TABLE IF NOT EXISTS ORGANIZATIONS(" +
                        "id SERIAL PRIMARY KEY," +
                        "name TEXT NOT NULL," +
                        "date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "coord_id BIGINT NOT NULL REFERENCES COORDINATES(id) ON DELETE CASCADE," +
                        "annual_turnover INT NOT NULL CHECK(annual_turnover > 0)," +
                        "full_name TEXT UNIQUE," +
                        "employees_count INT CHECK(employees_count > 0)," +
                        "organization_type_id SMALLINT REFERENCES ORGANIZATION_TYPES(id) ON DELETE CASCADE," +
                        "address_id BIGINT REFERENCES ADDRESSES(id) ON DELETE SET NULL," +
                        "owner_id BIGINT REFERENCES USERS(id) ON DELETE SET NULL" +
                        ")"
            )
            stat.close()
            conn.close()
        } catch (ex: SQLException) {
            conn.close()
            throw SQLException("Не удалось инициализировать таблицы")
        }

    }

    fun execute(transaction: AbstractTransaction): DataBaseChanges? {
        val conn = getConnection() ?: return null
        var res: DataBaseChanges? = null

        try {
            res = transaction.execute(conn)
        } catch (ex: SQLException) {
            ex.printStackTrace()
        } catch (ex: PSQLException) {
            ex.printStackTrace()
        }

        try { conn.autoCommit = true; conn.close() } catch (_: SQLException) {}

        return res
    }

    fun execute(query: AbstractQuery): List<Map<String, Any?>>? {
        val conn = getConnection() ?: return null
        var res: List<Map<String, Any?>>? = null

        try {
            res = query.execute(conn)
        } catch (ex: SQLException) {
            ex.printStackTrace()
        } catch (ex: PSQLException) {
            ex.printStackTrace()
        }

        try { conn.autoCommit = true; conn.close() } catch (_: SQLException) {}

        return res
    }
}