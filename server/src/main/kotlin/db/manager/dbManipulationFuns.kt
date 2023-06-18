package db.manager

import db.requests.transactions.UpdateTokenInfoTransaction
import java.util.regex.Pattern

const val TOKEN_VALIDITY_PERIOD = 600000L
val spaces = Pattern.compile("\\s").toRegex()

fun DataBaseManager.checkToken(token: String, updateLastUse: Boolean = true): Boolean? {
    val res = execute(UpdateTokenInfoTransaction(token)) ?: return null
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