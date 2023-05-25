package db

import org.apache.commons.dbcp.BasicDataSource
import organization.OrganizationType
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class DataBaseManager(
    host: String,
    port: Int,
    name: String,
    user: String,
    passwd: String,
) {
    val connection: Connection
        get() = connPool.connection

    private val connPool = BasicDataSource()

    init {
        DriverManager.registerDriver(org.postgresql.Driver())

        connPool.url = "jdbc:postgresql://$host:$port/$name"
        connPool.username = user
        connPool.password = passwd

        connPool.maxActive = 32
    }

    fun getConnection(): Connection {
        try {
            return connPool.connection
        } catch (ex: SQLException) {
            ex.printStackTrace()
            throw ex
        }
    }

    fun initTables() {
        val conn = connection
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

        val orgTypes = OrganizationType.values()

        for (i in orgTypes.indices) {
            val stat = conn.prepareStatement("INSERT INTO ORGANIZATION_TYPES VALUES(?, ?) ON CONFLICT DO NOTHING")
            stat.setInt(1, i + 1)
            stat.setString(2, orgTypes[i].name)
            stat.execute()
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
    }
}