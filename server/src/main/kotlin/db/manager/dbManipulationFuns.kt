package db.manager

import com.google.common.hash.Hashing
import db.requests.queries.GetUserIdByTokenQuery
import db.requests.transactions.UpdateTokenInfoTransaction
import organization.Organization
import java.sql.SQLException
import java.sql.Timestamp
import java.util.regex.Pattern

const val TOKEN_VALIDITY_PERIOD = 600000L
val spaces = Pattern.compile("\\s").toRegex()


fun DataBaseManager.addOrganization(
    org: Organization,
): Int? { /*
    val conn = getConnection()

    conn.autoCommit = false
    val statInsert = conn.prepareStatement(
        "INSERT INTO ORGANIZATIONS(name, coord_id, annual_turnover, full_name, employees_count, " +
                "organization_type_id, address_id, owner_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id"
    )

    statInsert.setString(1, org.name)
    statInsert.setInt(2, org.)
    statInsert.setInt(3, annualTurnover)
    statInsert.setString(4, fullName)
    if (employeesCount != null) statInsert.setLong(5, employeesCount) else statInsert.setNull(5, Types.BIGINT)
    statInsert.setInt(6, typeId)
    if (postalAddressId != null) statInsert.setInt(7, postalAddressId) else statInsert.setNull(7, Types.INTEGER)
    statInsert.setInt(8, owner_id)

    val res = statInsert.executeQuery()

    conn.commit()

    if (res.next()) {
        return res.getInt("id")
    }
*/

    return null
}

fun DataBaseManager.checkToken(token: String, updateLastUse: Boolean = true): Boolean {
    val res = execute(UpdateTokenInfoTransaction(token)) ?: throw SQLException("Не удалось выполнить запрос")
    return when (res.changes[0].second.primArgs[0]) {
        "t" -> true
        "f" -> false
        else -> throw ImplementationException("Реализация класса необходимой транзакции была изменена")
    }
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