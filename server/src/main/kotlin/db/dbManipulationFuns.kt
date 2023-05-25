package db

import com.google.common.hash.Hashing
import org.postgresql.ds.PGConnectionPoolDataSource
import org.postgresql.ds.PGPooledConnection
import organization.Address
import organization.Coordinates
import organization.OrganizationType
import java.sql.Timestamp
import java.sql.Types
import java.util.regex.Pattern
import javax.sql.ConnectionPoolDataSource

const val TOKEN_VALIDITY_PERIOD = 600000L
val spaces = Pattern.compile("\\s").toRegex()


fun DataBaseManager.addOrganization(
    name: String, coordId: Int, annualTurnover: Int, fullName: String?,
    employeesCount: Long?, typeId: Int, postalAddressId: Int?,
    owner_id: Int
): Int? {
    val statInsert = connection.prepareStatement(
        "INSERT INTO ORGANIZATIONS(name, coord_id, annual_turnover, full_name, employees_count, " +
                "organization_type_id, address_id, owner_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id"
    )

    statInsert.setString(1, name)
    statInsert.setInt(2, coordId)
    statInsert.setInt(3, annualTurnover)
    statInsert.setString(4, fullName)
    if (employeesCount != null) statInsert.setLong(5, employeesCount) else statInsert.setNull(5, Types.BIGINT)
    statInsert.setInt(6, typeId)
    if (postalAddressId != null) statInsert.setInt(7, postalAddressId) else statInsert.setNull(7, Types.INTEGER)
    statInsert.setInt(8, owner_id)

    val res = statInsert.executeQuery()
    if (res.next()) {
        return res.getInt("id")
    }
    return null
}

fun DataBaseManager.getUserByToken(token: String): Int? {
    val tokenHash = Hashing.sha1().hashString(token, charset("UTF-8")).toString()
    val tokens = connection.createStatement().executeQuery(
        "SELECT user_id FROM TOKENS WHERE TOKENS.token_hash = '$tokenHash'"
    )

    return if (tokens.next()) {
        tokens.getInt("user_id")
    } else {
        null
    }
}

fun DataBaseManager.checkToken(token: String, updateLastUse: Boolean = true): Boolean {
    val conn = getConnection()
    val tokenHash = Hashing.sha1().hashString(token, charset("UTF-8")).toString()
    val tokens = conn.createStatement().executeQuery(
        "SELECT * FROM TOKENS WHERE TOKENS.token_hash = '$tokenHash'"
    )
    if (tokens.next()) {
        if (System.currentTimeMillis() - tokens.getTimestamp("last_use").time < TOKEN_VALIDITY_PERIOD) {
            if (tokens.getBoolean("valid")) {
                if (updateLastUse) {
                    conn.createStatement().executeUpdate(
                        "UPDATE TOKENS SET last_use = '${Timestamp(System.currentTimeMillis())}' WHERE TOKENS.token_hash = '$tokenHash'"
                    )
                }
                return true
            }
        } else {
            conn.createStatement().executeUpdate(
                "UPDATE TOKENS SET valid = 'false' WHERE TOKENS.token_hash = '$tokenHash'"
            )
        }
    }

    conn.close()
    return false
}

fun checkPasswd(passwd: String): Boolean {
    val pLen = passwd.length
    return pLen in 8..24
            && passwd.replace(spaces, "").length == pLen
            && passwd.toSet().size > pLen / 2
}

fun checkLogin(login: String): Boolean {
    val lLen = login.length
    return lLen in 1..24
            && login.replace(spaces, "").length == lLen
}