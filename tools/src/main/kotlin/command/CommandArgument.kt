package command

import organization.Organization
import kotlinx.serialization.Serializable
import java.util.regex.Pattern

@Serializable
class CommandArgument(private val argumentString: String? = null) {
    private companion object {
        val argsPattern: Pattern = Pattern.compile("(?<=^|\\s)\"(.*?)\"(?=\\s|\$)|(?<=^|\\s)(.*?)(?=\\s|\$)")
    }

    val primitiveTypeArguments: List<String>?
    
    var organization: Organization? = null
        private set

    var needAnOrganization: Boolean = false

    init {
        primitiveTypeArguments = if (argumentString == null || argumentString.trim() == "") {
            null
        } else {
            val matcher = argsPattern.matcher(argumentString.trim())

            val tmpArgs = ArrayList<String>()
            while (matcher.find())
                tmpArgs.add(matcher.group())
            if (tmpArgs.last() == "\\") {
                tmpArgs.removeLast()
                needAnOrganization = true
            }

            tmpArgs
        }
    }
    fun setOrganization(org: Organization): Boolean {
        if (!needAnOrganization)
            return false

        organization = org
        return true
    }
}