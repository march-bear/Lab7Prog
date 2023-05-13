package db

import com.google.common.hash.Hashing
import java.sql.Timestamp
import java.util.regex.Pattern

const val TOKEN_VALIDITY_PERIOD = 600000L
val spaces = Pattern.compile("\\s").toRegex()


fun DataBaseManager.checkToken(token: String, updateLastUse: Boolean = true): Boolean {
    val tokenHash = Hashing.sha1().hashString(token, charset("UTF-8")).toString()
    val tokens = connection.createStatement().executeQuery(
        "SELECT * FROM TOKENS WHERE TOKENS.token_hash = $tokenHash"
    )
    if (tokens.next()) {
        if (System.currentTimeMillis() - tokens.getTimestamp("lastUse").time < TOKEN_VALIDITY_PERIOD) {
            if (tokens.getBoolean("valid")) {
                if (updateLastUse) {
                    connection.createStatement().executeUpdate(
                        "UPDATE TOKENS SET lastUse = '${Timestamp(System.currentTimeMillis())}' WHERE TOKENS.token_hash = $tokenHash"
                    )
                }
                return true
            }
        } else {
            connection.createStatement().executeUpdate(
                "UPDATE TOKENS SET valid = 'false' WHERE TOKENS.token_hash = $tokenHash"
            )
        }
    }
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