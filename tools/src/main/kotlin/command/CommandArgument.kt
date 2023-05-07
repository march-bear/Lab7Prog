package command

import organization.Organization
import kotlinx.serialization.Serializable
import java.util.regex.Pattern

@Serializable
class CommandArgument(private val argsString: String? = null) {
    private companion object {
        val argsPattern: Pattern = Pattern.compile("(?<=^|\\s)\"(.*?)\"(?=\\s|\$)|(?<=^|\\s)(.*?)(?=\\s|\$)")
    }

    val primArgs: MutableList<String> = if (argsString == null || argsString.trim() == "") {
        mutableListOf()
    } else {
        val matcher = argsPattern.matcher(argsString.trim())

        val tmpArgs = ArrayList<String>()
        while (matcher.find())
            tmpArgs.add(matcher.group())

        tmpArgs
    }

    var organization: Organization? = null
        private set

    fun setOrganization(org: Organization): Boolean = if (organization != null) { false } else { organization = org; true }
}